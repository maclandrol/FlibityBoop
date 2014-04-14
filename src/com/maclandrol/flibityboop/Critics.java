package com.maclandrol.flibityboop;

import org.json.JSONObject;

import android.net.Uri;
import android.webkit.URLUtil;

public class Critics {
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
		this(author, comment, null, date);
	}

	public Critics(String author, String comment) {
		this(author, comment, null, null);
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
}
