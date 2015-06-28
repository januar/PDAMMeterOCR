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
	// column dalam tabel history
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_METER_NUMBER,
			MySQLiteHelper.COLUMN_METER_RESULT,
			MySQLiteHelper.COLUMN_DATE,
			MySQLiteHelper.COLUMN_IMAGE,
			MySQLiteHelper.COLUMN_SENT};

	//constructor, inisialisasi datasource
	public ResultDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	// method untuk membuka koneksi ke database
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	// method untuk menutup koneksi ke database
	public void close() {
		dbHelper.close();
	}
	
	// method untuk menyimpan history
	// object Result di convert ke dalam bentuk yang dapat dikenali oleh SQLLite helper
	public Result saveResult(Result result) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_METER_NUMBER, result.getMeterNumber());
		values.put(MySQLiteHelper.COLUMN_METER_RESULT, result.getMeterResult());
		values.put(MySQLiteHelper.COLUMN_DATE, result.getDate());
		values.put(MySQLiteHelper.COLUMN_IMAGE, result.getImageByte());
		values.put(MySQLiteHelper.COLUMN_SENT, 0);
		long insertId = database.insert(MySQLiteHelper.TABLE_RESULT, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_RESULT,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Result newResult = cursorToResult(cursor);
		cursor.close();
		return newResult;
	}
	
	// mengambil seluruh isi table history pada sqlite
	public List<Result> getAllResult() {
		List<Result> resultList = new ArrayList<Result>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_RESULT,
				allColumns, null, null, null, null, MySQLiteHelper.COLUMN_ID + " DESC");

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Result result = cursorToResult(cursor);
//			result.setSent(false);
//			update(result);
			resultList.add(result);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return resultList;
	}
	
	// mengambil result yang akan dikirim
	public List<History> getToSent() {
		List<History> historyList = new ArrayList<History>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_RESULT,
				allColumns, MySQLiteHelper.COLUMN_SENT + " = ?", new String[]{String.valueOf(0)},
				null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Result result = cursorToResult(cursor);
			historyList.add(new History(result));
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return historyList;
	}
	
	// mengambil result berdasarkan id
	public Result getById(int id) {
		Result result = null;
		
		Cursor cursor = database.query(MySQLiteHelper.TABLE_RESULT, allColumns, MySQLiteHelper.COLUMN_ID + " = ?", 
				new String[] {String.valueOf(id)}, null, null, null);
		
		cursor.moveToFirst();
		if(!cursor.isAfterLast()){
			result = cursorToResult(cursor);
		}
		
		cursor.close();
		return result;
	}
	
	public void update(Result result) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_METER_NUMBER, result.getMeterNumber());
		values.put(MySQLiteHelper.COLUMN_METER_RESULT, result.getMeterResult());
		values.put(MySQLiteHelper.COLUMN_DATE, result.getDate());
		values.put(MySQLiteHelper.COLUMN_IMAGE, result.getImageByte());
		values.put(MySQLiteHelper.COLUMN_SENT, (result.isSent() == true)? 1:0);
		database.update(MySQLiteHelper.TABLE_RESULT, values, MySQLiteHelper.COLUMN_ID + " = " + result.getId(), null);
	}
	
	// method menghapus item
	public void deleteResult(Result result) {
		long id = result.getId();
		System.out.println("Result deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_RESULT, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}
	
	// method untuk mengubah object cursor kedalam bentuk Object Result
	private Result cursorToResult(Cursor cursor) {
		Result result = new Result();
		result.setId(cursor.getInt(0));
		result.setMeterNumber(cursor.getString(1));
		result.setMeterResult(cursor.getString(2));
		byte[] img = cursor.getBlob(4);
		result.setImage(BitmapFactory.decodeByteArray(img, 0, img.length));
		result.setDate(cursor.getString(3));
		result.setSent((cursor.getInt(5) == 0)? false : true);
		result.setSelected(false);
		return result;
	}

}
