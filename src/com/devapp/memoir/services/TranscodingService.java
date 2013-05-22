package com.devapp.memoir.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.devapp.memoir.MemoirApplication;
import com.devapp.memoir.database.Video;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

public class TranscodingService extends IntentService {

	public String extStorePath;
	public static String ActionCreateMyLife = "ActionCreateMyLife";
	public static String ActionTrimVideo = "ActionTrimVideo";

	public TranscodingService() {
		super("TranscodingService");
		// extStorePath = Environment
		// .getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
		// extStorePath =
		// getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();
	}

	/*
	 * private Movie appendMovie(Movie m1, Movie m2) throws IOException {
	 * 
	 * if (m1 == null) { return m2; } List<Track> videoTracks = new
	 * LinkedList<Track>(); List<Track> audioTracks = new LinkedList<Track>();
	 * 
	 * for (Track t : m1.getTracks()) { if (t.getHandler().equals("soun")) {
	 * audioTracks.add(t); } if (t.getHandler().equals("vide")) {
	 * videoTracks.add(t); } } for (Track t : m2.getTracks()) { if
	 * (t.getHandler().equals("soun")) { audioTracks.add(t); } if
	 * (t.getHandler().equals("vide")) { videoTracks.add(t); } } Movie result =
	 * new Movie();
	 * 
	 * if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks
	 * .toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0)
	 * { result.addTrack(new AppendTrack(videoTracks .toArray(new
	 * Track[videoTracks.size()]))); }
	 * 
	 * return result; }
	 */

	public void createMyLife(Intent intent) {

		String myLifePath = null;
		long startDate, endDate;
		startDate = intent.getLongExtra("startDate", 0);
		endDate = intent.getLongExtra("endDate", -1);
		Intent broadcastIntent = new Intent(
				TranscodingService.ActionCreateMyLife);

		myLifePath = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
				.getAbsolutePath() + "/Memoir/MyLife.mp4";

		File file = new File(MemoirApplication.convertPath(myLifePath));
		boolean deleted = file.exists() ? file.delete() : false;
		file = new File(myLifePath);
		deleted = file.exists() ? file.delete() : false;

		// Log.e("asd", "Memoir file deleted ? " + deleted);

		List<List<Video>> dateList = ((MemoirApplication) getApplication())
				.getDBA().getVideos(startDate, endDate, true, false);

		if (dateList == null) {
			// Log.e("asd", "File not being generated");
			broadcastIntent.putExtra("OutputFileName", "");
			LocalBroadcastManager.getInstance(this).sendBroadcast(
					broadcastIntent);
			return;
		}

		int i = 0, j = 0;
		ArrayList<Video> videoList = null;
		Video v = null;
		ArrayList<Movie> inMovies = new ArrayList<Movie>();
		File videoFile = null;

		Collections.reverse(dateList);
		for (i = 0; i < dateList.size(); i++) {
			videoList = (ArrayList<Video>) dateList.get(i);

			for (j = 0; j < videoList.size(); j++) {
				v = videoList.get(j);
				videoFile = new File(v.path);

				try {
					inMovies.add(MovieCreator.build(new FileInputStream(
							videoFile).getChannel()));
				} catch (FileNotFoundException e) {
					Log.d("asd", "In FileNotFoundException " + e);
					e.printStackTrace();
				} catch (IOException e) {
					Log.d("asd", "In IO EXCEPTION " + e);
					e.printStackTrace();
				}
			}
		}

		List<Track> videoTracks = new LinkedList<Track>();
		List<Track> audioTracks = new LinkedList<Track>();

		for (Movie m : inMovies) {
			for (Track t : m.getTracks()) {
				if (t.getHandler().equals("soun")) {
					audioTracks.add(t);
				}
				if (t.getHandler().equals("vide")) {
					videoTracks.add(t);
				}
			}
		}

		try {
			Movie result = new Movie();

			if (audioTracks.size() > 0) {
				result.addTrack(new AppendTrack(audioTracks
						.toArray(new Track[audioTracks.size()])));
			}
			if (videoTracks.size() > 0) {
				result.addTrack(new AppendTrack(videoTracks
						.toArray(new Track[videoTracks.size()])));
			}

			IsoFile out = new DefaultMp4Builder().build(result);
			FileOutputStream fos = new FileOutputStream(file);
			FileChannel fc = fos.getChannel();
			fc.position(0);
			out.getBox(fc);
			fc.close();
		} catch (IOException e) {
			broadcastIntent.putExtra("OutputFileName", "");
			broadcastIntent.putExtra("Error", "Video that was recently imported is of different quality. Only videos with same quality can be imported. Please delete the same.");
			LocalBroadcastManager.getInstance(this).sendBroadcast(
					broadcastIntent);

			e.printStackTrace();
			return;
		}

		MemoirApplication.mTL.convertThumbnail(myLifePath, MediaStore.Video.Thumbnails.MINI_KIND);

		broadcastIntent.putExtra("OutputFileName", myLifePath);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}

	protected static long getDuration(Track track) {
		long duration = 0;
		for (TimeToSampleBox.Entry entry : track.getDecodingTimeEntries()) {
			duration += entry.getCount() * entry.getDelta();
		}
		return duration;
	}

