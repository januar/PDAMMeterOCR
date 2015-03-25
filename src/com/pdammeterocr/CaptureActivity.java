package com.pdammeterocr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.pdammeterocr.camera.*;

public class CaptureActivity extends Activity {

	public Camera mCamera;
	public CameraPreview mPreview;
	public CameraFrame mFrame;
	public CameraConfiguration cameraManager;

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final String APP_IMAGE_PATH = "PDAM Meter OCR";

	private static final String TAG = "CAPTURE ACTIVITY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Create an instance of Camera
		mCamera = CameraConfiguration.getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = (CameraPreview) findViewById(R.id.mPreview);
		mPreview.mCamera = mCamera;
		mPreview.initCamera(mCamera, getApplication());

		mFrame = (CameraFrame) findViewById(R.id.cameraframe_view);
		mFrame.cameraManager.setCamera(mCamera);
		cameraManager = mFrame.cameraManager;
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
				Log.d("log", "currentX : " + event.getX() + " currentY: " + event.getY());
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
						Log.d("log", "rect.left : " + rect.left + " rect.right: " + rect.right + " rect.top: " + rect.top + " rect.bottom: " + rect.bottom);

						final int BUFFER = 50;
						final int BIG_BUFFER = 60;
						if (lastX >= 0) {
							// Adjust the size of the viewfinder rectangle.
							// Check if the touch event occurs in the corner
							// areas first, because the regions overlap.
							if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
									&& ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
								// Top left corner: adjust both top and left sides
								cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (lastY - currentY));
//								viewfinderView.removeResultText();
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
								cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (lastY - currentY));
//								viewfinderView.removeResultText();
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
								cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY));
//								viewfinderView.removeResultText();
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
								cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY));
//								viewfinderView.removeResultText();
							} else if (((currentX >= rect.left - BUFFER && currentX <= rect.left
									+ BUFFER) || (lastX >= rect.left - BUFFER && lastX <= rect.left
									+ BUFFER))
									&& ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
								// Adjusting left side: event falls within
								// BUFFER pixels of left side, and between top
								// and bottom side limits
								cameraManager.adjustFramingRect(2 * (lastX - currentX), 0);
//								viewfinderView.removeResultText();
							} else if (((currentX >= rect.right - BUFFER && currentX <= rect.right
									+ BUFFER) || (lastX >= rect.right - BUFFER && lastX <= rect.right
									+ BUFFER))
									&& ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
								// Adjusting right side: event falls within
								// BUFFER pixels of right side, and between top
								// and bottom side limits
								cameraManager.adjustFramingRect(
										2 * (currentX - lastX), 0);
//								viewfinderView.removeResultText();
							} else if (((currentY <= rect.top + BUFFER && currentY >= rect.top
									- BUFFER) || (lastY <= rect.top + BUFFER && lastY >= rect.top
									- BUFFER))
									&& ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
								// Adjusting top side: event falls within BUFFER
								// pixels of top side, and between left and
								// right side limits
								cameraManager.adjustFramingRect(0,
										2 * (lastY - currentY));
//								viewfinderView.removeResultText();
							} else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom
									- BUFFER) || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom
									- BUFFER))
									&& ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
								// Adjusting bottom side: event falls within
								// BUFFER pixels of bottom side, and between
								// left and right side limits
								cameraManager.adjustFramingRect(0,
										2 * (currentY - lastY));
//								viewfinderView.removeResultText();
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
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.d(TAG,
						"Error creating media file, check storage permissions");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}

			mPreview.mCamera.release();
		}
	};

	public void takePicture(View view) {
		mPreview.mCamera.takePicture(null, null, mPicture);
	}

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create file for saving an image or video */
	private static File getOutputMediaFile(int type) {
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
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else /*
				 * if(type == MEDIA_TYPE_VIDEO) { mediaFile = new
				 * File(mediaStorageDir.getPath() + File.separator + "VID_"+
				 * timeStamp + ".mp4"); } else
				 */{
			return null;
		}

		return mediaFile;
	}
}
