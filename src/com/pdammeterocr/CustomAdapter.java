package com.pdammeterocr;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pdammeterocr.db.Result;

public class CustomAdapter extends BaseAdapter {
	
	List<Result> data;
	Context context;
    LayoutInflater layoutInflater;

	public CustomAdapter(Context context, List<Result> data) {
		// TODO Auto-generated constructor stub
		super();
		this.data = data;
		this.context = context;
		this.layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View vi = convertView;
		if(convertView == null)
		{
			vi = layoutInflater.inflate(R.layout.rowlayout, null);
			TextView text_meter = (TextView)vi.findViewById(R.id.text_meter);
			TextView text_result = (TextView)vi.findViewById(R.id.text_result);
			ImageView image_item = (ImageView)vi.findViewById(R.id.image_item);
			TextView text_date = (TextView)vi.findViewById(R.id.text_date);
			
			if(data.size() > 0)
			{
				Result item = data.get(position);
				text_meter.setText("Meter Number : " + item.getMeterNumber());
				text_result.setText("scan result : " + item.getMeterResult());
				image_item.setImageBitmap(item.getImage());
				text_date.setText(item.getDate());
			}
		}
		
		return vi;
	}

}
