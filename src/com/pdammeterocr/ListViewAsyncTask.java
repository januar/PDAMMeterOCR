package com.pdammeterocr;

import java.util.List;

import com.pdammeterocr.db.Result;
import com.pdammeterocr.db.ResultDataSource;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class ListViewAsyncTask extends AsyncTask<String, String, Boolean> {
	private ProgressDialog progressDialog;
	public HistoryActivity activity;
	private ResultDataSource datasource;
	private CustomAdapter adapter;
	
	public ListViewAsyncTask(ProgressDialog progressDialog, HistoryActivity activity) {
		// TODO Auto-generated constructor stub
		this.progressDialog = progressDialog;
		this.activity = activity;
		datasource = new ResultDataSource(activity);
	}

	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub
		try {
			datasource.open();
			List<Result> data = datasource.getAllResult();
			adapter = new CustomAdapter(activity, data);
			
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
