package com.pdammeterocr.camera;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.preference.PreferenceManager;
import android.util.Log;

public final class AutoFocusManager implements AutoFocusCallback {

	private static final String TAG = AutoFocusManager.class.getSimpleName();

	public static final String KEY_AUTO_FOCUS = "preferences_auto_focus";
	private static final long AUTO_FOCUS_INTERVAL_MS = 3500L;
	private static final Collection<String> FOCUS_MODES_CALLING_AF;
	static {
		FOCUS_MODES_CALLING_AF = new ArrayList<String>(2);
		FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
		FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
	}

	private boolean active;
	private boolean manual;
	private final boolean useAutoFocus;
	private final Camera camera;
	private final Timer timer;
	private TimerTask outstandingTask;

	public AutoFocusManager(Context context, Camera camera) {
		// TODO Auto-generated constructor stub
		this.camera = camera;
		timer = new Timer(true);
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String currentFocusMode = camera.getParameters().getFocusMode();
		useAutoFocus = sharedPrefs.getBoolean(KEY_AUTO_FOCUS, true)
				&& FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
		Log.i(TAG, "Current focus mode '" + currentFocusMode
				+ "'; use auto focus? " + useAutoFocus);
		manual = false;
		checkAndStart();
	}

	@Override
	public void onAutoFocus(boolean arg0, Camera arg1) {
		// TODO Auto-generated method stub
		if (active && !manual) {
			outstandingTask = new TimerTask() {
				@Override
				public void run() {
					checkAndStart();
				}
			};
			timer.schedule(outstandingTask, AUTO_FOCUS_INTERVAL_MS);
		}
		manual = false;
	}

	void checkAndStart() {
		if (useAutoFocus) {
			active = true;
			start();
		}
	}

	synchronized void start() {
		try {
			camera.autoFocus(this);
		} catch (RuntimeException re) {
			// Have heard RuntimeException reported in Android 4.0.x+; continue?
			Log.w(TAG, "Unexpected exception while focusing", re);
		}
	}

	/**
	 * Performs a manual auto-focus after the given delay.
	 * 
	 * @param delay
	 *            Time to wait before auto-focusing, in milliseconds
	 */
	synchronized void start(long delay) {
		outstandingTask = new TimerTask() {
			@Override
			public void run() {
				manual = true;
				start();
			}
		};
		timer.schedule(outstandingTask, delay);
	}

	synchronized void stop() {
		if (useAutoFocus) {
			camera.cancelAutoFocus();
		}
		if (outstandingTask != null) {
			outstandingTask.cancel();
			outstandingTask = null;
		}
		active = false;
		manual = false;
	}

}
