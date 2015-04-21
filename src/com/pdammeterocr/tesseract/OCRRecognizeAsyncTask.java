package com.pdammeterocr.tesseract;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.pdammeterocr.CaptureActivity;
import com.pdammeterocr.R;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OCRRecognizeAsyncTask extends AsyncTask<Object, String, Boolean> {
	private TessBaseAPI baseApi;
	private CaptureActivity activity;
	private ProgressDialog progressDialog;

	public OCRRecognizeAsyncTask(TessBaseAPI baseApi, CaptureActivity activity,
			ProgressDialog progressDialog) {
		// TODO Auto-generated constructor stub
		this.baseApi = baseApi;
		this.activity = activity;
		this.progressDialog = progressDialog;
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		// TODO Auto-generated method stub
		long start = System.currentTimeMillis();
		String textResult;

		try {
			Bitmap meterImage = (Bitmap) params[0];
			baseApi.setImage(ReadFile.readBitmap(meterImage));
			textResult = baseApi.getUTF8Text();

			// Check for failure to recognize text
			if (textResult == null || textResult.equals("")) {
				activity.mPreview.mCamera.startPreview();
				progressDialog.setMessage("Failed");
				return false;
			}else{
				LinearLayout result_view = (LinearLayout)activity.findViewById(R.id.result_view);
				result_view.setVisibility(0);
				TextView resultTextView = (TextView)activity.findViewById(R.id.result_text_view);
				resultTextView.setText(textResult);
				ImageView image_view = (ImageView)activity.findViewById(R.id.image_view);
				image_view.setImageBitmap(meterImage);
				activity.mPreview.mCamera.release();
			}
			// ocrResult.setCharacterBoundingBoxes(baseApi.getCharacters().getBoxRects());
		} catch (RuntimeException e) {
			Log.e("OcrRecognizeAsyncTask",
					"Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.");
			e.printStackTrace();
			try {
				baseApi.clear();
			} catch (NullPointerException e1) {
				// Continue
			}
			return false;
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		progressDialog.dismiss();
		if (baseApi != null) {
			baseApi.clear();
		}
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog.setTitle("Please wait");
		progressDialog.setMessage("Recognizing");
		progressDialog.show();
	}
}
