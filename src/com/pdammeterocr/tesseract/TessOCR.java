package com.pdammeterocr.tesseract;

import java.io.File;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.pdammeterocr.CaptureActivity;
import com.pdammeterocr.R;

public class TessOCR extends AsyncTask<Object, String, Boolean>{
	private TessBaseAPI mTess;
	private ProgressDialog progressDialog;
	private CaptureActivity activity;
	
	/** Suffixes of required data files for Cube. */
	private static final String[] CUBE_DATA_FILES = { 
		".cube.bigrams",
		".cube.fold", 
		".cube.lm", 
		".cube.nn", 
		".cube.params", 
		".cube.size", // This file is not available for Hindi
		".cube.word-freq", 
		".tesseract_cube.nn", 
		".traineddata" };
	
	public static final String TESSERACT_PATH = "/tesseract/";
	private static final String TRAINING_PATH = TESSERACT_PATH + "tessdata/";
	public static final String LANGUAGE_CODE = "eng";
	
	/** Resource to use for data file downloads. */
	static final String DOWNLOAD_BASE = "http://tesseract-ocr.googlecode.com/files/";
	  
	/** Download filename for orientation and script detection (OSD) data. */
	static final String OSD_FILENAME = "tesseract-ocr-3.01.osd.tar";
	  
	/** Destination filename for orientation and script detection (OSD) data. */
	static final String OSD_FILENAME_BASE = "osd.traineddata";
	
	private Bitmap resultImage;
	private String resultText;
	private Boolean resultStatus;
	
	public TessOCR(ProgressDialog progressDialog, TessBaseAPI baseApi,
			CaptureActivity activity) {
		this.activity = activity;
		this.progressDialog = progressDialog;
		this.mTess = baseApi;
		resultStatus = false;
	}
	
	public Boolean checkTessTrainingData() {
		String dataPath = Environment.getExternalStorageDirectory() + TRAINING_PATH;
		File dir = new File(dataPath);
		if(!dir.exists())
		{
			dir.mkdirs();
			return false;
		}else{
			File[] listFile = dir.listFiles();
			Boolean check = false;
			for (String cube : CUBE_DATA_FILES) {
				check = false;
				for (File file : listFile) {
					if(file.getName().equals(LANGUAGE_CODE + cube))
					{
						check = true;
						break;
					}
				}
				
				if(!check){
					return false;
				}
			}
			return true;
		}
	}
	
	public String getOCRResult(Bitmap bitmap) {
		String result;
		try{
		mTess.setImage(ReadFile.readBitmap(bitmap));
		result = mTess.getUTF8Text();
		}catch(Exception e)
		{
			result = e.getMessage();
		}
		
		return result;
	}
	
	public void onDestroy() {
		if(mTess != null)
			mTess.end();
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		// TODO Auto-generated method stub
		
		publishProgress("Initialization OCR");
		mTess = new TessBaseAPI();
		if (!checkTessTrainingData()) {
			// download traning data
		}
		mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789");
		mTess.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!?@#$%&*()[]{}<>_-+=/.,:;'\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
		mTess.init(Environment.getExternalStorageDirectory() + TESSERACT_PATH,
				LANGUAGE_CODE);
		
		publishProgress("Recognize image");
		
		// long start = System.currentTimeMillis();
		try {
			resultImage = (Bitmap) params[0];
			this.mTess.setImage(ReadFile.readBitmap(resultImage));
			resultText = mTess.getUTF8Text();

			// Check for failure to recognize text
			if (resultText == null || resultText.equals("")) {
			    resultStatus = false;
				return false;
			}else{
				resultStatus = true;
			}
			// ocrResult.setCharacterBoundingBoxes(baseApi.getCharacters().getBoxRects());
		} catch (RuntimeException e) {
			Log.e("OcrRecognizeAsyncTask",
					"Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.");
			e.printStackTrace();
			try {
				this.mTess.clear();
			} catch (NullPointerException e1) {
				// Continue
			}
			return false;
		}
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
		activity.mFrame.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		try {
			progressDialog.dismiss();
			if(resultStatus == true)
			{
				LinearLayout result_view = (LinearLayout)activity.findViewById(R.id.result_view);
				result_view.setVisibility(View.VISIBLE);
				TextView resultTextView = (TextView)activity.findViewById(R.id.result_text_view);
				resultTextView.setText(resultText);
				ImageView image_view = (ImageView)activity.findViewById(R.id.image_view);
				image_view.setImageBitmap(resultImage);
				RelativeLayout button_done = (RelativeLayout)activity.findViewById(R.id.btndone_layout);
				button_done.setVisibility(View.VISIBLE);
				
				RelativeLayout button_layout = (RelativeLayout)activity.findViewById(R.id.button_layout);
				button_layout.setVisibility(View.INVISIBLE);
				activity.resultVisibility = true;
			}else{
				Toast toast = Toast.makeText(activity.getApplication(), "Failed", Toast.LENGTH_SHORT);
			    toast.show();
			}
		} catch (IllegalArgumentException e) {
			// Catch "View not attached to window manager" error, and continue
		}
	}
	
	@Override
	protected void onProgressUpdate(String... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		progressDialog.setMessage(values[0]);
	}
}
