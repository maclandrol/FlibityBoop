/**
 * IFT2905 : Interface personne machine
 * Projet de session: FlibityBoop.
 * Team: Vincent CABELI, Henry LIM, Pamela MEHANNA, Emmanuel NOUTAHI, Olivier TASTET
 * @author Emmanuel Noutahi, Vincent Cabeli
 */

package com.maclandrol.flibityboop;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.maclandrol.flibityboop.API.MediaType;

/**
 * Classe TraktTV, gère les requêtes vers l'API de TraktTV.
 */
public class TraktTV extends API {
	public static final String traktkey = "a62503a2e4aa0735de62a46bad65148a",
			trakt_base = "http://api.trakt.tv/";

	public TraktTV() {
		this.key = traktkey;
		this.baseURL = trakt_base;

	}

	/*
	 * Recherche d'un show basé sur un query
	 */
	public ArrayList<TraktTVSearch> searchShow(String query) {
		return this.searchShow(query, -1);
	}

	/*
	 * Recherche d'un show basé sur un query
	 */
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

	/*
	 * Récuperer la liste de show similaire à un show particulier
	 */
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

	/*
	 * Récuperer la liste des critiques/commentaires sur un show
	 */
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
							.optString("username"), show.optString("text"), "https://trakt.tv/", null));

				} catch (Exception e) {
					this.erreur = e.getMessage();
				}
			}
		}

		return critiques;
	}

}

/**
 * Classe TraktTVSearch, implémentant l'interface MediaInfos
 * Chaque instance correspond à un résultat de recherche d'un TV show sur TraktTV
 */
class TraktTVSearch implements MediaInfos {

	private static final long serialVersionUID = -9203729736235978006L;
	
