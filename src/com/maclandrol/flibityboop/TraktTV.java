package com.maclandrol.flibityboop;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.maclandrol.flibityboop.API.MediaType;

public class TraktTV extends API {
	public static final String traktkey = "a62503a2e4aa0735de62a46bad65148a",
			trakt_base = "http://api.trakt.tv/";

	public TraktTV() {
		this.key = traktkey;
		this.baseURL = trakt_base;

	}

	public ArrayList<TraktTVSearch> searchShow(String query) {
		return this.searchShow(query, -1);
	}

	public ArrayList<TraktTVSearch> searchShow(String query, int limit) {
		ArrayList<TraktTVSearch> result = new ArrayList<TraktTVSearch>();
		try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			erreur = e.getMessage();
			query = query.replace(" ", "%2b");
		} // this is important

		String url = this.baseURL + "search/shows.json/" + this.key + "?query="
				+ query;
		if (limit > 0)
			url += "&limit=" + limit;

		JSONArray j = this.getJSONArray(url);
		JSONObject show;
		if (j != null && j.length() > 0) {
			for (int i = 0; i < j.length(); i++) {
				show = j.optJSONObject(i);
				try {
					result.add(new TraktTVSearch(show, MediaType.TVShow));
				} catch (Exception e) {
					this.erreur = e.getMessage();
					e.printStackTrace();

				}
			}

		}
		return result;
	}

	public ArrayList<TraktTVSearch> getSimilarShow(int tvdb_id) {
		ArrayList<TraktTVSearch> result = new ArrayList<TraktTVSearch>();
		String url = this.baseURL + "show/related.json/" + this.key + "/"
				+ tvdb_id;
		JSONArray j = this.getJSONArray(url);
		JSONObject show;
		if (j != null && j.length() > 0) {
			for (int i = 0; i < j.length(); i++) {
				show = j.optJSONObject(i);
				try {
					result.add(new TraktTVSearch(show, MediaType.TVShow));
				} catch (Exception e) {
					this.erreur = e.getMessage();
					e.printStackTrace();
				}
			}
		}
		return result;

	}

	public ArrayList<Critics> getTVCritics(int tvdb_id) {
		ArrayList<Critics> critiques = new ArrayList<Critics>();
		String url = this.baseURL + "show/comments.json/" + this.key + "/"
				+ tvdb_id;
		JSONArray j = this.getJSONArray(url);
		JSONObject show;
		if (j != null && j.length() > 0) {
			for (int i = 0; i < j.length(); i++) {
				show = j.optJSONObject(i);
				try {
					critiques.add(new Critics(show.optJSONObject("user")
							.optString("username"), show.optString("text")));

				} catch (Exception e) {
					this.erreur = e.getMessage();
				}
			}
		}

		return critiques;
	}

}

class TraktTVSearch implements MediaInfos {

	String poster, title, type;
	int year, runtime, tvdb_id, voteCount;
	String imdb_id;
	double rating = -1.0;
	String air_day, air_time;
	String first_date;
	String genres, overview, network;
	private HashMap<String, String> addInfos = null;

