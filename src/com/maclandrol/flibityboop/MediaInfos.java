package com.maclandrol.flibityboop;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.os.Parcelable;

public interface MediaInfos extends Parcelable{

	public String getTitle();
	public double getScore();
	public boolean isMovie();
	public boolean isShow();
	public API.MediaType getType();
	public int getID();
	public String getDate();
	public String getOriginalPosterURL();
	public HashMap<String, String > getAdditionalFeatures();
	public ArrayList<Critics> getCritics();
	public ArrayList<? extends MediaInfos> getSimilar();
	public String getPosterURL(int i);
	
}