	String poster, title, type;
	int year, runtime, tvdb_id, voteCount;
	String imdb_id;
	double rating = -1.0;
	String air_day, air_time;
	String first_date;
	boolean ended;
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
		addInfos.put("status", js.optBoolean("ended") ? "ended":"running");
		ended = js.optBoolean("ended");
	}

	@SuppressWarnings("unchecked")
	public TraktTVSearch(Parcel source) {
		source.readInt();
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
		ended = source.readByte() != 0; 

		Bundle bundle = source.readBundle();
		HashMap<String, String> serializable = (HashMap<String, String>)bundle.getSerializable("infosMap");
		this.addInfos = serializable; 
		}

	
	public TraktTVSearch(Parcel source, boolean b) {
		if(b) source.readInt();
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
		ended = source.readByte() != 0; 
		Bundle bundle = source.readBundle();
		HashMap<String, String> serializable = (HashMap<String, String>)bundle.getSerializable("infosMap");
		this.addInfos = serializable; 
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

	public int getScore() {
		return (int) this.rating;
	}

	public String getTitle() {
		return this.title;
	}
	
	public String getDetailedTitle() {

		return (getTitle() + " (" + year + ")");
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
		return 2;
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {

		out.writeInt(describeContents());
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
		out.writeByte((byte) (ended ? 1 : 0));
		Bundle bundle = new Bundle();
		bundle.putSerializable("infosMap", addInfos);
		out.writeBundle(bundle);
	}
	
	public static final Parcelable.Creator<TraktTVSearch> CREATOR = new Creator<TraktTVSearch>(){

		@Override
		public TraktTVSearch createFromParcel(Parcel source) {
			
			return new TraktTVSearch(source);
		}

		@Override
		public TraktTVSearch[] newArray(int size) {

			return new TraktTVSearch[size];
		}
		
		
	};
	
	
	/*
	 * Récupérer le temps avant la prochaine diffusion
	 */
	public String getTimeUntilNextAirTime() {
	        
	        int daysToGo, hoursToGo, minutesToGo;
	        String result="";
	        GregorianCalendar today = new GregorianCalendar();
	        int todayDayOfWeek = today.get(GregorianCalendar.DAY_OF_WEEK);
	        int todayHour = today.get(GregorianCalendar.HOUR_OF_DAY);
	        int todayMinute = today.get(GregorianCalendar.MINUTE);
	       
	        int nextDayOfWeek;
	        int nextHour;
	        int nextMinute;

	        if (air_day.equals("Sunday"))
	                nextDayOfWeek = 1;
	        else if (air_day.equals("Monday"))
	                nextDayOfWeek = 2;
	        else if (air_day.equals("Tuesday"))
	                nextDayOfWeek = 3;
	        else if (air_day.equals("Wednesday"))
	                nextDayOfWeek = 4;
	        else if (air_day.equals("Thursday"))
	                nextDayOfWeek = 5;
	        else if (air_day.equals("Friday"))
	                nextDayOfWeek = 6;
	        else if (air_day.equals("Saturday"))
	                nextDayOfWeek = 7;
	        else
	                nextDayOfWeek = 0;
	        if(this.ended || nextDayOfWeek==0)
	             return result;
	       
	        int i = air_time.indexOf(':');
	        nextHour = Integer.parseInt(air_time.substring(0, i)) + (air_time.contains("pm") ? 12 : 0);
	        nextMinute = Integer.parseInt(air_time.substring(i+1, i+3));
	       
	        if (nextHour > todayHour)
	                hoursToGo = nextHour - todayHour - 1;
	        else
	                hoursToGo = nextHour +23 - todayHour;
	       
	        if (nextMinute >  todayMinute)
	                minutesToGo = nextMinute - todayMinute -1;
	        else
	                minutesToGo = nextMinute +60 - todayMinute;

	        if(nextDayOfWeek == todayDayOfWeek  && (nextHour < todayHour || (nextHour > todayHour
					&& nextMinute > todayMinute))){
	            daysToGo = nextDayOfWeek+7 - todayDayOfWeek;
	        }
	        else if (nextDayOfWeek < todayDayOfWeek)
	            daysToGo = nextDayOfWeek +7- todayDayOfWeek;
	        else
	            daysToGo = nextDayOfWeek - todayDayOfWeek;
	   
	       
	        result = minutesToGo+" min";
	        if(hoursToGo>0)
	        	result = hoursToGo+" h "+result;
	        
	        if(daysToGo>0)
	        	result= daysToGo+ " day "+result;
	        return "in "+result;
	  	}
	    
	  
	/*
	 * Récuperer l'heure de diffusion
	 */
		public int getHours() {
			int i = this.getAirTime().indexOf(':');
			if(i>0)
				return Integer.parseInt(air_time.substring(0, i))+ (air_time.contains("pm") ? 12 : 0);
			return -1;
		}


		/*
		 * Récuperer les minutes de l'heure de diffusion
		 */
		public int getMinutes() {
			int i = this.getAirTime().indexOf(':');
			if (i > 0)
				return Integer.parseInt(air_time.substring(i + 1, i + 3));
			return -1;
		}
		
		/*
		 * Récuperer la durée de diffusion
		 */
		public int getDuration(){
			return this.runtime;
		}
		
		
		/*
		 * Récuperer le temps en long milliseconde avant la prochaine diffusion
		 */
		public long getTimeToGoMillis() {
			int nextDayOfWeek = 0, daysToGo, nextMinute= getMinutes(), nextHour=getHours(), hoursToGo, minutesToGo;
	        GregorianCalendar today = new GregorianCalendar();
	        int todayHour = today.get(GregorianCalendar.HOUR_OF_DAY);
	        int todayMinute = today.get(GregorianCalendar.MINUTE);
	        int todayDayOfWeek = today.get(GregorianCalendar.DAY_OF_WEEK);

			if (air_day.equals("Sunday"))
				nextDayOfWeek = 1;
			else if (air_day.equals("Monday"))
				nextDayOfWeek = 2;
			else if (air_day.equals("Tuesday"))
				nextDayOfWeek = 3;
			else if (air_day.equals("Wednesday"))
				nextDayOfWeek = 4;
			else if (air_day.equals("Thursday"))
				nextDayOfWeek = 5;
			else if (air_day.equals("Friday"))
				nextDayOfWeek = 6;
			else if (air_day.equals("Saturday"))
				nextDayOfWeek = 7;

			if (nextHour > todayHour)
				hoursToGo = nextHour - todayHour - 1;
			else
				hoursToGo = nextHour + 23 - todayHour;

			if (nextMinute > todayMinute)
				minutesToGo = nextMinute - todayMinute - 1;
			else
				minutesToGo = nextMinute + 60 - todayMinute;

			if (nextDayOfWeek == todayDayOfWeek
					&& (nextHour < todayHour || (nextHour > todayHour && nextMinute > todayMinute))) {
				daysToGo = nextDayOfWeek + 6 - todayDayOfWeek;
			} else if (nextDayOfWeek < todayDayOfWeek)
				daysToGo = nextDayOfWeek + 7 - todayDayOfWeek;
			else
				daysToGo = nextDayOfWeek - todayDayOfWeek;

			today.add(GregorianCalendar.DAY_OF_YEAR,daysToGo);
	        today.add(GregorianCalendar.HOUR,hoursToGo);
	        today.add(GregorianCalendar.MINUTE,minutesToGo);
				System.err.println(today.getTimeInMillis());
	        return today.getTimeInMillis();
		}
	}
