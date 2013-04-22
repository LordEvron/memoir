package com.devapp.memoir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import android.app.Application;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class TranscodingService extends IntentService {

	private File extStorePath;
	
	public TranscodingService() {
		super("TranscodingService");
	    extStorePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
	}

	private Movie appendMovie(Movie m1, Movie m2) throws IOException {
		
		if(m1 == null) {
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
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        if (videoTracks.size() > 0) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }

        return result;
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {

/*		//NOTE: Dont delete this code sweetheart I need it for later purpose 
 * 		Bundle bundle = intent.getExtras();
        PendingIntent receiver = bundle.getParcelable("pendingIntent");
        // Perform the operation associated with PendingIntent
        Intent tmpintent = new Intent();
        try {
			receiver.send(getApplicationContext(), 0, tmpintent);
		} catch (CanceledException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
*/		
		
		Log.d("asd", "in onHandleIntent");
		long startDate, endDate;
		Intent broadcastIntent = new Intent("TranscodingComplete");
		startDate = intent.getLongExtra("startDate", 0);
		endDate = intent.getLongExtra("endDate", -1);
		
		List<List<Video>> dateList = ((MemoirApplication) getApplication()).getDBA().getVideos(startDate, endDate, true);

		if(dateList == null) {
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
			return;
		}
		
		int i = 0, j = 0, k = 0;
		ArrayList<Video> videoList = null;
		Video v = null;
		//Movie[] inMovies = new Movie[dateList.size()];
		ArrayList<Movie> inMovies = new ArrayList<Movie>();
		File videoFile = null;
		
		for(i = 0; i < dateList.size(); i++) {
			videoList = (ArrayList<Video>) dateList.get(i);
			
			for(j = 0; j < videoList.size(); j++) {
				v = videoList.get(j);
				videoFile = new File(v.path);
				
				try {
					inMovies.add(MovieCreator.build(new FileInputStream(videoFile).getChannel()));
//					inMovies[k++] = MovieCreator.build(new FileInputStream(videoFile).getChannel());
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

	        //File video1 = new File(extStorePath, "1.mp4");
	        //File video2 = new File(extStorePath, "2.mp4");

	        //{MovieCreator.build(new FileInputStream(video1).getChannel()), MovieCreator.build(new FileInputStream(video2).getChannel())};

	        /*List<Track> videoTracks = new LinkedList<Track>();
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


	        if (audioTracks.size() > 0) {
	            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
	        }
	        if (videoTracks.size() > 0) {
	            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
	        }*/

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
			    
			    File file = new File(extStorePath, "/Memoir/output.mp4");
			    Log.d("Swati", "File " + file.getAbsolutePath());
			    FileOutputStream fos = new FileOutputStream(file);
				out.getBox(fos.getChannel());
				fos.close();
			    
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    // We can only read the media
			    mExternalStorageAvailable = true;
			    mExternalStorageWriteable = false;
			} else {
			    // Something else is wrong. It may be one of many other states, but all we need
			    //  to know is we can neither read nor write
			    mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
			
			
			
			//FileOutputStream fos = openFileOutput(String.format("output.mp4"), Context.MODE_WORLD_WRITEABLE);
			Log.d("asd", "Reached till the end :) ");
		} catch (IOException e) {
			Log.d("asd", "In IO EXCEPTION " + e);
			e.printStackTrace();
		}
		
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}
}
