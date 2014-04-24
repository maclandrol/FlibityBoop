package com.maclandrol.flibityboop;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.maclandrol.flibityboop.API.MediaType;

public class RottenTomatoes extends API {

	public static final String rottenbase = "http://api.rottentomatoes.com/api/public/v1.0/",
			rottenkey = "?apikey=fb9vdz4avkk4g7puuapap4mf";
	// baseURL=http://api.rottentomatoes.com/api/public/v1.0/lists.json?apikey=fb9vdz4avkk4g7puuapap4mf
	public static final int DEFAULT_PAGE_LIMIT = 30, DEFAULT_MAX_PAGE = 10;

	public RottenTomatoes() {
		super(rottenbase, rottenkey);
	}

	public ArrayList<RTSearch> getRequestPerLink(String url, int page_limit,
			int maxPage) {

		ArrayList<RTSearch> sr = new ArrayList<RTSearch>();
		if (page_limit > 50 || page_limit < 1)
			page_limit = DEFAULT_PAGE_LIMIT;

		maxPage = maxPage < DEFAULT_MAX_PAGE ? maxPage : DEFAULT_MAX_PAGE;

		if (maxPage < 1)
			maxPage = 1;

		boolean maxPageReached = false;
		int page = 1, total_result = 1;
		JSONObject request;
		JSONArray movie_list;

		while (page <= maxPage && !maxPageReached) {

			try {

				request = this.getJSON(url + "&page_limit=" + page_limit
						+ "&page=" + page);
				total_result = request.optInt("total");
				movie_list = request.optJSONArray("movies");
				if (movie_list != null && movie_list.length() > 0) {
					for (int i = 0; i < movie_list.length(); i++) {
						sr.add(new RTSearch(movie_list.optJSONObject(i),
								MediaType.Movies));
					}

				}
			} catch (Exception e) {
				this.erreur = e.getMessage();
			}
			page++;
			if (Math.ceil(((double) total_result) / page_limit) == page)
				maxPageReached = true;

		}

		return sr;
	}