	private static double correctTimeToSyncSample(Track track, double cutHere,
			boolean next) {
		double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
		long currentSample = 0;
		double currentTime = 0;
		for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
			TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
			for (int j = 0; j < entry.getCount(); j++) {
				if (Arrays.binarySearch(track.getSyncSamples(),
						currentSample + 1) >= 0) {
					// samples always start with 1 but we start with zero
					// therefore +1
					timeOfSyncSamples[Arrays.binarySearch(
							track.getSyncSamples(), currentSample + 1)] = currentTime;
				}
				currentTime += (double) entry.getDelta()
						/ (double) track.getTrackMetaData().getTimescale();
				currentSample++;
			}
		}
		double previous = 0;
		for (double timeOfSyncSample : timeOfSyncSamples) {
			if (timeOfSyncSample > cutHere) {
				if (next) {
					return timeOfSyncSample;
				} else {
					return previous;
				}
			}
			previous = timeOfSyncSample;
		}
		return timeOfSyncSamples[timeOfSyncSamples.length - 1];
	}

	public void trimVideo(Intent intent) {
		Intent broadcastIntent = new Intent(TranscodingService.ActionTrimVideo);
		Movie movie = null;
		try {
			movie = MovieCreator.build(new FileInputStream(intent
					.getStringExtra("filePath")).getChannel());
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		List<Track> tracks = movie.getTracks();
		movie.setTracks(new LinkedList<Track>());

		double startTime1 = intent.getFloatExtra("startTime", 0);
		double endTime1 = intent.getFloatExtra("endTime", 0);

		boolean timeCorrected = false;

		// Here we try to find a track that has sync samples. Since we can only
		// start decoding
		// at such a sample we SHOULD make sure that the start of the new
		// fragment is exactly
		// such a frame
		for (Track track : tracks) {
			if (track.getSyncSamples() != null
					&& track.getSyncSamples().length > 0) {
				if (timeCorrected) {
					// This exception here could be a false positive in case we
					// have multiple tracks
					// with sync samples at exactly the same positions. E.g. a
					// single movie containing
					// multiple qualities of the same video (Microsoft Smooth
					// Streaming file)

					throw new RuntimeException(
							"The startTime has already been corrected by another track with SyncSample. Not Supported.");
				}
				startTime1 = correctTimeToSyncSample(track, startTime1, false);
				endTime1 = correctTimeToSyncSample(track, endTime1, true);
				timeCorrected = true;
			}
		}

		/*
		 * for (Track track : tracks) { long currentSample = 0; double
		 * currentTime = 0; long startSample = -1; long endSample = -1;
		 * 
		 * for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
		 * TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
		 * for (int j = 0; j < entry.getCount(); j++) { // entry.getDelta() is
		 * the amount of time the current sample covers.
		 * 
		 * if (currentTime <= startTime) { // current sample is still before the
		 * new starttime startSample = currentSample; } else if (currentTime <=
		 * endTime) { // current sample is after the new start time and still
		 * before the new endtime endSample = currentSample; } else { // current
		 * sample is after the end of the cropped video break; } currentTime +=
		 * (double) entry.getDelta() / (double)
		 * track.getTrackMetaData().getTimescale(); currentSample++; } }
		 * movie.addTrack(new CroppedTrack(track, startSample, endSample)); }
		 */

		for (Track track : tracks) {
			long currentSample = 0;
			double currentTime = 0;
			double lastTime = -1;
			long startSample1 = -1;
			long endSample1 = -1;

			for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
				TimeToSampleBox.Entry entry = track.getDecodingTimeEntries()
						.get(i);
				for (int j = 0; j < entry.getCount(); j++) {

					if (currentTime > lastTime && currentTime <= startTime1) {
						// current sample is still before the new starttime
						startSample1 = currentSample;
					}
					if (currentTime > lastTime && currentTime <= endTime1) {
						// current sample is after the new start time and still
						// before the new endtime
						endSample1 = currentSample;
					}
					lastTime = currentTime;
					currentTime += (double) entry.getDelta()
							/ (double) track.getTrackMetaData().getTimescale();
					currentSample++;
				}
			}
			try {
				movie.addTrack(new AppendTrack(new CroppedTrack(track,
						startSample1, endSample1)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// long start1 = System.currentTimeMillis();
		IsoFile out = new DefaultMp4Builder().build(movie);
		// long start2 = System.currentTimeMillis();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(intent.getStringExtra("outputFilePath"));
			FileChannel fc = fos.getChannel();
			out.getBox(fc);
			fc.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// long start3 = System.currentTimeMillis();
		// Log.d("asd", "Building IsoFile took : " + (start2 - start1) + "ms");
		// Log.d("asd", "Writing IsoFile took  : " + (start3 - start2) + "ms");
		// Log.d("asd", "Writing IsoFile speed : " + (new
		// File(String.format("output-%f-%f.mp4", startTime1,
		// endTime1)).length() / (start3 - start2) / 1000) + "MB/s");

		broadcastIntent.putExtra("OutputFileName",
				intent.getStringExtra("outputFilePath"));
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (intent.getAction().equals(TranscodingService.ActionCreateMyLife)) {
			createMyLife(intent);
		} else if (intent.getAction()
				.equals(TranscodingService.ActionTrimVideo)) {
			trimVideo(intent);
		}
	}
}
