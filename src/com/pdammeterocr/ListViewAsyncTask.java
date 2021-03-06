package com.pdammeterocr;

import java.util.List;

import com.pdammeterocr.db.Result;
import com.pdammeterocr.db.ResultDataSource;

import android.app.ProgressDialog;
import android.os.AsyncTask;

// class ini adalah async task untuk mengambil data dari sql lite untuk ditampilkan
// dalam list view. Data yang ditampilkan adalah data history
public final class ListViewAsyncTask extends AsyncTask<String, String, Boolean> {
	private ProgressDialog progressDialog;
	public HistoryActivity activity;
	private ResultDataSource datasource;
	private CustomAdapter adapter;
	
	public ListViewAsyncTask(ProgressDialog progressDialog, HistoryActivity activity, CustomAdapter adapter) {
		// TODO Auto-generated constructor stub
		// inisialisasi datasurce dan item lainnya
		this.progressDialog = progressDialog;
		this.activity = activity;
		this.adapter = adapter;
		datasource = new ResultDataSource(activity);
	}

	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub
		try {
			datasource.open(); // membuka koneksi ke sql lite
			List<Result> data = datasource.getAllResult(); // mengambil data dari sql lite
			adapter = new CustomAdapter(activity, data); // data yang diterima di masukkan kedalam adapter list view 
														 //untuk ditampilkan
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog.setCancelable(false);
		progressDialog.setMessage("Retrive data ...");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		try {
			activity.setListAdapter(adapter);
//			adapter.notifyDataSetChanged();
			progressDialog.dismiss();
			activity.adapter = this.adapter;
			activity.datasource = this.datasource;
		} catch (IllegalArgumentException e) {
			// Catch "View not attached to window manager" error, and continue
		}
	}
	
	@Override
	protected void onProgressUpdate(String... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		progressDialog.setMessage(values[0]);
	}

}