	public ArrayList<RTSearch> searchMovies(String query, int page_limit,
			int maxPage) {
		try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			erreur = e.getMessage();
			query = query.replace(" ", "%20");
		}
		String url = this.baseURL + "movies.json" + this.key + "&q=" + query;
		return this.getRequestPerLink(url, page_limit, maxPage);
	}

	public ArrayList<Critics> getRTCritics(int movieID, int limit) {
		ArrayList<Critics> critique = new ArrayList<Critics>();
		String url = this.baseURL + "movies/" + movieID + "/reviews.json"
				+ this.key;
		JSONObject request = this.getJSON(url);
		int total = request.optInt("total");
		JSONArray review_list = request.optJSONArray("reviews");
		for (int i = 0; i < limit && i < total; i++) {
			critique.add(new Critics(review_list.optJSONObject(i)));
		}

		return critique;
	}

	public ArrayList<RTSearch> getSimilarMovies(int movieID, int limit) {
		String url = this.baseURL + "movies/" + movieID + "/similar.json"
				+ this.key;
		if (limit > 0 && limit < 6)
			url += "&limit=" + limit;
		return this.getRequestPerLink(url, 1, 1);

	}

	public HashMap<String, String> getMovieInfos(int movieID) {
		String url = this.baseURL + "movies/" + movieID + ".json" + this.key;
		HashMap<String, String> infos = new HashMap<String, String>();
		try {
			JSONObject mediajson = this.getJSON(url);
			JSONArray genres = mediajson.optJSONArray("genres");
			String genre_list = "";
			JSONObject g;
			if (genres != null && genres.length() > 0) {
				int i = 0;
				for (i = 0; i < genres.length() - 1; i++) {
					g = genres.optJSONObject(i);
					if (g != null && g.has("name")) {
						genre_list += g.optString("name") + ", ";
					}

				}
				g = genres.optJSONObject(i);
				if (g != null && g.has("name")) {
					genre_list += g.optString("name");

				}
			}
			infos.put("genres", genre_list);
			infos.put("overview", mediajson.optString("synopsis", null));
			infos.put("studio", mediajson.optString("studio", null));
			JSONArray director = mediajson.optJSONArray("abridged_directors");
			String dir_list = "";
			if (director != null && director.length() > 0) {
				int i = 0;
				for (i = 0; i < director.length() - 1; i++) {
					g = director.optJSONObject(i);
					if (g != null && g.has("name")) {
						dir_list += g.optString("name") + ", ";
					}

				}
				g = director.optJSONObject(i);
				if (g != null && g.has("name")) {
					dir_list += g.optString("name");

				}
			}

			infos.put("directors", dir_list);

			JSONArray cast = mediajson.optJSONArray("abridged_cast");
			String cast_list = "";
			if (cast != null && cast.length() > 0) {
				int i = 0;
				for (i = 0; i < cast.length() - 1; i++) {
					g = cast.optJSONObject(i);
					if (g != null && g.has("name")) {
						cast_list += g.optString("name") + ", ";
					}

				}
				g = cast.optJSONObject(i);
				if (g != null && g.has("name")) {
					cast_list += g.optString("name");

				}
			}
			JSONObject link = mediajson.optJSONObject("links");
			String links = null;
			if (link != null && link.has("alternate")) {
				links = link.optString("alternate");
			}
			infos.put("actors", cast_list);
			infos.put("homepage", links);
			String imdb_id = null;
			JSONObject alt_ids = mediajson.optJSONObject("alternate_ids");
			if (alt_ids != null) {
				imdb_id = alt_ids.getString("imdb");
			}
			if (imdb_id != null)
				imdb_id = "tt" + imdb_id;
			infos.put("imdb", imdb_id);

		} catch (Exception e) {
			erreur = "Impossible to process request";
			e.printStackTrace();
		}

		return infos;
	}
}

class RTSearch implements MediaInfos {

	private static final long serialVersionUID = -4764592788822302046L;
	int id, years;
	String title, imdb_id, type, critic_consensus, release_date, runtime,
			freshness;
	int audience_score, critics_score;
	String poster_small, poster_original;
	private HashMap<String, String> addInfos = null;

	public RTSearch(JSONObject jsObj, MediaType type) {
		if (type != MediaType.Movies)
			this.type = "unknown";
		else
			this.type = "movie";

		this.id = jsObj.optInt("id");
		this.years = jsObj.optInt("year");
		this.runtime = jsObj.optString("runtime", null);
		this.audience_score = 0;
		this.critics_score = 0;
		this.freshness = null;
		JSONObject rating = jsObj.optJSONObject("ratings");
		if (rating != null) {
			this.audience_score = (int) rating.optDouble("audience_score");
			this.critics_score = (int) rating.optDouble("critics_score");
			this.freshness = rating.optString("critics_rating", null);
		}

		this.title = jsObj.optString("title", null);
		this.critic_consensus = jsObj.optString("critics_consensus", null);
		try {
			this.release_date = jsObj.getJSONObject("release_dates").getString(
					"theater");
		} catch (Exception e) {
			this.release_date = null;
		}
		try {
			this.poster_small = jsObj.getJSONObject("posters").getString(
					"profile");
		} catch (Exception e) {
			this.poster_small = null;
		}

		try {
			this.poster_original = jsObj.getJSONObject("posters").getString(
					"original");
		} catch (Exception e) {
			this.poster_original = null;
		}
		this.imdb_id = null;
		JSONObject alt_ids = jsObj.optJSONObject("alternate_ids");
		if (alt_ids != null) {
			this.imdb_id = alt_ids.optString("imdb", null);
		}
		if (this.imdb_id != null)
			this.imdb_id = "tt" + this.imdb_id;
	}

