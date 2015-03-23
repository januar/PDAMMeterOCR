package com.pdammeterocr;

import java.io.File;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        PackageManager m = getPackageManager();
        String s = getPackageName();
        PackageInfo p;
		try {
			p = m.getPackageInfo(s, 0);
			s = p.applicationInfo.dataDir;
			String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
	        String language = "eng";
	        File dir = new File(datapath + "tessdata/");
	        if (!dir.exists()) 
	            dir.mkdirs();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void captureCamera(View view)
    {
    	Intent intent = new Intent(this, CaptureActivity.class);
    	startActivity(intent);
//    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//    	
//    	fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
//
//        // start the image capture Intent
//        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
}
