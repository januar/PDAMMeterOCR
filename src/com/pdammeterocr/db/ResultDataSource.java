package com.pdammeterocr.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;

public class ResultDataSource {

	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_METER_NUMBER,
			MySQLiteHelper.COLUMN_METER_RESULT,
			MySQLiteHelper.COLUMN_DATE,
			MySQLiteHelper.COLUMN_IMAGE};

	public ResultDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
	
	public Result saveResult(Result result) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_METER_NUMBER, result.getMeterNumber());
		values.put(MySQLiteHelper.COLUMN_METER_RESULT, result.getMeterResult());
		values.put(MySQLiteHelper.COLUMN_DATE, result.getDate());
		values.put(MySQLiteHelper.COLUMN_IMAGE, result.getImageByte());
		long insertId = database.insert(MySQLiteHelper.TABLE_RESULT, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_RESULT,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Result newResult = cursorToResult(cursor);
		cursor.close();
		return newResult;
	}
	
	public List<Result> getAllResult() {
		List<Result> resultList = new ArrayList<Result>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_RESULT,
				allColumns, null, null, null, null, MySQLiteHelper.COLUMN_DATE + " DESC");

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Result result = cursorToResult(cursor);
			resultList.add(result);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return resultList;
	}
	
	public void deleteComment(Result result) {
		long id = result.getId();
		System.out.println("Result deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_RESULT, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}
	
	private Result cursorToResult(Cursor cursor) {
		Result result = new Result();
		result.setId(cursor.getInt(0));
		result.setMeterNumber(cursor.getString(1));
		result.setMeterResult(cursor.getString(2));
		byte[] img = cursor.getBlob(4);
		result.setImage(BitmapFactory.decodeByteArray(img, 0, img.length));
		result.setDate(cursor.getString(3));
		return result;
	}

}
