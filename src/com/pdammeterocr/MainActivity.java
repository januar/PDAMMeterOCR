package com.pdammeterocr;

import android.support.v7.appcompat.*;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    
    // click event of capture meter button
    // use for move to Meter Data Activity for take meter data
    public void captureCamera(View view)
    {
    	Intent intent = new Intent(this, MeterDataActivity.class);
    	startActivity(intent);
    }
    
    // click event for history button
    // use for move to History Activity
    public void historyActivity(View view)
    {
    	Intent intent = new Intent(this, HistoryActivity.class);
    	startActivity(intent);
    }
    
    // click event of about button
    // use for move to About activity
    public void aboutActivity(View view) {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}
}
