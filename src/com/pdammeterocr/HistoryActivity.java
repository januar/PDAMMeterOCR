package com.pdammeterocr;

import android.support.v7.appcompat.*;
import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;
import java.util.List;

import com.pdammeterocr.db.Result;
import com.pdammeterocr.db.ResultDataSource;

import android.os.Bundle;
import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class HistoryActivity extends ActionBarActivity {
	private ListView mListView;
	private ProgressDialog progressDialog;
	public CustomAdapter adapter;
	public ResultDataSource datasource;
	MenuItem delete;

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
			@SuppressWarnings("null")
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
					if(object.isSelected()){
						index.add(object);
						datasource.deleteResult(object); // menghapus object dari sqlite
					}
				}
				
				for (Result ind : index) {
					adapter.data.remove(ind); // menghapus item dari list view
				}
				
				adapter.notifyDataSetChanged(); // adapter di refresh kembali
			} catch (Exception e) {
				// TODO: handle exception
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
	        return ((HeaderViewListAdapter)adapter).getWrappedAdapter();
	    } else {
	        return adapter;
	    }
	}

}
