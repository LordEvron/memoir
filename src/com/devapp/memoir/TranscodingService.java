package com.devapp.memoir;

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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TimeToSampleBox;
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
//		extStorePath = Environment
//				.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
//		extStorePath = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();
	}
	
	private Movie appendMovie(Movie m1, Movie m2) throws IOException {

		if (m1 == null) {
			return m2;
		}
		List<Track> videoTracks = new LinkedList<Track>();
		List<Track> audioTracks = new LinkedList<Track>();

		for (Track t : m1.getTracks()) {
			if (t.getHandler().equals("soun")) {
				audioTracks.add(t);
			}
			if (t.getHandler().equals("vide")) {
				videoTracks.add(t);
			}
		}
		for (Track t : m2.getTracks()) {
			if (t.getHandler().equals("soun")) {
				audioTracks.add(t);
			}
			if (t.getHandler().equals("vide")) {
				videoTracks.add(t);
			}
		}
		Movie result = new Movie();

		if (audioTracks.size() > 0) {
			result.addTrack(new AppendTrack(audioTracks
					.toArray(new Track[audioTracks.size()])));
		}
		if (videoTracks.size() > 0) {
			result.addTrack(new AppendTrack(videoTracks
					.toArray(new Track[videoTracks.size()])));
		}

		return result;
	}
	
	public void createMyLife(Intent intent) {
		/*
		 * //NOTE: Dont delete this code sweetheart I need it for later purpose
		 * Bundle bundle = intent.getExtras(); PendingIntent receiver =
		 * bundle.getParcelable("pendingIntent"); // Perform the operation
		 * associated with PendingIntent Intent tmpintent = new Intent(); try {
		 * receiver.send(getApplicationContext(), 0, tmpintent); } catch
		 * (CanceledException e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); }
		 */

		// Log.d("asd", "in onHandleIntent");
		long startDate, endDate;
		startDate = intent.getLongExtra("startDate", 0);
		endDate = intent.getLongExtra("endDate", -1);
		Intent broadcastIntent = new Intent(TranscodingService.ActionCreateMyLife);

		File file = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath(), "/Memoir/MyLife.mp4");
		boolean deleted = file.exists() ? file.delete() : false;
		Log.e("asd", "Memoir file deleted ? " + deleted);
		
		List<List<Video>> dateList = ((MemoirApplication) getApplication())
				.getDBA().getVideos(startDate, endDate, true, false);

		if (dateList == null) {
			Log.e("asd", "File not being generated");
			broadcastIntent.putExtra("OutputFileName", "");
			LocalBroadcastManager.getInstance(this).sendBroadcast(
					broadcastIntent);
			return;
		}

		int i = 0, j = 0, k = 0;
		ArrayList<Video> videoList = null;
		Video v = null;
		// Movie[] inMovies = new Movie[dateList.size()];
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
					// inMovies[k++] = MovieCreator.build(new
					// FileInputStream(videoFile).getChannel());
				} catch (FileNotFoundException e) {
					Log.d("asd", "In FileNotFoundException " + e);
					e.printStackTrace();
				} catch (IOException e) {
					Log.d("asd", "In IO EXCEPTION " + e);
					e.printStackTrace();
				}
			}
		}

		try {
			Movie result = null;

			for (Movie m : inMovies) {
				result = appendMovie(result, m);
			}

			IsoFile out = new DefaultMp4Builder().build(result);

			boolean mExternalStorageAvailable = false;
			boolean mExternalStorageWriteable = false;
			String state = Environment.getExternalStorageState();

			if (Environment.MEDIA_MOUNTED.equals(state)) {
				// We can read and write the media
				mExternalStorageAvailable = mExternalStorageWriteable = true;

				Log.d("asd", "File " + file.getAbsolutePath());
				FileOutputStream fos = new FileOutputStream(file);
				out.getBox(fos.getChannel());
				fos.close();

			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				// We can only read the media
				mExternalStorageAvailable = true;
				mExternalStorageWriteable = false;
			} else {
				// Something else is wrong. It may be one of many other states,
				// but all we need
				// to know is we can neither read nor write
				mExternalStorageAvailable = mExternalStorageWriteable = false;
			}

			Log.d("asd", "Reached till the end :) ");
		} catch (IOException e) {
			Log.d("asd", "In IO EXCEPTION " + e);
			e.printStackTrace();
		}

		broadcastIntent.putExtra("OutputFileName", getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath()
				+ "/Memoir/MyLife.mp4");
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}
	
    protected static long getDuration(Track track) {
        long duration = 0;
        for (TimeToSampleBox.Entry entry : track.getDecodingTimeEntries()) {
            duration += entry.getCount() * entry.getDelta();
        }
        return duration;
    }

    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
            TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
            for (int j = 0; j < entry.getCount(); j++) {
                if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                    // samples always start with 1 but we start with zero therefore +1
                    timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
                }
                currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
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
			movie = MovieCreator.build(new FileInputStream(intent.getStringExtra("filePath")).getChannel());
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());
        // remove all tracks we will create new tracks from the old

        double startTime1 = intent.getFloatExtra("startTime", 0);
        double endTime1 = intent.getFloatExtra("endTime", 0);
        Log.d("asd", "StartTime > " + startTime1 + "  endtime" + endTime1);
