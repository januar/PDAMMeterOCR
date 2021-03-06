package com.pdammeterocr.tesseract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.pdammeterocr.CaptureActivity;
import com.pdammeterocr.R;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Async task untuk proses pengenalan chractes.
 * Dimulai dari preproses sampai proses recognize
 * */
public final class OcrRecognizeAsyncTask extends AsyncTask<Object, String, Boolean> {
	private TessBaseAPI baseApi;
	private CaptureActivity activity;
	private ProgressDialog progressDialog;
	private byte[] data;
	private int width;
	private int height;
	private String path = null;
	private Boolean resultStatus;
	private String resultText;
	private Bitmap resultImage;

	public OcrRecognizeAsyncTask(TessBaseAPI baseApi, CaptureActivity activity,
			ProgressDialog progressDialog, byte[] data, int width, int heigth) {
		// TODO Auto-generated constructor stub
		this.baseApi = baseApi;
		this.activity = activity;
		this.progressDialog = progressDialog;
		this.data = data;
		this.width = width;
		this.height = heigth;
		this.resultStatus = false;
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		// TODO Auto-generated method stub

		try {
			// pre processing
			// mengcrop gambar dan di grayscale
			Rect rect = activity.cameraManager.getFramingRect();
			resultImage = renderCroppedGreyscaleBitmap(data, width, height, rect.top, rect.left, rect.width(), rect.height());
			
			// inisialisasi Mat object yang merupkana bitmap object di openCV
			Mat grayMeterMat = new Mat();
			Mat meterImageMat = new Mat();
			Mat destination = new Mat(grayMeterMat.rows(), grayMeterMat.cols(), grayMeterMat.type());
			Utils.bitmapToMat(resultImage, meterImageMat); // mengubah bitmap ke mat
			Imgproc.cvtColor(meterImageMat, grayMeterMat, Imgproc.COLOR_BGR2GRAY);
			
			// melakukan proses thresholding
			Imgproc.threshold(grayMeterMat, destination, 0, 255, Imgproc.THRESH_OTSU);
			Bitmap thresImage = resultImage.copy(Bitmap.Config.ARGB_8888, true);
			Utils.matToBitmap(destination, thresImage);
			Bitmap ocrimage = thresImage.copy(Config.ARGB_8888, true);
			
			// menyimpan file hasil crop
			File picture = CaptureActivity.getOutputMediaFile(1, false);
			FileOutputStream fos = new FileOutputStream(picture);
			resultImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.close();
			path = picture.getAbsolutePath();
			
			// menyimpan gambar hasil preprocessing
			FileOutputStream prefos = new FileOutputStream(CaptureActivity.getOutputMediaFile(1, true));
			ocrimage.compress(Bitmap.CompressFormat.JPEG, 100, prefos);
			prefos.close();
			// end pre processing
			
			// ocr process
			// set gambar yang akan di proses oleh tesseract OCR
			this.baseApi.setImage(ReadFile.readBitmap(ocrimage));
			resultText = baseApi.getUTF8Text(); // hasil disimpan dalam variabel resultTest

			// Check for failure to recognize text
			if (resultText == null || resultText.equals("")) {
			    resultStatus = false;
				return false;
			}else{
				resultStatus = true;
			}
		} catch (RuntimeException e) {
			Log.e("OcrRecognizeAsyncTask",
					"Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.");
			e.printStackTrace();
			try {
				this.baseApi.clear();
			} catch (NullPointerException e1) {
				// Continue
			}
			return false;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		try {
			progressDialog.dismiss();
			if(resultStatus == true)
			{
				// menampilkan hasil
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
				activity.imagePath = path;
				activity.resultVisibility = true;
			}else{
				activity.mFrame.setVisibility(View.VISIBLE);
				Toast toast = Toast.makeText(activity.getApplication(), "Failed", Toast.LENGTH_SHORT);
			    toast.show();
			}
		} catch (IllegalArgumentException e) {
			// Catch "View not attached to window manager" error, and continue
		}
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog.setTitle("Please wait");
		progressDialog.setMessage("Image Preprocessing");
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);
		progressDialog.show();
		activity.mFrame.setVisibility(View.INVISIBLE);
	}
	
	@Override
	protected void onProgressUpdate(String... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		progressDialog.setMessage(values[0]);
	}
	
	public Bitmap renderCroppedGreyscaleBitmap(byte[] yuvData, int dataWidth,
			int dataHeight, int top, int left, int width, int height) {
		int[] pixels = new int[width * height];
		byte[] yuv = yuvData;
		int inputOffset = top * dataWidth + left;

		for (int y = 0; y < height; y++) {
			int outputOffset = y * width;
			for (int x = 0; x < width; x++) {
				int grey = yuv[inputOffset + x] & 0xff;
				pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
			}
			inputOffset += dataWidth;
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
}
