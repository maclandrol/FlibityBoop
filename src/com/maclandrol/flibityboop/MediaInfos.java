package com.maclandrol.flibityboop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

public interface MediaInfos extends Parcelable, Serializable{
	
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
	
	public static final Parcelable.Creator<MediaInfos> CREATOR = new Creator<MediaInfos>(){

		@Override
		public MediaInfos createFromParcel(Parcel source) {
			int type= source.readInt();
			if(type==1)
					return new RTSearch(source);
			else if(type==2)
				return new TraktTVSearch(source);
			return null;
		}

		@Override
		public MediaInfos[] newArray(int size) {
			return new MediaInfos[size];
		}
		
		
	};
	}
