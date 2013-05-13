package com.devapp.memoir.database;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.devapp.memoir.MemoirApplication;

public class MemoirDBA {

	private MemoirDBHelper mMDBHelper;
	private String DATABASE_NAME = "memoir.db";
	private int DATABASE_VERSION = 1;
	private Context cxt = null;

	private List<List<Video>> localVideos = null;
	private long localSD = 0, localED = 0;
	private boolean localSelected = false, localShowOnlyMultiple = false;

	public MemoirDBA(Context context) {
		this.cxt = context;
		mMDBHelper = new MemoirDBHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
	}

	public List<List<Video>> getVideos(long startDate, long endDate,
			boolean selected, boolean showOnlyMultiple) {
		if(localVideos == null || startDate != localSD || endDate != localED || selected != localSelected || showOnlyMultiple != localShowOnlyMultiple) {
			localVideos = mMDBHelper.getVideos(this.cxt, startDate, endDate, selected,
					showOnlyMultiple);
			localSD = startDate;
			localED = endDate;
			localSelected = selected;
			localShowOnlyMultiple = showOnlyMultiple;
		} else {
			Log.d("asd", "Reading videos from cache");
		}
		return localVideos;
	}

	public long addVideo(Video v) {
		localVideos = null;
		v.thumbnailPath = MemoirApplication.storeThumbnail(cxt, v.path);
		return mMDBHelper.addVideo(v);
	}

	public int deleteVideo(Video v) {
		localVideos = null;
		if (!v.path.isEmpty())
			return mMDBHelper.deleteVideo(v);
		return 0;
	}

	public boolean selectVideo(Video v) {
		localVideos = null;
		return mMDBHelper.selectVideo(v);
	}

	public boolean checkVideoInLimit() {
		return mMDBHelper.checkVideoInLimit();
	}

	public boolean checkIfAnyUserVideo() {
		return mMDBHelper.checkIfAnyUserVideo();
	}

	public void updateDatabase() {
		mMDBHelper.updateDatabase();
	}

	public void setStartEndDates() {
		mMDBHelper.setStartEndDates();
	}

	private static class MemoirDBHelper extends SQLiteOpenHelper {
		private SharedPreferences mPrefs = null;
		private String VIDEOS_TABLE_NAME = "videos";

		private String V_TABLE_KEY = "key";
		private String V_TABLE_DATE = "date";
		private String V_TABLE_PATH = "path";
		private String V_TABLE_THUMBNAIL_PATH = "thumbnail";
		private String V_TABLE_SELECTED = "selected";
		private String V_TABLE_LENGTH = "length";
		private String V_TABLE_USER_TAKEN = "usertaken";

		private int V_TABLE_KEY_INDEX = 0;
		private int V_TABLE_DATE_INDEX = 1;
		private int V_TABLE_PATH_INDEX = 2;
		private int V_TABLE_THUMBNAIL_PATH_INDEX = 3;
		private int V_TABLE_SELECTED_INDEX = 4;
		private int V_TABLE_LENGTH_INDEX = 5;
		private int V_TABLE_USER_TAKEN_INDEX = 6;

		private String VIDEOS_TABLE_CREATE = "CREATE TABLE "
				+ VIDEOS_TABLE_NAME + " (" + V_TABLE_KEY
				+ " integer primary key autoincrement, " + V_TABLE_DATE
				+ " INTEGER, " + V_TABLE_PATH + " TEXT, "
				+ V_TABLE_THUMBNAIL_PATH + " TEXT, " + V_TABLE_SELECTED
				+ " INTEGER, " + V_TABLE_LENGTH + " INTEGER, "
				+ V_TABLE_USER_TAKEN + " INTEGER);";

		public MemoirDBHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			mPrefs = context.getSharedPreferences("com.devapp.memoir",
					Context.MODE_PRIVATE);
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

		public List<List<Video>> getVideos(Context cxt, long startDate,
				long endDate, boolean selected, boolean showOnlyMultiple) {
			SQLiteDatabase db = this.getReadableDatabase();

			long t1, t2;
			t1 = startDate >= 0 ? startDate : 0;
			t2 = endDate > 0 ? endDate : 29990101;

			String selection = V_TABLE_DATE + " >= " + t1 + " AND "
					+ V_TABLE_DATE + " <= " + t2;
			if (selected) {
				selection = selection + " AND " + V_TABLE_SELECTED + " = 1 ";
			}
			// Log.d("asd", "Selection Query = " + selection);
			String orderBy = V_TABLE_DATE + " DESC ";

			Cursor c = db.query(VIDEOS_TABLE_NAME, null, selection, null, null,
					null, orderBy);

			List<List<Video>> dateList = null;
			long currentDate = 0;
			List<Video> currentVideoList = null;

			if (c.getCount() > 0) {
				dateList = new ArrayList<List<Video>>();
			}

			if (c.moveToFirst()) {
				while (!c.isAfterLast()) {
					long date = c.getLong(V_TABLE_DATE_INDEX);
					if (date != currentDate) {
						currentVideoList = new ArrayList<Video>();
						dateList.add(currentVideoList);
						currentDate = date;
					}

					Video v = new Video(
							c.getInt(V_TABLE_KEY_INDEX),
							c.getLong(V_TABLE_DATE_INDEX),
							c.getString(V_TABLE_PATH_INDEX),
							c.getString(V_TABLE_THUMBNAIL_PATH_INDEX),
							c.getInt(V_TABLE_SELECTED_INDEX) > 0 ? true : false,
							c.getInt(V_TABLE_LENGTH_INDEX),
							c.getInt(V_TABLE_USER_TAKEN_INDEX) > 0 ? true
									: false);
					currentVideoList.add(v);

					c.moveToNext();
				}
			}
			c.close();

			List<List<Video>> dateList2 = null;
			if (showOnlyMultiple && dateList != null) {
				for (List<Video> lv : dateList) {
					if (lv.size() > 1) {
						if (dateList2 == null) {
							dateList2 = new ArrayList<List<Video>>();
						}
						dateList2.add(lv);
					}
				}
				return dateList2;
			}

			return dateList;
		}

