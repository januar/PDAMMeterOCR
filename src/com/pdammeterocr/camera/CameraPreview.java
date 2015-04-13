/**
 * 
 */
package com.pdammeterocr.camera;

import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/**
 * @author 7rait
 * 
 */
public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	public Camera mCamera;

	public static final String TAG = "PDAMMeterOCRCamera";
	private Point cameraResolution;
	public CameraConfiguration cameraConfig;
	private AutoFocusManager autofocusManager;

	/**
	 * @param context
	 */
	public CameraPreview(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 */
	public CameraPreview(Context context, Camera camera) {
		super(context);
		// TODO Auto-generated constructor stub
		initCamera(camera, context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void initCamera(Camera camera, Context context) {
		mCamera = camera;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		// We're landscape-only, and have apparently seen issues with display
		// thinking it's portrait
		// when waking from sleep. If it's not landscape, assume it's mistaken
		// and reverse them:
		if (width < height) {
			Log.i(TAG,
					"Display reports portrait orientation; assuming this is incorrect");
			int temp = width;
			width = height;
			height = temp;
		}

		Point screenResolution = new Point(width, height);
		Log.i(TAG, "Screen resolution: " + screenResolution);
		cameraResolution = CameraConfiguration.findBestPreviewSizeValue(
				parameters, screenResolution);
		Log.i(TAG, "Camera resolution: " + cameraResolution);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try {
			setDesiredCameraParameters(mCamera);
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
			autofocusManager = new AutoFocusManager(getContext(), mCamera);
		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mCamera.release();
	}

	void setDesiredCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();

		if (parameters == null) {
			Log.w(TAG,
					"Device error: no camera parameters are available. Proceeding without configuration.");
			return;
		}

		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		camera.setParameters(parameters);

		List<String> focusModes = parameters.getSupportedFocusModes();
		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			mCamera.setParameters(parameters);
		}
	}
	
	public synchronized void requestAutoFocus(long delay) {
		autofocusManager.start(delay);
	}
}
