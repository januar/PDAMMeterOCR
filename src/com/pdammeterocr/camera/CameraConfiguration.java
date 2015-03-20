package com.pdammeterocr.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;

public class CameraConfiguration {
	private static final String TAG = "CameraConfiguration";
	// This is bigger than the size of a small screen, which is still supported.
	// The routine
	// below will still select the default (presumably 320x240) size for these.
	// This prevents
	// accidental selection of very low resolution on some devices.
	private static final int MIN_PREVIEW_PIXELS = 470 * 320; // normal screen
	private static final int MAX_PREVIEW_PIXELS = 800 * 600; // more than
																// large/HD
																// scree

	public CameraConfiguration() {
		// TODO Auto-generated constructor stub
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

}