	public TraktTVSearch(JSONObject js, MediaType type) throws JSONException {

		this.title = js.optString("title");
		this.year = js.optInt("year");
		this.type = "show";
		this.overview = js.optString("overview");
		this.runtime = js.optInt("runtime", -1);
		this.network = js.optString("network");
		this.air_day = js.optString("air_day");
		this.air_time = js.optString("air_time");
		this.imdb_id = js.optString("imdb_id");
		this.tvdb_id = js.optInt("tvdb_id", -1);
		JSONArray genre_list = js.optJSONArray("genres");
		String genre = "";
		int i;
		if (genre_list != null && genre_list.length() > 0) {
			for (i = 0; i < genre_list.length() - 1; i++) {
				genre += genre_list.optString(i, "") + "/";
			}
			genre += genre_list.optString(i);
		}
		this.genres = genre;
		this.poster = null;
		try {
			this.poster = js.optJSONObject("images").optString("poster");
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.first_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(js
				.optLong("first_aired") * 1000));
		try {
			this.rating = js.optJSONObject("ratings").optDouble("percentage",
					-1);
			this.voteCount = js.optJSONObject("ratings").optInt("votes", -1);
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		addInfos = new HashMap<String, String>();
		addInfos.put("overview", this.overview);
		addInfos.put("genres", this.genres);
		addInfos.put("runtime", String.valueOf(this.runtime));
		addInfos.put("imdb", this.imdb_id);
		addInfos.put("network", this.network);
		addInfos.put("homepage", js.optString("url"));
		addInfos.put("country", js.optString("country"));
		addInfos.put("fanart", js.optString("fanart"));
		addInfos.put("status", js.optBoolean("ended") ? "running" : "ended");
	}

	public TraktTVSearch(Parcel source) {
		
		title = source.readString();
		imdb_id = source.readString();
		type = source.readString();
		poster = source.readString();
		air_day = source.readString();
		overview = source.readString();
		air_time = source.readString();
		first_date = source.readString();
		genres = source.readString();
		network = source.readString();

		year = source.readInt();
		runtime = source.readInt();
		tvdb_id = source.readInt();
		voteCount = source.readInt();

		rating = source.readDouble();

		addInfos = source.readHashMap(null);
	}

	public String getNetwork() {
		return this.network;
	}

	public String get_IMDB() {
		return this.imdb_id;
	}

	public String getAirDay() {
		return this.air_day;
	}

	public String getAirTime() {
		return this.air_time;
	}

	public String[] getGenres() {
		return this.genres.split("\\");
	}

	public int getRuntime() {
		return this.runtime;
	}

	public String getOverview() {
		return this.overview;
	}

	public boolean isMovie() {
		return this.type.equalsIgnoreCase("movie");
	}

	public boolean isShow() {
		return this.type.equalsIgnoreCase("show");
	}

	public MediaType getType() {
		return this.isMovie() ? MediaType.Movies : MediaType.TVShow;
	}

	public String getReleaseDate() {
		return this.getDate();
	}

	public String getFirstAirDate() {
		return this.getDate();
	}

	public String getDate() {
		return this.first_date;
	}

	public double getScore() {
		return this.rating;
	}

	public String getTitle() {
		return this.title;
	}

	public int getVoteCount() {
		return this.voteCount;
	}

	public String getOriginalPosterURL() {
		return this.poster;
	}

	public String toString() {
		return "\nTitle : " + this.getTitle() + "\nID : " + this.getID()
				+ "\nType : " + this.getType() + "\nDate : " + this.getDate()
				+ "\nScore : " + this.getScore() + "\nPoster url : "
				+ this.getOriginalPosterURL();
	}

	public int getID() {
		return this.tvdb_id;
	}

	@Override
	public HashMap<String, String> getAdditionalFeatures() {
		return addInfos;
	}

	@Override
	public ArrayList<Critics> getCritics() {
		return new TraktTV().getTVCritics(this.getID());
	}

	public ArrayList<? extends MediaInfos> getSimilar() {
		return new TraktTV().getSimilarShow(this.getID());
	}

	public boolean equals(Object obj) {
		if (obj instanceof MediaInfos) {
			MediaInfos autres = (MediaInfos) obj;
			if (autres.getTitle().equalsIgnoreCase(this.getTitle())
					&& autres.getType() == this.getType())
				return true;
			else
				return false;
		}
		return false;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	public String getPosterURL(int i) {
		return poster;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {

		out.writeString(title);
		out.writeString(imdb_id);
		out.writeString(type);
		out.writeString(poster);
		out.writeString(air_day);
		out.writeString(overview);
		out.writeString(air_time);
		out.writeString(first_date);
		out.writeString(genres);
		out.writeString(network);

		out.writeInt(year);
		out.writeInt(runtime);
		out.writeInt(tvdb_id);
		out.writeInt(voteCount);

		out.writeDouble(rating);

		out.writeMap(addInfos);
	}
	
	public static final Parcelable.Creator<TraktTVSearch> CREATOR = new Creator<TraktTVSearch>(){

		@Override
		public TraktTVSearch createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new TraktTVSearch(source);
		}

		@Override
		public TraktTVSearch[] newArray(int size) {
			// TODO Auto-generated method stub
			return new TraktTVSearch[size];
		}
		
		
	};

}