	public RTSearch(Parcel source) {
		source.readInt();
		title = source.readString();
		imdb_id = source.readString();
		type = source.readString();
		critic_consensus = source.readString();
		release_date = source.readString();
		runtime = source.readString();
		freshness = source.readString();
		poster_small = source.readString();
		poster_original = source.readString();

		id = source.readInt();
		years = source.readInt();

		audience_score = (int) source.readDouble();
		critics_score = (int) source.readDouble();

		Bundle bundle = source.readBundle();
		@SuppressWarnings("unchecked")
		HashMap<String, String> serializable = (HashMap<String, String>)bundle.getSerializable("infosMap");
		addInfos = serializable; 
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	public double getAudienceScore() {
		return this.audience_score;
	}

	public double getCriticScore() {
		return this.critics_score;
	}

	public int getScore() {
		return this.audience_score > 0 ? this.audience_score
				: (this.critics_score > 0 ? this.critics_score : 0);
	}

	@Override
	public boolean isMovie() {
		return this.type.equalsIgnoreCase("movie");
	}

	@Override
	public boolean isShow() {
		return this.type.equalsIgnoreCase("show");
	}

	public MediaType getType() {
		return this.isMovie() ? MediaType.Movies : MediaType.TVShow;
	}

	@Override
	public int getID() {
		return this.id;
	}

	public String getFirstAirDate() {
		return this.getDate();
	}

	public double getRuntime() {
		if (this.runtime != null)
			return Double.valueOf(this.runtime);
		return -1;
	}

	@Override
	public String getDate() {
		return this.release_date != null ? this.release_date
				: (this.years != 0 ? +years + "" : "N/A");
	}

	public String getReleaseDate() {
		return this.getDate();
	}

	@Override
	public String getOriginalPosterURL() {
		return this.poster_original;
	}

	public String getProfilePosterURL() {
		return this.poster_small;
	}

	public String getConsensusCritic() {
		return this.critic_consensus;
	}

	public boolean isFresh() {
		return this.freshness != null
				&& this.freshness.toLowerCase().indexOf("fresh") > 0;
	}

	public String toString() {
		return "\nTitle : " + this.getTitle() + "\nID : " + this.getID()
				+ "\nType : " + this.getType() + "\nDate : " + this.getDate()
				+ "\nScore : " + this.getScore() + "\nPoster url : "
				+ this.getOriginalPosterURL();
	}

	@Override
	public HashMap<String, String> getAdditionalFeatures() {

		if (addInfos == null)
			addInfos = new RottenTomatoes().getMovieInfos(this.getID());
		return addInfos;
	}

	public ArrayList<Critics> getCritics(int limit) {
		return new RottenTomatoes().getRTCritics(this.id, limit);
	}

	@Override
	public ArrayList<Critics> getCritics() {
		return this.getCritics(10);
	}

	@Override
	public ArrayList<? extends MediaInfos> getSimilar() {
		return new RottenTomatoes().getSimilarMovies(this.id, 5);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
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

	@Override
	public String getPosterURL(int i) {
		if (i == 0)
			return poster_small;

		else
			return poster_original;
	}

	@Override
	public int describeContents() {
		return 1;
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeInt(describeContents());
		out.writeString(title);
		out.writeString(imdb_id);
		out.writeString(type);
		out.writeString(critic_consensus);
		out.writeString(release_date);
		out.writeString(runtime);
		out.writeString(freshness);
		out.writeString(poster_small);
		out.writeString(poster_original);

		out.writeInt(id);
		out.writeInt(years);

		out.writeDouble(audience_score);
		out.writeDouble(critics_score);
		Bundle bundle = new Bundle();
		bundle.putSerializable("infosMap", addInfos);
		out.writeBundle(bundle);
	}

	public static final Parcelable.Creator<RTSearch> CREATOR = new Creator<RTSearch>() {

		@Override
		public RTSearch createFromParcel(Parcel source) {
			return new RTSearch(source);
		}

		@Override
		public RTSearch[] newArray(int size) {
			return new RTSearch[size];
		}

	};

}
