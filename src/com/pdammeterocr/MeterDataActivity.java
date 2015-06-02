package com.pdammeterocr;

import android.support.v7.appcompat.*;
import android.support.v7.app.ActionBarActivity;
import java.io.File;
import com.google.zxing.integration.android.IntentIntegrator;
import com.pdammeterocr.db.Result;
import com.pdammeterocr.db.ResultDataSource;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MeterDataActivity extends ActionBarActivity {
	private static int REQUEST_CODE = 21;
	private Activity activity;
	private String imagePath;
	private ResultDataSource datasource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meter_data);
		this.activity = this;
		this.datasource = new ResultDataSource(this);
		datasource.open();
		
		Button btn_scan_meter = (Button) findViewById(R.id.btn_scan_meter);
		btn_scan_meter.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent capture_activity = new Intent(getApplication(), CaptureActivity.class);
				startActivityForResult(capture_activity, REQUEST_CODE);
			}
		});
		
		Button btn_qrcode_scanner = (Button)findViewById(R.id.btn_qrcode_scan);
		btn_qrcode_scanner.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
	                IntentIntegrator scanIntegrator = new IntentIntegrator(activity);
	                scanIntegrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
	            } catch (Exception e) {    
	                Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
	                Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
	                startActivity(marketIntent);
	            }
			}
		});
		
		EditText txt_meter_number = (EditText)findViewById(R.id.txt_meter_number);
		txt_meter_number.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				check();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				check();
			}
		});
		
        EditText txt_meter_result = (EditText)findViewById(R.id.txt_meter_result);
        txt_meter_result.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				check();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				check();
			}
		});
        
        Button btn_save = (Button)findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{
					EditText txt_meter_result = (EditText) findViewById(R.id.txt_meter_result);
					EditText txt_meter_number = (EditText) findViewById(R.id.txt_meter_number);
					File imageFile = new File(imagePath);
					Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

					Result result = new Result(txt_meter_number.getText()
							.toString(), txt_meter_result.getText().toString(),
							image);
					datasource.saveResult(result);
					Toast.makeText(activity, "Save success", Toast.LENGTH_SHORT).show();
					finish();
				}
				catch(NullPointerException en){
					Toast.makeText(activity, "Image not found. Please start scan!", Toast.LENGTH_LONG).show();
				}
				catch(Exception e)
				{
					Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
			if (data.hasExtra("status")) {
				if (data.getExtras().getBoolean("status")) {
					String meter = data.getExtras().getString("meter");
					EditText txt_meter_result = (EditText)findViewById(R.id.txt_meter_result);
					txt_meter_result.setText(meter);
					
					imagePath = data.getExtras().getString("image");
					File imageFile = new File(imagePath);
					ImageView result_image = (ImageView)findViewById(R.id.result_image);
					Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
					result_image.setImageBitmap(image);
					Toast.makeText(this, "Scan success",
							Toast.LENGTH_SHORT).show();
				}
			}
		}else if(resultCode == RESULT_OK && requestCode == IntentIntegrator.REQUEST_CODE)
		{
			//qrcode return result
			String contents = data.getStringExtra("SCAN_RESULT");
            String format = data.getStringExtra("SCAN_RESULT_FORMAT");
            EditText txt_meter_number = (EditText)findViewById(R.id.txt_meter_number);
            txt_meter_number.setText(contents);
            Toast.makeText(this, contents + " - " + format, Toast.LENGTH_LONG).show();
		}
	}
	
	public void check() {
		Button btn_save = (Button)findViewById(R.id.btn_save);
		EditText txt_meter_number = (EditText)findViewById(R.id.txt_meter_number);
        String meter_number = txt_meter_number.getText().toString();
        EditText txt_meter_result = (EditText)findViewById(R.id.txt_meter_result);
        String meter_result = txt_meter_result.getText().toString();
        
        if(meter_number.trim().length() != 0 && meter_result.trim().length() != 0)
        {
        	btn_save.setEnabled(true);
        }else{
        	btn_save.setEnabled(false);
        }
	}
}
