package com.devapp.memoir;

//import java.sql.Date;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
	private Context cxt = null;
	
	public MemoirDBA(Context context) {
		this.cxt = context;
		mMDBHelper = new MemoirDBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public List<List<Video>> getVideos(long startDate, long endDate, boolean selected) {
		return mMDBHelper.getVideos(this.cxt, startDate, endDate, selected);
	}
	
	public long addVideo(Video v) {
		v.thumbnailPath = MemoirApplication.storeThumbnail(cxt, v.path);
		return mMDBHelper.addVideo(v);
	}
	
	public int deleteVideo(Video v) {
		if(!v.path.isEmpty())
			return mMDBHelper.deleteVideo(v);
		return 0;
	}

	public boolean selectVideo(Video v) {
		return mMDBHelper.selectVideo(v);
	}
	
	private static class MemoirDBHelper extends SQLiteOpenHelper {
		
		private String VIDEOS_TABLE_NAME = "videos";
		
		private String V_TABLE_KEY = "key";
		private String V_TABLE_DATE = "date";
		private String V_TABLE_PATH = "path";
		private String V_TABLE_THUMBNAIL_PATH = "thumbnail";
		private String V_TABLE_SELECTED = "selected";
		private String V_TABLE_LENGTH = "length";
		
		private int V_TABLE_KEY_INDEX = 0;
		private int V_TABLE_DATE_INDEX = 1;
		private int V_TABLE_PATH_INDEX = 2;
		private int V_TABLE_THUMBNAIL_PATH_INDEX = 3;
		private int V_TABLE_SELECTED_INDEX = 4;
		private int V_TABLE_LENGTH_INDEX = 5;

		private String VIDEOS_TABLE_CREATE = "CREATE TABLE " + VIDEOS_TABLE_NAME + " ("
				+ V_TABLE_KEY + " integer primary key autoincrement, "
				+ V_TABLE_DATE + " INTEGER, "
				+ V_TABLE_PATH + " TEXT, "
				+ V_TABLE_THUMBNAIL_PATH + " TEXT, "
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
	
		public List<List<Video>> getVideos(Context cxt, long startDate, long endDate, boolean selected) {
			SQLiteDatabase db = this.getReadableDatabase();
			
			long t1, t2;
			t1 = startDate >=0 ? startDate : 0;
			t2 = endDate > 0 ? endDate : 29990101;
			
			String selection = V_TABLE_DATE + " >= " + t1 + " AND " + V_TABLE_DATE + " <= " + t2;
			if(selected) {
				selection = selection + " AND " + V_TABLE_SELECTED + " = 1 " ;
			}
			//Log.d("asd", "Selection Query = " + selection);
			String orderBy = V_TABLE_DATE + " DESC ";
			
			Cursor c = db.query(VIDEOS_TABLE_NAME, null, selection, null, null, null, orderBy);

			List<List<Video>> dateList = null;
			long currentDate = 0;
			List<Video> currentVideoList = null;
			
			if(c.getCount() > 0) {
				dateList = new ArrayList<List<Video>>();
			}
			
			if (c.moveToFirst()) {
				while(!c.isAfterLast()) {
					long date = c.getLong(V_TABLE_DATE_INDEX);
					//Log.d("asd", "Date of this video is >" + date);
					if(date != currentDate) {
						//Log.d("asd", "New video from a new day");
						currentVideoList = new ArrayList<Video>();
						dateList.add(currentVideoList);
						currentDate = date;
					}
					//Log.d("asd", "reading video");
					
					Video v = new Video(c.getInt(V_TABLE_KEY_INDEX), c.getLong(V_TABLE_DATE_INDEX), c.getString(V_TABLE_PATH_INDEX), c.getString(V_TABLE_THUMBNAIL_PATH_INDEX), c.getInt(V_TABLE_SELECTED_INDEX) > 0 ? true : false, c.getInt(V_TABLE_LENGTH_INDEX));
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
			values.put(V_TABLE_THUMBNAIL_PATH, v.thumbnailPath);
			values.put(V_TABLE_SELECTED, v.selected);
			values.put(V_TABLE_LENGTH, v.length);
			
			//Log.d("asd", "Inserting values Date>" + v.date + "  Path>" + v.path + "   selected>" + v.selected + "  >length" + v.length);
			
			return db.insert(VIDEOS_TABLE_NAME, null, values);
		}
		
		public int deleteVideo (Video v) {
			
			SQLiteDatabase db = this.getWritableDatabase();

			String whereClause = V_TABLE_PATH + " = '" + v.path + "'";
			int returnValue = db.delete(VIDEOS_TABLE_NAME, whereClause, null);
			
			File file = new File(v.path);
			file.delete();

			file = new File(v.path.substring(0, v.path.length() - 3) + "png");
			file.delete();

			/** NOTE: We dont need this here as we are doing it through UI for now 
			 * @Swati : Dont delete this for now :p*/
/*			if(v.selected) {
				int key = 0;
			
				String selection = V_TABLE_DATE + " = " + v.date;
				Cursor c = db.query(VIDEOS_TABLE_NAME, null, selection, null, null, null, null);

				if(c.getCount() > 0) {
					if (c.moveToFirst()) {
						if(!c.isAfterLast()) {
							key = c.getInt(V_TABLE_KEY_INDEX);
						}
					}
					c.close();

					ContentValues values = new ContentValues();
					values.remove(V_TABLE_SELECTED);
					values.put(V_TABLE_SELECTED, 1);
					whereClause = V_TABLE_KEY + " = " + key;
					db.update(VIDEOS_TABLE_NAME, values, whereClause, null);
				}
			}*/
			return returnValue;
		}
		
		public boolean selectVideo (Video v) {
			SQLiteDatabase db = this.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(V_TABLE_SELECTED, 0);
			String whereClause = V_TABLE_DATE + " = " + v.date;
			db.update(VIDEOS_TABLE_NAME, values, whereClause, null);

			values.remove(V_TABLE_SELECTED);
			values.put(V_TABLE_SELECTED, 1);
			whereClause = V_TABLE_PATH  + " = '" + v.path + "'";
			if(db.update(VIDEOS_TABLE_NAME, values, whereClause, null) > 0)
				return true;
			
			return false;
		}
	}
}
