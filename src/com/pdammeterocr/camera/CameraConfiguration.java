package com.pdammeterocr.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class CameraConfiguration {
	private static final String TAG = "CameraConfiguration";
	// This is bigger than the size of a small screen, which is still supported.
	// The routine
	// below will still select the default (presumably 320x240) size for these.
	// This prevents
	// accidental selection of very low resolution on some devices.
	private static final int MIN_PREVIEW_PIXELS = 470 * 320; // normal screen
	private static final int MAX_PREVIEW_PIXELS = 800 * 600; // more than large/HD scree
	private static final int MIN_FRAME_WIDTH = 50; // originally 240
	private static final int MIN_FRAME_HEIGHT = 20; // originally 240
	private static final int MAX_FRAME_WIDTH = 800; // originally 480
	private static final int MAX_FRAME_HEIGHT = 600; // originally 360
	
	private int requestedFramingRectWidth;
	private int requestedFramingRectHeight;
	private Rect framingRectInPreview;
	
	private Rect framingRect;
	private Camera camera;
	private Context context;
	private boolean initialized;
	
	public CameraConfiguration(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}
	
	public void setCamera(Camera camera) {
		this.camera = camera;
		initialized = true;
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	public static Point findBestPreviewSizeValue(Camera.Parameters parameters,
			Point screenResolution) {

		// Sort by size, descending
		List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(
				parameters.getSupportedPreviewSizes());
		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size a, Camera.Size b) {
				int aPixels = a.height * a.width;
				int bPixels = b.height * b.width;
				if (bPixels < aPixels) {
					return -1;
				}
				if (bPixels > aPixels) {
					return 1;
				}
				return 0;
			}
		});

		if (Log.isLoggable(TAG, Log.INFO)) {
			StringBuilder previewSizesString = new StringBuilder();
			for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
				previewSizesString.append(supportedPreviewSize.width)
						.append('x').append(supportedPreviewSize.height)
						.append(' ');
			}
			Log.i(TAG, "Supported preview sizes: " + previewSizesString);
		}

		Point bestSize = null;
		float screenAspectRatio = (float) screenResolution.x
				/ (float) screenResolution.y;

		float diff = Float.POSITIVE_INFINITY;
		for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
			int realWidth = supportedPreviewSize.width;
			int realHeight = supportedPreviewSize.height;
			int pixels = realWidth * realHeight;
			if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
				continue;
			}
			boolean isCandidatePortrait = realWidth < realHeight;
			int maybeFlippedWidth = isCandidatePortrait ? realHeight
					: realWidth;
			int maybeFlippedHeight = isCandidatePortrait ? realWidth
					: realHeight;
			if (maybeFlippedWidth == screenResolution.x
					&& maybeFlippedHeight == screenResolution.y) {
				Point exactPoint = new Point(realWidth, realHeight);
				Log.i(TAG, "Found preview size exactly matching screen size: "
						+ exactPoint);
				return exactPoint;
			}
			float aspectRatio = (float) maybeFlippedWidth
					/ (float) maybeFlippedHeight;
			float newDiff = Math.abs(aspectRatio - screenAspectRatio);
			if (newDiff < diff) {
				bestSize = new Point(realWidth, realHeight);
				diff = newDiff;
			}
		}

		if (bestSize == null) {
			Camera.Size defaultSize = parameters.getPreviewSize();
			bestSize = new Point(defaultSize.width, defaultSize.height);
			Log.i(TAG, "No suitable preview sizes, using default: " + bestSize);
		}

		Log.i(TAG, "Found best approximate preview size: " + bestSize);
		return bestSize;
	}
	
	public Point getScreenResolution() {
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        // We're landscape-only, and have apparently seen issues with display thinking it's portrait 
        // when waking from sleep. If it's not landscape, assume it's mistaken and reverse them:
        if (width < height) {
          Log.i(TAG, "Display reports portrait orientation; assuming this is incorrect");
          int temp = width;
          width = height;
          height = temp;
        }
        
        Point screenResolution = new Point(width, height);
        Log.d("log", "Resolution width: " + width + " height: " + height);
        return screenResolution;
	}
	
	/**
	 * Calculates the framing rect which the UI should draw to show the user
	 * where to place the barcode. This target helps with alignment as well as
	 * forces the user to hold the device far enough away to ensure the image
	 * will be in focus.
	 * 
	 * @return The rectangle to draw on screen in window coordinates.
	 */
	public synchronized Rect getFramingRect() {
		if (framingRect == null) {
			if (camera == null) {
				return null;
			}
			Point screenResolution = getScreenResolution();
			if (screenResolution == null) {
				// Called early, before init even finished
				return null;
			}
			int width = screenResolution.x * 3 / 5;
			if (width < MIN_FRAME_WIDTH) {
				width = MIN_FRAME_WIDTH;
			} else if (width > MAX_FRAME_WIDTH) {
				width = MAX_FRAME_WIDTH;
			}
			int height = screenResolution.y * 1 / 5;
			if (height < MIN_FRAME_HEIGHT) {
				height = MIN_FRAME_HEIGHT;
			} else if (height > MAX_FRAME_HEIGHT) {

				height = MAX_FRAME_HEIGHT;
			}
			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height) / 2;
			framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
					topOffset + height);
		}
		return framingRect;
	}
	
	/**
	 * Changes the size of the framing rect.
	 * 
	 * @param deltaWidth Number of pixels to adjust the width
	 * @param deltaHeight Number of pixels to adjust the height
	 */
	public synchronized void adjustFramingRect(int deltaWidth, int deltaHeight) {
		if (initialized) {
			Point screenResolution = getScreenResolution();
			Log.d("adjustFramingRect", "deltaWidth : " + deltaWidth + " deltaHeight: " + deltaHeight);

			// Set maximum and minimum sizes
			if ((framingRect.width() + deltaWidth > screenResolution.x - 4)
					|| (framingRect.width() + deltaWidth < 50)) {
				deltaWidth = 0;
			}
			if ((framingRect.height() + deltaHeight > screenResolution.y - 4)
					|| (framingRect.height() + deltaHeight < 50)) {
				deltaHeight = 0;
			}
			Log.d("adjustFramingRect", "deltaWidth : " + deltaWidth + " deltaHeight: " + deltaHeight);

			int newWidth = framingRect.width() + deltaWidth;
			int newHeight = framingRect.height() + deltaHeight;
			int leftOffset = (screenResolution.x - newWidth) / 2;
			int topOffset = (screenResolution.y - newHeight) / 2;
			Log.d("adjustFramingRect", "leftOffset: " + leftOffset + " topOffset: " + topOffset + " newWidth: " + newWidth);
			framingRect = new Rect(leftOffset, topOffset, + newWidth, topOffset + newHeight);
			framingRectInPreview = null;
		} else {
			requestedFramingRectWidth = deltaWidth;
			requestedFramingRectHeight = deltaHeight;
		}
	}
	
	/**
	 * Like {@link #getFramingRect} but coordinates are in terms of the preview
	 * frame, not UI / screen.
	 */
	public synchronized Rect getFramingRectInPreview() {
		if (framingRectInPreview == null) {
			Rect rect = new Rect(getFramingRect());
			Point screenResolution = getScreenResolution();
			Point cameraResolution = findBestPreviewSizeValue(camera.getParameters(), screenResolution);
			if (cameraResolution == null || screenResolution == null) {
				// Called early, before init even finished
				return null;
			}
			rect.left = rect.left * cameraResolution.x / screenResolution.x;
			rect.right = rect.right * cameraResolution.x / screenResolution.x;
			rect.top = rect.top * cameraResolution.y / screenResolution.y;
			rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
			framingRectInPreview = rect;
		}
		return framingRectInPreview;
	}
}
