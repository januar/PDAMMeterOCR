package com.pdammeterocr;

import android.support.v7.appcompat.*;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pdammeterocr.db.History;
import com.pdammeterocr.db.Result;
import com.pdammeterocr.db.ResultDataSource;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class HistoryActivity extends ActionBarActivity {
	private ListView mListView;
	private ProgressDialog progressDialog;
	public CustomAdapter adapter;
	public ResultDataSource datasource;
	MenuItem delete;
	TelephonyManager mngr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);

		// menjalankan async task untuk mengambil data history dari sqlite
		// mucul progress dialog selama proses tersebut
		progressDialog = new ProgressDialog(this);
		new ListViewAsyncTask(progressDialog, this, adapter).execute("");

		ListView list = getListView();
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			// set event untuk list view jika list ditekan lama.
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				adapter.isShow = true;
				adapter.setChecked(position);
				adapter.notifyDataSetChanged();
				delete.setVisible(true);
				return true;
			}
		});

		mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history, menu);
		delete = menu.findItem(R.id.action_delete);
		delete.setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.action_delete:
			// menghapus item list history yang dipilih
			try {
				List<Result> index = new ArrayList<Result>();
				for (Result object : adapter.data) {
					if (object.isSelected()) {
						index.add(object);
						datasource.deleteResult(object); // menghapus object
															// dari sqlite
					}
				}

				for (Result ind : index) {
					adapter.data.remove(ind); // menghapus item dari list view
				}

				adapter.notifyDataSetChanged(); // adapter di refresh kembali
			} catch (Exception e) {
				// TODO: handle exception
			}
		case R.id.action_sync:
			List<History> listitem = new ArrayList<History>();
			listitem = datasource.getToSent();
			
			if(listitem.size() <= 0)
			{
				Toast.makeText(this, "All data have been sent.", Toast.LENGTH_SHORT).show();
			}else{
//				new HttpAsyncTask(listitem).execute("http://192.168.137.1/pdam/api/sent");
				new HttpAsyncTask(listitem).execute("http://pdam-freepro.rhcloud.com/api/sent");
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected ListView getListView() {
		if (mListView == null) {
			mListView = (ListView) findViewById(android.R.id.list);
		}
		return mListView;
	}

	protected void setListAdapter(ListAdapter adapter) {
		getListView().setAdapter(adapter);
	}

	protected ListAdapter getListAdapter() {
		ListAdapter adapter = getListView().getAdapter();
		if (adapter instanceof HeaderViewListAdapter) {
			return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
		} else {
			return adapter;
		}
	}

	/* beberapa method yang digunakan untuk connect dan mengirim data ke server */

	// method untuk mengecek apakah perangkat memiliki koneksi jaringan
	public boolean isConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	// method untuk melakukan pengiriman data ke server melalui http post
	// request
	public String POST(String url, List<History> listHistory) {
		InputStream inputStream = null;
		String result = "";
		try {
			// create http client
			HttpClient httpclient = new DefaultHttpClient();

			// membuat post request ke url yang diberikan
			HttpPost httpPost = new HttpPost(url);

			String json = "";

			// build json object
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("imei", mngr.getDeviceId());
			jsonObject.put("name", android.os.Build.MANUFACTURER + " "
					+ android.os.Build.MODEL);
			
			JSONArray jsonArray = new JSONArray();
			for (History history : listHistory) {
				JSONObject item = new JSONObject();
				item.put("meter_number", history.getMeterNumber());
				item.put("meter_result", history.getMeterResult());
				item.put("date", history.getDate());
				item.put("image", history.getImage());
				jsonArray.put(item);
			}
			jsonObject.put("history", jsonArray);
			

			// convert json to json string
			json = jsonObject.toString();

			// set json to StringEntity
			StringEntity se = new StringEntity(json);

			// set http post entity
			httpPost.setEntity(se);

			// set header
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			// execute post request
			HttpResponse httpResponse = httpclient.execute(httpPost);

			// receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// convert inputstream to string
			if (inputStream != null)
				result = convertInputStreamToString(inputStream);
			else
				result = "{\"status\":false}";
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("pdammeterocr - history", e.getMessage());
			result = "{\"status\":false}";
		}
		return result;
	}

	// method untuk mengconvert input stream dari response http menjadi string
	private static String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;
	}

	private class HttpAsyncTask extends AsyncTask<String, String, Boolean> {
		private List<History> data;
		private boolean succes;
		private String message;
		
		public HttpAsyncTask(List<History> _data){
			this.data =_data;
			this.succes = false;
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				publishProgress(data.size() + " data is sending....");
				String result = POST(params[0], this.data);
				JSONObject out = new JSONObject(result);
				if(out.getBoolean("status") == false){
					Log.e("pdammeterocr - history", "Error connecting to server.");
					message = "Error connecting to server.";
				}else{
					this.succes = true;
					for (History item : this.data) {
						Result rslt = datasource.getById(item.getId());
						rslt.setSent(true);
						datasource.update(rslt);
					}
				}
			}catch (JSONException je)
			{
				Log.e("pdammeterocr - histor", je.getMessage());
				message = "Error get response from server.";
				return false;
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("pdammeterocr - histor", e.getMessage());
				message = e.getMessage();
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progressDialog.dismiss();
			if(this.succes)
			{
				Toast.makeText(getBaseContext(), "Data have been sent",
						Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getBaseContext(), message,
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);

			progressDialog.setMessage(values[0]);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			progressDialog.setCancelable(false);
			progressDialog.setMessage("Send data ...");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.show();
		}
	}

}
