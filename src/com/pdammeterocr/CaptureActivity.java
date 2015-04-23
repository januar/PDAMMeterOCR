package com.pdammeterocr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.pdammeterocr.camera.*;
import com.pdammeterocr.tesseract.TessOCR;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CaptureActivity extends Activity {

	public Camera mCamera;
	public CameraPreview mPreview;
	public CameraFrame mFrame;
	public CameraConfiguration cameraManager;
	public ProgressDialog progressDialog;
	public Boolean resultVisibility;
	public String imagePath;

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final String APP_IMAGE_PATH = "PDAM Meter OCR";

	private static final String TAG = "CAPTURE ACTIVITY";
	
	private TessBaseAPI ocrEngine;
	private AsyncTask<Object, String, Boolean> recognizer;
	private CaptureActivity activity;

	@SuppressLint("ClickableViewAccessibility") 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_capture);
		if(!OpenCVLoader.initDebug())
		{
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
		}
		this.activity = this;
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Button btn_done = (Button)findViewById(R.id.button_done);
		btn_done.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setIndeterminate(true);
		ocrEngine = new TessBaseAPI();
		recognizer = new TessOCR(progressDialog, ocrEngine, this);
		
		// Create an instance of Camera
		mCamera = CameraConfiguration.getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = (CameraPreview) findViewById(R.id.mPreview);
		mPreview.mCamera = mCamera;
		mPreview.initCamera(mCamera, getApplication());

		mFrame = (CameraFrame) findViewById(R.id.cameraframe_view);
		mFrame.cameraManager.setCamera(mCamera);
		cameraManager = mFrame.cameraManager;
		resultVisibility = false;
		/*
		 * FrameLayout preview = (FrameLayout)
		 * findViewById(R.id.camera_preview); preview.addView(mPreview);
		 */

		mFrame.setOnTouchListener(new View.OnTouchListener() {
			int lastX = -1;
			int lastY = -1;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//Log.d("log", "LastX : " + lastX + " LastY: " + lastY);
				Log.d(TAG, "currentX : " + event.getX() + " currentY: " + event.getY());
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = -1;
					lastY = -1;
					return true;
				case MotionEvent.ACTION_MOVE:
					int currentX = (int) event.getX();
					int currentY = (int) event.getY();

					try {
						Rect rect = cameraManager.getFramingRect();
						Log.d(TAG, "rect.left : " + rect.left + " rect.right: " + rect.right + " rect.top: " + rect.top + " rect.bottom: " + rect.bottom);
						Log.d(TAG, "LastX : " + lastY + " lastY : " + lastY);

						final int BUFFER = 50;
						final int BIG_BUFFER = 60;
						if (lastX >= 0) {
							// Adjust the size of the viewfinder rectangle.
							// Check if the touch event occurs in the corner
							// areas first, because the regions overlap.
							if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left
									+ BIG_BUFFER) || (lastX >= rect.left
									- BIG_BUFFER && lastX <= rect.left
									+ BIG_BUFFER))
									&& ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top
											- BIG_BUFFER) || (lastY <= rect.top
											+ BIG_BUFFER && lastY >= rect.top
											- BIG_BUFFER))) {
								// Top left corner: adjust both top and left
								// sides
								cameraManager.adjustFramingRect(
										2 * (lastX - currentX),
										2 * (lastY - currentY));
							} else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right
									+ BIG_BUFFER) || (lastX >= rect.right
									- BIG_BUFFER && lastX <= rect.right
									+ BIG_BUFFER))
									&& ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top
											- BIG_BUFFER) || (lastY <= rect.top
											+ BIG_BUFFER && lastY >= rect.top
											- BIG_BUFFER))) {
								// Top right corner: adjust both top and right
								// sides
								cameraManager.adjustFramingRect(
										2 * (currentX - lastX),
										2 * (lastY - currentY));
							} else if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left
									+ BIG_BUFFER) || (lastX >= rect.left
									- BIG_BUFFER && lastX <= rect.left
									+ BIG_BUFFER))
									&& ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom
											- BIG_BUFFER) || (lastY <= rect.bottom
											+ BIG_BUFFER && lastY >= rect.bottom
											- BIG_BUFFER))) {
								// Bottom left corner: adjust both bottom and
								// left sides
								cameraManager.adjustFramingRect(
										2 * (lastX - currentX),
										2 * (currentY - lastY));
							} else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right
									+ BIG_BUFFER) || (lastX >= rect.right
									- BIG_BUFFER && lastX <= rect.right
									+ BIG_BUFFER))
									&& ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom
											- BIG_BUFFER) || (lastY <= rect.bottom
											+ BIG_BUFFER && lastY >= rect.bottom
											- BIG_BUFFER))) {
								// Bottom right corner: adjust both bottom and
								// right sides
								cameraManager.adjustFramingRect(
										2 * (currentX - lastX),
										2 * (currentY - lastY));
							} else if (((currentX >= rect.left - BUFFER && currentX <= rect.left
									+ BUFFER) || (lastX >= rect.left - BUFFER && lastX <= rect.left
									+ BUFFER))
									&& ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
								// Adjusting left side: event falls within
								// BUFFER pixels of left side, and between top
								// and bottom side limits
								cameraManager.adjustFramingRect(
										2 * (lastX - currentX), 0);
							} else if (((currentX >= rect.right - BUFFER && currentX <= rect.right
									+ BUFFER) || (lastX >= rect.right - BUFFER && lastX <= rect.right
									+ BUFFER))
									&& ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
								// Adjusting right side: event falls within
								// BUFFER pixels of right side, and between top
								// and bottom side limits
								cameraManager.adjustFramingRect(
										2 * (currentX - lastX), 0);
							} else if (((currentY <= rect.top + BUFFER && currentY >= rect.top
									- BUFFER) || (lastY <= rect.top + BUFFER && lastY >= rect.top
									- BUFFER))
									&& ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
								// Adjusting top side: event falls within BUFFER
								// pixels of top side, and between left and
								// right side limits
								cameraManager.adjustFramingRect(0,
										2 * (lastY - currentY));
							} else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom
									- BUFFER) || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom
									- BUFFER))
									&& ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
								// Adjusting bottom side: event falls within
								// BUFFER pixels of bottom side, and between
								// left and right side limits
								cameraManager.adjustFramingRect(0,
										2 * (currentY - lastY));
							}
						}
					} catch (NullPointerException e) {
						Log.e(TAG, "Framing rect not available", e);
					}
					v.invalidate();
					lastX = currentX;
					lastY = currentY;
					return true;
				case MotionEvent.ACTION_UP:
					lastX = -1;
					lastY = -1;
					return true;
				}
				return false;
			}
		});
	}
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
    	@Override
    	public void onManagerConnected(int status){
    		switch (status) {
	            case LoaderCallbackInterface.SUCCESS:
	            {
	                Log.i(TAG, "OpenCV loaded successfully");
	            } break;
	            default:
	            {
	                super.onManagerConnected(status);
	            } break;
	        }
    	}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.capture, menu);
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

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			mPreview.mCamera.startPreview();
			Toast toast = Toast.makeText(getApplication(), "", Toast.LENGTH_LONG);
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE, false);
			if (pictureFile == null) {
				Log.d(TAG,
						"Error creating media file, check storage permissions");

				toast.setText("Error creating media file, check storage permissions");
				toast.show();
				return;
			}

			try {				
				Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
				Rect rect = cameraManager.getFramingRect();
				Point resolution = cameraManager.getScreenResolution();
				float widthSkala = image.getWidth() / resolution.x;
				float heightSkala = image.getHeight() / resolution.y;
				int newWidth = (int) (rect.width() * widthSkala);
				int newHeight = (int) (rect.height() * heightSkala);
				int newX = (int)(rect.left * widthSkala);
				int newY = (int)(rect.top * heightSkala);
				Bitmap meterImage = Bitmap.createBitmap(image, newX, newY, newWidth, newHeight);
				
				Mat meterImageMat = new Mat();
				Mat grayMeterMat = new Mat();
				Utils.bitmapToMat(meterImage, meterImageMat);
				Imgproc.cvtColor(meterImageMat, grayMeterMat, Imgproc.COLOR_BGR2GRAY);
				Mat destination = new Mat(grayMeterMat.rows(), grayMeterMat.cols(), grayMeterMat.type());
				Imgproc.threshold(grayMeterMat, destination, 0, 255, Imgproc.THRESH_OTSU);
				
				Bitmap desBitmap = meterImage.copy(Bitmap.Config.ARGB_8888, true);
				Utils.matToBitmap(destination, desBitmap);
				FileOutputStream preFos = new FileOutputStream(getOutputMediaFile(MEDIA_TYPE_IMAGE, true));
				desBitmap.compress(CompressFormat.JPEG, 100, preFos);
				
				FileOutputStream fos = new FileOutputStream(pictureFile);
				imagePath = pictureFile.getAbsolutePath();
				recognizer = new TessOCR(progressDialog, ocrEngine, activity);
				recognizer.execute(meterImage);
				meterImage.compress(CompressFormat.JPEG, 100, fos);
				fos.close();
				preFos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
				toast.setText(e.getMessage());
				toast.show();
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
				toast.setText(e.getMessage());
				toast.show();
			}

