package com.pdammeterocr.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class History {
	private int id;
	private String meter_number;
	private String meter_result;
	private String date;
	private String image;
	
	public History() {
		// TODO Auto-generated constructor stub
	}
	
	public History(Result result)
	{
		this.id = result.getId();
		this.meter_number = result.getMeterNumber();
		this.meter_result = result.getMeterResult();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		try {
			this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sdf.parse(result.getDate()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.image = encodeTobase64(result.getImageByte());
	}
	
	public static String encodeTobase64(byte[] image)
	{
	    String imageEncoded = Base64.encodeToString(image, Base64.DEFAULT);
	    return imageEncoded;
	}
	
	public static Bitmap decodeBase64(String input) 
	{
	    byte[] decodedByte = Base64.decode(input, 0);
	    return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length); 
	}
	
	/*
	 * setter
	 * getter
	 * */
	public int getId(){
		return id;
	}
	public void setId(int _id){
		this.id= _id;
	}
	
	public String getMeterNumber(){
		return meter_number;
	}
	public void setMeterNumber(String meter_number){
		this.meter_number = meter_number;
	}
	
	public String getMeterResult(){
		return meter_result;
	}
	public void setMeterResult(String meter_result){
		this.meter_result = meter_result;
	}
	
	public String getDate(){
		return date;
	}
	public void setDate(String date){
		this.date = date;
	}
	
	public String getImage(){
		return this.image; // gambar dikembalikan dalam bentuk bitmap
	}
	
	public void setImage(byte[] image){
		this.image =  encodeTobase64(image);
	}

}