		public long addVideo(Video v) {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(V_TABLE_DATE, v.date);
			values.put(V_TABLE_PATH, v.path);
			values.put(V_TABLE_THUMBNAIL_PATH, v.thumbnailPath);
			values.put(V_TABLE_SELECTED, v.selected == true ? 1 : 0);
			values.put(V_TABLE_LENGTH, v.length);
			values.put(V_TABLE_USER_TAKEN, v.userTaken == true ? 1 : 0);

			// Log.d("asd", "Inserting values Date>" + v.date + "  Path>" +
			// v.path + "   selected>" + v.selected + "  >length" + v.length);
			return db.insert(VIDEOS_TABLE_NAME, null, values);
		}

		public int deleteVideo(Video v) {

			SQLiteDatabase db = this.getWritableDatabase();

			String whereClause = V_TABLE_PATH + " = '" + v.path + "'";
			int returnValue = db.delete(VIDEOS_TABLE_NAME, whereClause, null);

			File file = new File(v.path);
			file.delete();

			file = new File(v.path.substring(0, v.path.length() - 3) + "png");
			file.delete();

			return returnValue;
		}

		public boolean selectVideo(Video v) {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(V_TABLE_SELECTED, 0);
			String whereClause = V_TABLE_DATE + " = " + v.date;
			db.update(VIDEOS_TABLE_NAME, values, whereClause, null);

			values.remove(V_TABLE_SELECTED);
			values.put(V_TABLE_SELECTED, 1);
			whereClause = V_TABLE_PATH + " = '" + v.path + "'";
			if (db.update(VIDEOS_TABLE_NAME, values, whereClause, null) > 0)
				return true;

			return false;
		}

		public boolean checkVideoInLimit() {
			SQLiteDatabase db = this.getReadableDatabase();
			SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
			long d = Long.parseLong(ft.format(new Date()));

			String selection = V_TABLE_DATE + " = " + d;
			Cursor c = db.query(VIDEOS_TABLE_NAME, null, selection, null, null,
					null, null);
			if (c.getCount() < 5) {
				c.close();
				return true;
			}
			c.close();
			return false;
		}

		public boolean checkIfAnyUserVideo() {
			SQLiteDatabase db = this.getReadableDatabase();
			SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
			long d = Long.parseLong(ft.format(new Date()));

			String selection = V_TABLE_DATE + " = " + d + " AND "
					+ V_TABLE_USER_TAKEN + " > 0";
			Cursor c = db.query(VIDEOS_TABLE_NAME, null, selection, null, null,
					null, null);
			if (c.getCount() > 0) {
				c.close();
				return true;
			}
			c.close();
			return false;
		}

		public void updateDatabase() {
			SQLiteDatabase db = this.getReadableDatabase();
			StringBuilder sb = new StringBuilder("1 = 0");
			String[] columns = { V_TABLE_KEY, V_TABLE_PATH,
					V_TABLE_THUMBNAIL_PATH };
			Cursor c = db.query(VIDEOS_TABLE_NAME, columns, null, null, null,
					null, null);
			if (c.getCount() > 0) {
				if (c.moveToFirst()) {
					while (!c.isAfterLast()) {
						if (!(new File(c.getString(1)).exists())) {
							sb.append(" or " + V_TABLE_KEY + " = "
									+ c.getInt(0) + " ");
							new File(c.getString(2)).delete();
							mPrefs.edit()
									.putBoolean(
											"com.devapp.memoir.datachanged",
											true).commit();
						}
						c.moveToNext();
					}
				}
			}
			c.close();

			db = this.getWritableDatabase();
			db.delete(VIDEOS_TABLE_NAME, sb.toString(), null);
		}

		public void setStartEndDates() {
			SQLiteDatabase db = this.getReadableDatabase();
			String[] columns = { V_TABLE_DATE };
			String orderBy = V_TABLE_DATE + " DESC";
			Cursor c = db.query(VIDEOS_TABLE_NAME, columns, null, null, null,
					null, orderBy);
			if (c.getCount() > 0) {
				if (c.moveToFirst()) {
					mPrefs.edit()
							.putLong("com.devapp.memoir.endall", c.getInt(0))
							.commit();
				}
			}
			c.close();

			orderBy = V_TABLE_DATE;
			c = db.query(VIDEOS_TABLE_NAME, columns, null, null, null, null,
					orderBy);
			if (c.getCount() > 0) {
				if (c.moveToFirst()) {
					mPrefs.edit()
							.putLong("com.devapp.memoir.startall", c.getInt(0))
							.commit();
				}
			}
			c.close();
		}
	}
}