//			mPreview.mCamera.release();
			//mPreview.mCamera.startPreview();
		}
	};

	public void takePicture(View view) {
		mPreview.mCamera.takePicture(null, null, mPicture);
	}

	/** Create a file Uri for saving an image or video */
/*	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type, false));
	}*/

	/** Create file for saving an image or video */
	@SuppressLint("SimpleDateFormat")
	private static File getOutputMediaFile(int type, Boolean preprocess) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				APP_IMAGE_PATH);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			if(preprocess){
				mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + "_preprocess.jpg");
			}else{
				mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
			}
		} else /*
				 * if(type == MEDIA_TYPE_VIDEO) { mediaFile = new
				 * File(mediaStorageDir.getPath() + File.separator + "VID_"+
				 * timeStamp + ".mp4"); } else
				 */{
			return null;
		}

		return mediaFile;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_FOCUS) {
			// Only perform autofocus if user is not holding down the button.
			if (event.getRepeatCount() == 0) {
				mPreview.requestAutoFocus(500L);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(resultVisibility)
			{
				LinearLayout result_view = (LinearLayout)findViewById(R.id.result_view);
				result_view.setVisibility(View.INVISIBLE);
				RelativeLayout button_done = (RelativeLayout)activity.findViewById(R.id.btndone_layout);
				button_done.setVisibility(View.INVISIBLE);
				RelativeLayout button_layout = (RelativeLayout)findViewById(R.id.button_layout);
				button_layout.setVisibility(View.VISIBLE);
				resultVisibility = false;
				mFrame.setVisibility(View.VISIBLE);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	void resumeOCR() {
	    Log.d(TAG, "resumeOCR()");
	    
	    if (ocrEngine != null) {
	    	ocrEngine.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
//	    	ocrEngine.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, characterBlacklist);
//	    	ocrEngine.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, characterWhitelist);
	    }
	    
	    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
	}
	
	@Override
	public void finish() {
	  // Prepare data intent 
	  Intent data = new Intent();
	  data.putExtra("status", resultVisibility);
	  if(resultVisibility)
	  {
		  TextView result_text_view = (TextView) findViewById(R.id.result_text_view);
		  ImageView image_view = (ImageView)activity.findViewById(R.id.image_view); 
		  data.putExtra("meter", result_text_view.getText());
		  data.putExtra("image", imagePath);
	  }
	  // Activity finished ok, return the data
	  setResult(RESULT_OK, data);
	  super.finish();
	} 
}
