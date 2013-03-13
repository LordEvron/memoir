package com.devapp.memoir;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class MemoirDBA  {

	private MemoirDBHelper mMDBHelper;
	private String DATABASE_NAME = "memoir.db";
	private int DATABASE_VERSION = 1;
	
	public MemoirDBA(Context context) {
		mMDBHelper = new MemoirDBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public ArrayList<ArrayList<Video>> getVideos(long startDate, long endDate, boolean selected) {
		return mMDBHelper.getVideos(new Date(startDate), new Date(endDate), selected);
	}
	
	public long addVideo(Video v) {
		return mMDBHelper.addVideo(v);
	}
	
	private static class MemoirDBHelper extends SQLiteOpenHelper {
		
		private String VIDEOS_TABLE_NAME = "videos";
		
		private String V_TABLE_KEY = "key";
		private String V_TABLE_DATE = "date";
		private String V_TABLE_PATH = "path";
		private String V_TABLE_SELECTED = "selected";
		private String V_TABLE_LENGTH = "length";
		
		private int V_TABLE_KEY_INDEX = 0;
		private int V_TABLE_DATE_INDEX = 1;
		private int V_TABLE_PATH_INDEX = 2;
		private int V_TABLE_SELECTED_INDEX = 3;
		private int V_TABLE_LENGTH_INDEX = 4;

		private String VIDEOS_TABLE_CREATE = "CREATE TABLE " + VIDEOS_TABLE_NAME + " ("
				+ V_TABLE_KEY + " integer primary key autoincrement, "
				+ V_TABLE_DATE + " INTEGER, "
				+ V_TABLE_PATH + " TEXT, "
				+ V_TABLE_SELECTED + " INTEGER, "
				+ V_TABLE_LENGTH + " INTEGER);";

		public MemoirDBHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(VIDEOS_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + VIDEOS_TABLE_NAME);
			onCreate(db);
		}
	
		public ArrayList<ArrayList<Video>> getVideos(Date startDate, Date endDate, boolean selected) {
			SQLiteDatabase db = this.getReadableDatabase();
			
			long t1, t2;
			t1 = (startDate != null) ? startDate.getTime() : 0;
			t2 = (endDate != null) ? endDate.getTime() : -1;
			
			String selection = V_TABLE_DATE + " >= " + t1 + " AND " + V_TABLE_DATE + " <= " + t2;
			if(selected) {
				selection = selection + " AND " + V_TABLE_SELECTED + " = 1 " ;
			}
			Log.d("asd", "Selection Query = " + selection);
			Cursor c = db.query(VIDEOS_TABLE_NAME, null, selection, null, null, null, null);

			ArrayList<ArrayList<Video>> dateList = null;
			long currentDate = 0;
			ArrayList<Video> currentVideoList = null;
			
			if(c.getCount() > 0) {
				dateList = new ArrayList<ArrayList<Video>>();
			}
			
			if (c.moveToFirst()) {
				while(!c.isAfterLast()) {
					long date = c.getLong(V_TABLE_DATE_INDEX);
					if(date != currentDate) {
						currentVideoList = new ArrayList<Video>();
						dateList.add(currentVideoList);
					}
					Video v = new Video(c.getInt(V_TABLE_KEY_INDEX), c.getLong(V_TABLE_DATE_INDEX), c.getString(V_TABLE_PATH_INDEX), c.getInt(V_TABLE_SELECTED_INDEX) > 0 ? true : false, c.getInt(V_TABLE_LENGTH_INDEX));
					currentVideoList.add(v);
					
					c.moveToNext();
				}
			}
			c.close();
			
			return dateList;
		}
		
		public long addVideo (Video v) {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(V_TABLE_DATE, v.date);
			values.put(V_TABLE_PATH, v.path);
			values.put(V_TABLE_SELECTED, v.selected);
			values.put(V_TABLE_LENGTH, v.length);
			
			return db.insert(VIDEOS_TABLE_NAME, null, values);
		}
	}
}
