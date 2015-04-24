package com.pdammeterocr;

import java.io.File;

import com.google.zxing.integration.android.IntentIntegrator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MeterDataActivity extends Activity {
	private static int REQUEST_CODE = 21;
	private Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meter_data);
		this.activity = this;
		
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
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
			if (data.hasExtra("status")) {
				if (data.getExtras().getBoolean("status")) {
					String meter = data.getExtras().getString("meter");
					EditText txt_meter_result = (EditText)findViewById(R.id.txt_meter_result);
					txt_meter_result.setText(meter);
					
					String imagePath = data.getExtras().getString("image");
					File imageFile = new File(imagePath);
					ImageView result_image = (ImageView)findViewById(R.id.result_image);
					Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
					result_image.setImageBitmap(image);
					Toast.makeText(this, "Scan success",
							Toast.LENGTH_SHORT).show();
				}
			}
		}else if(resultCode == RESULT_OK && requestCode == 0)
		{
			String contents = data.getStringExtra("SCAN_RESULT");
            String format = data.getStringExtra("SCAN_RESULT_FORMAT");
            Toast.makeText(this, contents + " - " + format, Toast.LENGTH_LONG).show();
		}
	}
}