//        double startTime2 =30;
//        double endTime2 = 40;

        boolean timeCorrected = false;

        // Here we try to find a track that has sync samples. Since we can only start decoding
        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
        // such a frame
        for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                if (timeCorrected) {
                    // This exception here could be a false positive in case we have multiple tracks
                    // with sync samples at exactly the same positions. E.g. a single movie containing
                    // multiple qualities of the same video (Microsoft Smooth Streaming file)

                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                }
                startTime1 = correctTimeToSyncSample(track, startTime1, false);
                endTime1 = correctTimeToSyncSample(track, endTime1, true);
//                startTime2 = correctTimeToSyncSample(track, startTime2, false);
//                endTime2 = correctTimeToSyncSample(track, endTime2, true);
                timeCorrected = true;
            }
        }

        Log.d("asd", "Time correctedstartTime1 >" + startTime1 + "   endTime1" + endTime1);
        
        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            double lastTime = 0;
            long startSample1 = -1;
            long endSample1 = -1;
//            long startSample2 = -1;
//            long endSample2 = -1;

            for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
                TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
                for (int j = 0; j < entry.getCount(); j++) {


                    if (currentTime > lastTime && currentTime <= startTime1) {
                        // current sample is still before the new starttime
                        startSample1 = currentSample;
                    }
                    if (currentTime > lastTime && currentTime <= endTime1) {
                        // current sample is after the new start time and still before the new endtime
                        endSample1= currentSample;
                    }
/*                    if (currentTime > lastTime && currentTime <= startTime2) {
                        // current sample is still before the new starttime
                        startSample2 = currentSample;
                    }
                    if (currentTime > lastTime && currentTime <= endTime2) {
                        // current sample is after the new start time and still before the new endtime
                        endSample2 = currentSample;
                    }*/
                    lastTime = currentTime;
                    currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
                    currentSample++;
                }
            }
            try {
				movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        long start1 = System.currentTimeMillis();
        IsoFile out = new DefaultMp4Builder().build(movie);
        long start2 = System.currentTimeMillis();
        FileOutputStream fos;
		try {
			fos = new FileOutputStream(intent.getStringExtra("outputFilePath"));
	        FileChannel fc = fos.getChannel();
	        out.getBox(fc);
	        fc.close();
	        fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        long start3 = System.currentTimeMillis();
        Log.d("asd", "Building IsoFile took : " + (start2 - start1) + "ms");
        Log.d("asd", "Writing IsoFile took  : " + (start3 - start2) + "ms");
        Log.d("asd", "Writing IsoFile speed : " + (new File(String.format("output-%f-%f.mp4", startTime1, endTime1)).length() / (start3 - start2) / 1000) + "MB/s");

		broadcastIntent.putExtra("OutputFileName", intent.getStringExtra("outputFilePath"));
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if(intent.getAction().equals(TranscodingService.ActionCreateMyLife)) {
			createMyLife(intent);
		} else if(intent.getAction().equals(TranscodingService.ActionTrimVideo)) {
			trimVideo(intent);
		}
	}
}
	/*
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ShortenExample {


    public static void main(String[] args) throws IOException {
        //Movie movie = new MovieCreator().build(new RandomAccessFile("/home/sannies/suckerpunch-distantplanet_h1080p/suckerpunch-distantplanet_h1080p.mov", "r").getChannel());
        Movie movie = MovieCreator.build(new FileInputStream("D:\\Downloads\\The.Americans.2013.S01E04.HDTV.x264-LOL\\The.Americans.2013.S01E04.HDTV.x264-LOL.mp4").getChannel());

        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());
        // remove all tracks we will create new tracks from the old

        double startTime1 =10;
        double endTime1 = 20;
        double startTime2 =30;
        double endTime2 = 40;

        boolean timeCorrected = false;

        // Here we try to find a track that has sync samples. Since we can only start decoding
        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
        // such a frame
        for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                if (timeCorrected) {
                    // This exception here could be a false positive in case we have multiple tracks
                    // with sync samples at exactly the same positions. E.g. a single movie containing
                    // multiple qualities of the same video (Microsoft Smooth Streaming file)

                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                }
                startTime1 = correctTimeToSyncSample(track, startTime1, false);
                endTime1 = correctTimeToSyncSample(track, endTime1, true);
                startTime2 = correctTimeToSyncSample(track, startTime2, false);
                endTime2 = correctTimeToSyncSample(track, endTime2, true);
                timeCorrected = true;
            }
        }

        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            double lastTime = 0;
            long startSample1 = -1;
            long endSample1 = -1;
            long startSample2 = -1;
            long endSample2 = -1;

            for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
                TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
                for (int j = 0; j < entry.getCount(); j++) {


                    if (currentTime > lastTime && currentTime <= startTime1) {
                        // current sample is still before the new starttime
                        startSample1 = currentSample;
                    }
                    if (currentTime > lastTime && currentTime <= endTime1) {
                        // current sample is after the new start time and still before the new endtime
                        endSample1= currentSample;
                    }
                    if (currentTime > lastTime && currentTime <= startTime2) {
                        // current sample is still before the new starttime
                        startSample2 = currentSample;
                    }
                    if (currentTime > lastTime && currentTime <= endTime2) {
                        // current sample is after the new start time and still before the new endtime
                        endSample2 = currentSample;
                    }
                    lastTime = currentTime;
                    currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
                    currentSample++;
                }
            }
            movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1), new CroppedTrack(track, startSample2, endSample2)));
        }
        long start1 = System.currentTimeMillis();
        IsoFile out = new DefaultMp4Builder().build(movie);
        long start2 = System.currentTimeMillis();
        FileOutputStream fos = new FileOutputStream(String.format("output-%f-%f--%f-%f.mp4", startTime1, endTime1, startTime2, endTime2));
        FileChannel fc = fos.getChannel();
        out.getBox(fc);
        fc.close();
        fos.close();
        long start3 = System.currentTimeMillis();
        System.err.println("Building IsoFile took : " + (start2 - start1) + "ms");
        System.err.println("Writing IsoFile took  : " + (start3 - start2) + "ms");
        System.err.println("Writing IsoFile speed : " + (new File(String.format("output-%f-%f--%f-%f.mp4", startTime1, endTime1, startTime2, endTime2)).length() / (start3 - start2) / 1000) + "MB/s");
    }

    protected static long getDuration(Track track) {
        long duration = 0;
        for (TimeToSampleBox.Entry entry : track.getDecodingTimeEntries()) {
            duration += entry.getCount() * entry.getDelta();
        }
        return duration;
    }

    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
            TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
            for (int j = 0; j < entry.getCount(); j++) {
                if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                    // samples always start with 1 but we start with zero therefore +1
                    timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
                }
                currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
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
    */
