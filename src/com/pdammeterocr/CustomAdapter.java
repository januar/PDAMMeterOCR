package com.pdammeterocr;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.pdammeterocr.db.Result;

// class ini adalah custom apdater untuk menampilkan list view dari halaman history
// custom adapter ini berguna untuk membuat list yang sudah ditentukan
public class CustomAdapter extends BaseAdapter {
	
	public Boolean isShow;
	List<Result> data;
	Context context;
    LayoutInflater layoutInflater;

	public CustomAdapter(Context context, List<Result> data) {
		// TODO Auto-generated constructor stub
		super();
		this.data = data;
		this.context = context;
		this.isShow = false;
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
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		// menginisialisasi item dalam list dan memberikan nilai dan eventnya
		View vi = convertView;
		if (convertView == null) {
			vi = layoutInflater.inflate(R.layout.rowlayout, null);
		}

		TextView text_meter = (TextView) vi.findViewById(R.id.text_meter);
		TextView text_result = (TextView) vi.findViewById(R.id.text_result);
		ImageView image_item = (ImageView) vi.findViewById(R.id.image_item);
		TextView text_date = (TextView) vi.findViewById(R.id.text_date);
		final CheckBox chk_selected = (CheckBox) vi
				.findViewById(R.id.chk_selected);

		if (data.size() > 0) {
			final Result item = data.get(position);
			text_meter.setText(" " + item.getMeterNumber());
			text_result.setText(" " + item.getMeterResult());
			image_item.setImageBitmap(item.getImage());
			text_date.setText(item.getDate());
			chk_selected.setChecked(item.isSelected());
			if (isShow) {
				chk_selected.setVisibility(View.VISIBLE);
			}
			
			// event jika checkbox di list di tekan
			chk_selected.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					CheckBox cb = (CheckBox) v;
					item.setSelected(cb.isChecked());
				}
			});
		}

		return vi;
	}
	
	public void setChecked(int position) {
		Result item = data.get(position);
		item.setSelected(true);
	}

}
