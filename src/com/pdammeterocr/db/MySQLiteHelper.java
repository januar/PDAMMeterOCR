package com.pdammeterocr.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
	
	public static final String TABLE_RESULT = "results";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_METER_NUMBER = "meter_number";
	public static final String COLUMN_METER_RESULT = "meter_result";
	public static final String COLUMN_IMAGE = "image";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_SENT = "sent";

	private static final String DATABASE_NAME = "pdammeterocr.db";
	private static final int DATABASE_VERSION = 2;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_RESULT + "(" + COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_METER_NUMBER + " text not null, "
			+ COLUMN_METER_RESULT + " text not null, "
			+ COLUMN_IMAGE + " blob not null, "
			+ COLUMN_DATE + " text not null, "
			+ COLUMN_SENT + " integer);";

	public MySQLiteHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public MySQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// membuat database sqlite dan table yang dibutuhkan
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		// mengupgrade database jika versi diperbaharui
		Log.w(MySQLiteHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		    db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESULT);
		    onCreate(db);
	}

}
