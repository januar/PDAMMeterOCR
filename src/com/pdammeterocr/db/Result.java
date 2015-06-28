package com.pdammeterocr.db;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

/*
 * Entity object dari table dalam SQL Lite yang dibuat sebelumnya*/
public class Result {
	private int id;
	private String meter_number;
	private String meter_result;
	private String date;
	private byte[] image;
	private Boolean selected;
	private Boolean is_sent;

	public Result() {
		// TODO Auto-generated constructor stub
	}
	
	// constructor
	public Result(int id, String meter_number, String meter_result, String date, byte[] image) {
		this.id = id;
		this.meter_number = meter_number;
		this.meter_result = meter_result;
		this.date = date;
		this.image = image;
	}
	
	// constructor
	public Result(int id, String meter_number, String meter_result, String date, Bitmap image) {
		this(id, meter_number, meter_result, date, getBitmapAsByteArray(image));
	}
	
	// constructor
	@SuppressLint("SimpleDateFormat") 
	public Result(String meter_number, String meter_result, Bitmap image){
		this(-1, meter_number, meter_result, "", getBitmapAsByteArray(image));
		date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
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
	
	public Bitmap getImage(){
		return BitmapFactory.decodeByteArray(image, 0, image.length); // gambar dikembalikan dalam bentuk bitmap
	}
	public byte[] getImageByte(){
		return image;
	}
	public void setImage(Bitmap image){
		this.image =  getBitmapAsByteArray(image);
	}
	
	public static byte[] getBitmapAsByteArray(Bitmap bitmap) { // convert bitmap menjadi byte array
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    bitmap.compress(CompressFormat.PNG, 0, outputStream);       
	    return outputStream.toByteArray();
	}
	
	public Boolean isSelected() {
		return this.selected;
	}
	
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
	
	public Boolean isSent(){
		return this.is_sent;
	}
	
	public void setSent(Boolean sent)
	{
		this.is_sent = sent;
	}

}
