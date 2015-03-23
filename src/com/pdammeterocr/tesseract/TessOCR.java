package com.pdammeterocr.tesseract;

import java.io.File;
import java.util.Arrays;

import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TessOCR {
	private TessBaseAPI mTess;
	
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
	
	public TessOCR() {
		// TODO Auto-generated constructor stub
		mTess = new TessBaseAPI();
		if (!checkTessTrainingData()) {
			//download traning data
		}
		mTess.init(TESSERACT_PATH, LANGUAGE_CODE);
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
					if(file.getName() == LANGUAGE_CODE + cube)
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
	
	
	
	public void onDestroy() {
		if(mTess != null)
			mTess.end();
	}

}
