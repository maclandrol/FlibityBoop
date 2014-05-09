/**
 * IFT2905 : Interface personne machine
 * Projet de session: FlibityBoop.
 * Team: Vincent CABELI, Henry LIM, Pamela MEHANNA, Emmanuel NOUTAHI, Olivier TASTET
 * @author Emmanuel Noutahi, Vincent Cabeli
 */
package com.maclandrol.flibityboop;

import java.io.Serializable;

import org.json.JSONObject;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.URLUtil;

/**
 * Classe Critics, contient les commentaires et les critiques sur un média ainsi que les méthodes
 * pour avoir accès aux informations relatives à ces critiques
 */
public class Critics implements Parcelable, Serializable {

	private static final long serialVersionUID = -4069392063214205777L;
	String author, comment, url, date;

	public Critics(String author, String comment, String url, String date) {

		this.author = author;
		this.comment = comment;
		this.url = url;
		this.date = date;
	}
	

	public Critics(JSONObject c) {
		// setting author
		String author = null, date = null, url = null, comment = null;
		if (c != null) {
			if (c.has("author"))
				author = c.optString("author");
			else if (c.has("critic"))
				author = c.optString("critic");

			// setting quote
			if (c.has("content"))
				comment = c.optString("content");
			else if (c.has("quote"))
				comment = c.optString("quote");

			// setting url
			if (c.has("url"))
				url = c.optString("url");
			else if (c.has("links"))
				url = c.optJSONObject("links").optString("review");

			// setting date
			if (c.has("date"))
				date = c.optString("date");
		}

		this.author = author;
		this.comment = comment;
		this.date = date;
		this.url = url;
	}

	public Critics(String author, String comment, String date) {
		this(author, comment, null , date);
	}

	public Critics(String author, String comment) {
		this(author, comment, null, null);
	}

	public Critics(Parcel source) {
		this.author= source.readString();
		this.comment = source.readString();
		this.url = source.readString();
		this.date = source.readString();
	}


	public boolean isPrintable() {
		return this.author != null && this.comment != null;
	}

	public boolean hasDate() {
		return this.date != null;
	}

	public boolean hasURL() {
		return  URLUtil.isValidUrl(url);
	}

	public Uri getURL(){
		return Uri.parse(this.url);
	}
	
	public String getDomain(){
		try {
			Uri uri = Uri.parse(url);
			String domain = uri.getHost();
			return domain.startsWith("www.") ? domain.substring(4) : domain;
		} catch (Exception e) {
			return null;
		}
	}

	public String getAuthor() {
		return author;
	}

	public String getComment() {
		return comment;
	}
	
	public String toString(){
		if(this.isPrintable())
			return "\nAuthor : "+this.author +"\nComment : "+this.comment;
		return "";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(author);
		dest.writeString(comment);
		dest.writeString(url);
		dest.writeString(date);
	}
	
	public static final Parcelable.Creator<Critics> CREATOR = new Creator<Critics>(){

		@Override
		public Critics createFromParcel(Parcel source) {
			return new Critics(source);
		}

		@Override
		public Critics[] newArray(int size) {
			return new Critics[size];
		}
		
		
	};
}
