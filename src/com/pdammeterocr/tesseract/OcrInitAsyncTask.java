package com.pdammeterocr.tesseract;

import java.io.File;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.pdammeterocr.CaptureActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;

public class OcrInitAsyncTask extends AsyncTask<String, String, Boolean> {

	private ProgressDialog progressDialog;
	private TessBaseAPI baseApi;
	private CaptureActivity activity;

	/** Suffixes of required data files for Cube. */
	private static final String[] CUBE_DATA_FILES = { ".cube.bigrams",
			".cube.fold", ".cube.lm", ".cube.nn", ".cube.params", ".cube.size",
			".cube.word-freq", ".tesseract_cube.nn", ".traineddata" };

	public static final String TESSERACT_PATH = "/tesseract/";
	private static final String TRAINING_PATH = TESSERACT_PATH + "tessdata/";
	public static final String LANGUAGE_CODE = "eng";

	/** Resource to use for data file downloads. */
	static final String DOWNLOAD_BASE = "http://tesseract-ocr.googlecode.com/files/";

	/** Download filename for orientation and script detection (OSD) data. */
	static final String OSD_FILENAME = "tesseract-ocr-3.01.osd.tar";

	/** Destination filename for orientation and script detection (OSD) data. */
	static final String OSD_FILENAME_BASE = "osd.traineddata";

	public OcrInitAsyncTask(ProgressDialog progressDialog, TessBaseAPI baseApi,
			CaptureActivity activity) {
		// TODO Auto-generated constructor stub
		this.progressDialog = progressDialog;
		this.baseApi = baseApi;
		this.activity = activity;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub
		baseApi = new TessBaseAPI();
		if (!checkTessTrainingData()) {
			// download traning data
			return false;
		}
		baseApi.init(Environment.getExternalStorageDirectory() + TESSERACT_PATH,LANGUAGE_CODE);
		return true;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog.setTitle("Please wait");
		progressDialog.setMessage("Checking for data installation...");
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);
		progressDialog.show();
		progressDialog.setMessage("Initialization OCR");
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		try {
			progressDialog.dismiss();
		} catch (IllegalArgumentException e) {
			// Catch "View not attached to window manager" error, and continue
		}
	}

	private Boolean checkTessTrainingData() {
		String dataPath = Environment.getExternalStorageDirectory()
				+ TRAINING_PATH;
		File dir = new File(dataPath);
		if (!dir.exists()) {
			dir.mkdirs();
			return false;
		} else {
			File[] listFile = dir.listFiles();
			Boolean check = false;
			for (String cube : CUBE_DATA_FILES) {
				check = false;
				for (File file : listFile) {
					if (file.getName().equals(LANGUAGE_CODE + cube)) {
						check = true;
						break;
					}
				}

				if (!check) {
					return false;
				}
			}
			return true;
		}
	}

}
