package com.maclandrol.flibityboop;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.maclandrol.flibityboop.API.MediaType;

public class TheMovieDB extends API {

	public static final String tmdbKey = "?api_key=ecda05db82a153f8f3a1d1f0eecb1c00",
			tmdbAPI = "https://api.themoviedb.org/3/";
	public static final String img_URL = "http://image.tmdb.org/t/p/";

	// format possible des posters
	static int[] poster_size = { 92, 154, 185, 342, 500, 780 };

	// liste de tous les genre possible. Mieux en durs (encore mieux dans une
	// base de donnée)
	private static HashMap<String, Integer> genres = new HashMap<String, Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("action", 28);
			put("adventure", 12);
			put("animation", 16);
			put("comedy", 35);
			put("crime", 80);
			put("disaster", 105);
			put("documentary", 99);
			put("drama", 18);
			put("eastern", 82);
			put("erotic", 2916);
			put("family", 10751);
			put("fan film", 10750);
			put("fantasy", 14);
			put("film noir", 10753);
			put("fiction", 878);
			put("foreign", 10769);
			put("history", 36);
			put("holiday", 10595);
			put("horror", 27);
			put("indie", 10756);
			put("music", 10402);
			put("musical", 22);
			put("mystery", 9648);
			put("neo-noir", 10754);
			put("road movie", 1115);
			put("romance", 10749);
			put("science fiction", 878);
			put("short", 10755);
			put("sport", 9805);
			put("sporting event", 10758);
			put("sport film", 10757);
			put("suspense", 10748);
			put("tv movie", 10770);
			put("thriller", 53);
			put("war", 10752);
			put("western", 37);

		}
	};

	// default and uniq contructor
	public TheMovieDB() {
		super(tmdbAPI, tmdbKey);
	}

	// Methode privée pour decoder un json de themoviedb
	private ArrayList<TMDBSearch> decodeJSONArray(JSONArray result,
			MediaType type) {
		ArrayList<TMDBSearch> sr = new ArrayList<TMDBSearch>();
		for (int i = 0; i < result.length(); i++) {
			try {
				sr.add(new TMDBSearch(result.getJSONObject(i), type));
			} catch (JSONException e) {
				this.erreur = e.getMessage();
			}

		}
		return sr;
	}

	// Trouver un media en fonction de son idée: confert API : /find/{id}
	public ArrayList<TMDBSearch> findMedia(String id, String source) {
		if (source == null)
			source = "imdb_id";
		String url = this.baseURL + "find/" + id + this.key
				+ "&external_source=" + source;
		ArrayList<TMDBSearch> sr = new ArrayList<TMDBSearch>();
		JSONArray movie_result, tv_result;
		try {
			JSONObject jo = this.getJSON(url);
			movie_result = jo.getJSONArray("movie_results");
			sr = decodeJSONArray(movie_result, MediaType.Movies);
			tv_result = jo.getJSONArray("tv_results");
			sr.addAll(decodeJSONArray(tv_result, MediaType.TVShow));

		} catch (JSONException e) {
			e.printStackTrace();
			this.erreur = e.getMessage();
		}

		return sr;
	}

	private ArrayList<TMDBSearch> getRequestPerLink(String url, MediaType type,
			int maxPage) {

		ArrayList<TMDBSearch> sr = new ArrayList<TMDBSearch>();
		int page = 1;
		long total_page = 1;
		boolean maxPageReached = false;
		JSONObject r;
		JSONArray result;

		if (type != MediaType.Any) {

			if (maxPage <= 0)
				maxPage = 1;

			while (page <= maxPage && !maxPageReached) {
				try {
					r = this.getJSON(url + "&page=" + page);
					result = r.getJSONArray("results");
					total_page = r.optLong("total_pages", 1);
					sr.addAll(decodeJSONArray(result, type));

				} catch (Exception e) {
					this.erreur = e.getMessage();
				}
				page++;
				if (page == total_page) {
					maxPageReached = true;
				}
			}
		}

		return sr;

	}

	// Discover media based on the minVote, the min vote count. Je supporte TV,
	// MOVIE et ANY dans la même fonction
	// avec MediaType type. int maxPage permet de specifier le nombre max de
	// page à visiter dans la recherhce
	// l'API est mal faite et retourne toujours la premiere page (soit au plus
	// 10 resultat). Je fais des requetes supp pour completer les recherches
	// This can be greedy in time
	public ArrayList<TMDBSearch> discoverMedia(MediaType type, double minVote,
			int minCount, int maxPage) {

		ArrayList<TMDBSearch> sr = new ArrayList<TMDBSearch>();

		if (minCount <= 0)
			minCount = 1;

		if (type != MediaType.Any) {
			String url = this.baseURL + "discover/" + type.toString()
					+ this.key + "&vote_average.gte=" + minVote
					+ "&vote_count.gte=" + minCount;
			sr = getRequestPerLink(url, type, maxPage);
		} else {
			sr.addAll(discoverMedia(MediaType.Movies, minVote, minCount,
					maxPage));
			sr.addAll(discoverMedia(MediaType.TVShow, minVote, minCount,
					maxPage));
		}
		return sr;

	}

	// GetPopularMedia
	public ArrayList<TMDBSearch> getPopularMedia(MediaType type, int maxPage) {

		ArrayList<TMDBSearch> sr = new ArrayList<TMDBSearch>();
		if (type != MediaType.Any) {
			String url = this.baseURL + type.toString() + "/popular" + this.key;
			sr = getRequestPerLink(url, type, maxPage);
		} else {
			sr.addAll(getPopularMedia(MediaType.TVShow, maxPage));
			sr.addAll(getPopularMedia(MediaType.Movies, maxPage));
		}

		return sr;
	}

	// GetTopRated
	public ArrayList<TMDBSearch> getTopRatedMedia(MediaType type, int maxPage) {

		ArrayList<TMDBSearch> sr = new ArrayList<TMDBSearch>();
		if (type != MediaType.Any) {
			String url = this.baseURL + type.toString() + "/top_rated"
					+ this.key;
			sr = getRequestPerLink(url, type, maxPage);
		} else {
			sr.addAll(getTopRatedMedia(MediaType.TVShow, maxPage));
			sr.addAll(getTopRatedMedia(MediaType.Movies, maxPage));
		}
		return sr;

	}

	// Get Movie in theather
	public ArrayList<TMDBSearch> getInTheaterMovies(int maxPage) {

		String url = this.baseURL + "movie/now_playing" + this.key;
		return getRequestPerLink(url, MediaType.Movies, maxPage);

	}

	// Get TV on air
	public ArrayList<TMDBSearch> getOnAirTV(int maxPage) {

		String url = this.baseURL + "tv/on_the_air" + this.key;
		return getRequestPerLink(url, MediaType.TVShow, maxPage);

	}

	// getAiringToday TV
	public ArrayList<TMDBSearch> getAiringToday(int maxPage) {

		String url = this.baseURL + "tv/airing_today" + this.key;
		return getRequestPerLink(url, MediaType.TVShow, maxPage);

	}

	// getUPcomings movies
	public ArrayList<TMDBSearch> getUpcomingMovies(int maxPage) {

		String url = this.baseURL + "movie/upcoming" + this.key;
		return getRequestPerLink(url, MediaType.Movies, maxPage);

	}

	// Trouver des films par genres, case insensitif pour le genre et un boolean
	// pour specifier si tous les films sont a mettre en cache?
	// maxPage a la meme role que precedemment expliqué
	public ArrayList<TMDBSearch> getMoviesByGenres(String genre,
			boolean include_all_movies, int maxPage) {
		ArrayList<TMDBSearch> movies = new ArrayList<TMDBSearch>();

		try {
			int id = genres.get(genre.toLowerCase()).intValue();
			movies = getMoviesByGenres(id, include_all_movies, maxPage);

		} catch (Exception e) {
			erreur = "Genre inconnu\n";
		}
		return movies;
	}

	public ArrayList<TMDBSearch> getMoviesByGenres(int genre,
			boolean include_all_movies, int maxPage) {
		String url;
		ArrayList<TMDBSearch> movies = new ArrayList<TMDBSearch>();
		int page = 1;
		long total_page = 1;
		boolean maxPageReached = false;
		JSONObject r;
		JSONArray result;
		try {
			url = this.baseURL + "genre/" + genre + "/movies" + this.key
					+ "&include_all_movies=" + include_all_movies;

		} catch (Exception e) {
			erreur = "Genre inconnu\n";
			return movies;
		}
		while (page < maxPage && !maxPageReached) {
			try {
				r = this.getJSON(url + "&page=" + page);
				result = r.getJSONArray("results");
				total_page = r.optLong("total_pages", 1);
				movies.addAll(decodeJSONArray(result, MediaType.Movies));

			} catch (Exception e) {
				this.erreur = e.getMessage();
			}
			page++;
			if (page == total_page) {
				maxPageReached = true;
			}

		}
		return movies;
	}

	public ArrayList<Critics> getMovieReviews(int movieID, int maxPage) {
		String url = this.baseURL + "movie/" + movieID + "/reviews" + this.key;
		ArrayList<Critics> critique = new ArrayList<Critics>();
		int page = 1;
		long total_page = 1;
		boolean maxPageReached = false;
		if (maxPage <= 0)
			maxPage = 1;
		JSONObject c;
		JSONArray result;
		while (page < maxPage && !maxPageReached) {
			try {
				c = this.getJSON(url + "&page=" + page);
				result = c.getJSONArray("results");
				total_page = c.optLong("total_pages", 1);
				for (int i = 0; i < result.length(); i++) {
					critique.add(new Critics(result.getJSONObject(i)));
				}
			} catch (Exception e) {
				this.erreur = e.getMessage();
			}
			page++;
			if (page == total_page) {
				maxPageReached = true;
			}
		}

		return critique;
	}

	// public abstract ArrayList<TheMovieDBSearchResult> getTVSeasonInfos(int
	// tvID, int season);

	public HashMap<String, String> getMediaByID(MediaType type, int id) {

		String url = this.baseURL + type.toString() + "/" + id + this.key;
		HashMap<String, String> features = new HashMap<String, String>();
		try {
			JSONObject mediajson = this.getJSON(url);
			features.put("overview", mediajson.optString("overview"));
			features.put("homepage", mediajson.optString("homepage"));
			features.put("status", mediajson.optString("status").toLowerCase());

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
			features.put("genres", genre_list);

			if (type == MediaType.Movies) {
				features.put("tagline", mediajson.optString("tagline", null));
				features.put("runtime", mediajson.optString("runtime", null));
				features.put("imdb", mediajson.optString("imdb_id", null));

			}

			else if (type == MediaType.TVShow) {
				JSONArray runtime = mediajson.optJSONArray("episode_run_time");
				String rtime = null;
				if (runtime != null && runtime.length() > 0) {
					rtime = "";
					int i;
					for (i = 0; i < runtime.length() - 1; i++) {
						rtime += runtime.optInt(i) + "-";
					}
					rtime += runtime.optInt(i);
				}
				features.put("runtime", rtime);
				features.put("saison", String.valueOf(mediajson.optInt(
						"number_of_seasons", 0)));
				features.put("episode", String.valueOf(mediajson.optInt(
						"number_of_episodes", 0)));

			}
		} catch (Exception e) {
			erreur = "Impossible to process request";
			e.printStackTrace();
		}
		return features;
	}

	public ArrayList<TMDBSearch> getSimilarMovie(int movieID, int maxPage,
			double minVote, int minVoteCount) {

		String url = this.baseURL + "movie/" + movieID + "/similar_movies"
				+ this.key;
		ArrayList<TMDBSearch> sr = new ArrayList<TMDBSearch>();
		sr = getRequestPerLink(url, MediaType.Movies, maxPage);
		Iterator<TMDBSearch> i = sr.iterator();
		ArrayList<TMDBSearch> result = new ArrayList<TMDBSearch>();
		TMDBSearch tmp = null;

		while (i.hasNext()) {
			tmp = i.next();
			if (tmp.getScore() > minVote && tmp.getVoteCount() > minVoteCount)
				result.add(tmp);
		}
		return result;
	}

	// Recherche d'un media, en fonction du type, du titre et aussi, du nombre
	// max de page a visiter
	public ArrayList<TMDBSearch> searchMedia(MediaType type, String name,
			int maxPage) {

		try {
			name = URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			erreur = e.getMessage();
			name = name.replace(" ", "%20");
		} // this is important

		ArrayList<TMDBSearch> sr = new ArrayList<TMDBSearch>();

		if (type != MediaType.Any) {
			String url = this.baseURL + "search/" + type.toString() + this.key
					+ "&query=" + name;
			sr = getRequestPerLink(url, type, maxPage);
		} else {
			sr.addAll(searchMedia(MediaType.TVShow, name, maxPage));
			sr.addAll(searchMedia(MediaType.Movies, name, maxPage));
		}

		return sr;

	}

	// /Cette Classe fait la même chose que SearchResult dans tastekid, mais a
	// des attribut diffrents
}

class TMDBSearch implements MediaInfos {

	private static final long serialVersionUID = 5773288890801480572L;
	String poster, ori_title, title, type;
	int id, voteCount;
	double averageVote, popularity;
	String first_date;
	private HashMap<String, String> addInfos = null;

	public TMDBSearch(JSONObject js, MediaType type) throws JSONException {

		this.poster = js.getString("poster_path");
		this.popularity = js.getDouble("popularity");
		this.id = (int) js.getLong("id");
		this.voteCount = (int) js.getLong("vote_count");
		this.averageVote = js.getDouble("vote_average");
		if (type == MediaType.Movies) {
			this.ori_title = js.getString("original_title");
			this.title = js.getString("title");
			this.type = "movie";
			this.first_date = js.getString("release_date");
		} else if (type == MediaType.TVShow) {
			this.ori_title = js.getString("original_name");
			this.title = js.getString("name");
			this.first_date = js.getString("first_air_date");
			this.type = "show";
		}
	}

	public TMDBSearch(Parcel source) {

		source.readInt();
		this.poster = source.readString();
		this.ori_title = source.readString();
		this.title = source.readString();
		this.type=source.readString();
		this.first_date=source.readString();
		this.id=source.readInt();
		this.voteCount=source.readInt();
		this.averageVote=source.readDouble();
		this.popularity=source.readDouble();

		Bundle bundle = source.readBundle();
		@SuppressWarnings("unchecked")
		HashMap<String, String> serializable = (HashMap<String, String>)bundle.getSerializable("map");
		this.addInfos = serializable; 
		
	}

	public TMDBSearch(Parcel source, boolean b) {
		if(b) source.readInt();
		this.poster = source.readString();
		this.ori_title = source.readString();
		this.title = source.readString();
		this.type=source.readString();
		this.first_date=source.readString();
		this.id=source.readInt();
		this.voteCount=source.readInt();
		this.averageVote=source.readDouble();
		this.popularity=source.readDouble();

		Bundle bundle = source.readBundle();
		@SuppressWarnings("unchecked")
		HashMap<String, String> serializable = (HashMap<String, String>)bundle.getSerializable("map");
		this.addInfos = serializable; 
		
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

	public int getVoteCount() {
		return this.voteCount;
	}

	public int getID() {
		return this.id;
	}

	public int getScore() {
		return (int) this.averageVote * 10;
	}

	public double getPopularity() {
		return this.popularity;
	}

	public String getTitle() {
		return this.getTitle(false);
	}

	public String getTitle(boolean original) {
		if (original)
			return this.ori_title;
		else
			return this.title;
	}
	
	public String getDetailedTitle() {

		return (getTitle() + " (" + first_date + ")");

	}

	public String getOriginalPosterURL() {
		return TheMovieDB.img_URL + "original" + this.poster;
	}

	public String getPosterURL(int size) {
		switch (size) {
		case 0:
			return TheMovieDB.img_URL + "w" + TheMovieDB.poster_size[0]
					+ this.poster;
		case 1:
			return TheMovieDB.img_URL + "w" + TheMovieDB.poster_size[1]
					+ this.poster;
		case 2:
			return TheMovieDB.img_URL + "w" + TheMovieDB.poster_size[2]
					+ this.poster;
		case 3:
			return TheMovieDB.img_URL + "w" + TheMovieDB.poster_size[3]
					+ this.poster;
		case 4:
			return TheMovieDB.img_URL + "w" + TheMovieDB.poster_size[4]
					+ this.poster;
		default:
			return getOriginalPosterURL();
		}
	}

	public String toString() {
		return "\nTitle : " + this.getTitle() + "\nID : " + this.getID()
				+ "\nType : " + this.getType() + "\nDate : " + this.getDate()
				+ "\nScore : " + this.getScore() + "\nPoster url : "
				+ this.getOriginalPosterURL();
	}

	@Override
	public HashMap<String, String> getAdditionalFeatures() {
		if (addInfos == null) {
			addInfos = new TheMovieDB().getMediaByID(
					this.isMovie() ? MediaType.Movies : MediaType.TVShow,
					this.id);
		}
		return addInfos;
	}

	@Override
	public ArrayList<Critics> getCritics() {
		return new TheMovieDB().getMovieReviews(this.id, 1);
	}

	@Override
	public ArrayList<? extends MediaInfos> getSimilar() {
		return this.isMovie() ? new TheMovieDB().getSimilarMovie(this.id, 2,
				2.0, 1) : null;
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

	@Override
	public int describeContents() {

		return 3;
	}

	public void writeToParcel(Parcel out, int arg1) {
		out.writeInt(describeContents());
		out.writeString(poster);
		out.writeString(ori_title);
		out.writeString(title);
		out.writeString(type);
		out.writeString(first_date);
		out.writeInt(id);
		out.writeInt(voteCount);
		out.writeDouble(averageVote);
		out.writeDouble(popularity);
		Bundle bundle = new Bundle();
		bundle.putSerializable("map", addInfos);
		out.writeBundle(bundle);
	}
 
	public static final Parcelable.Creator<TMDBSearch> CREATOR = new Creator<TMDBSearch>() {

		@Override
		public TMDBSearch createFromParcel(Parcel source) {
			return new TMDBSearch(source);
		}

		@Override
		public TMDBSearch[] newArray(int size) {
			return new TMDBSearch[size];
		}

	};

	public static final Parcelable.Creator<TMDBSearch> CREATOR = new Creator<TMDBSearch>() {

		@Override
		public TMDBSearch createFromParcel(Parcel source) {
			return new TMDBSearch(source);
		}

		@Override
		public TMDBSearch[] newArray(int size) {
			return new TMDBSearch[size];
		}

	};

}
