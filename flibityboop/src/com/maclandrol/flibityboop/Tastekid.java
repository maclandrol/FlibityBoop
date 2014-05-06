package com.maclandrol.flibityboop;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

///CLASSE TASTEKID API!!
public class Tastekid extends API {

	// Arguments pour les requetes avec l'API
	public final static String tastekidAPI = "http://www.tastekid.com/ask/ws?q=",
			tastekidKey = "f=mediare6786&k=y2vhzjjlzwnl";
	String format = "format=JSON";
	final int VERBOSE = 1;

	// Constructeur par défaut, et unique constructeur
	public Tastekid() {
		super(tastekidAPI, tastekidKey);

	}

	// ICI on veut avoir un JSON constitué des FILMS et TVSHOW de la recherche
	// "name" de type qType
	// Example : JSONOBject j = tsk.getJSONMedia("death note",
	// MediaType.Movies);
	public JSONObject getJSONMedia(String name, MediaType qType)
			throws UnsupportedEncodingException {
		JSONObject movies = getJSONMovies(name, qType);
		JSONObject series = getJSONSeries(name, qType);

		try {
			JSONArray serie_result = series.getJSONObject("Similar")
					.optJSONArray("Results");
			for (int i = 0; i < serie_result.length(); i++) {
				movies.getJSONObject("Similar").accumulate("Results",
						serie_result.get(i));
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.erreur = e.getMessage();
		}
		return movies;
	}

	// ICI on veut uniquement le JSON de l'ensemble des reponses de type FILMS
	// dans la requete uniquement (pas de TVSHOW)
	public JSONObject getJSONMovies(String name, MediaType qType)
			throws UnsupportedEncodingException {

		String resType = "";
		name = URLEncoder.encode(name, "UTF-8");
		if (qType == MediaType.TVShow)
			resType = "show:";
		else if (qType == MediaType.Movies)
			resType = qType.toString() + ":";

		String url = this.baseURL;
		String resForm = "//movies&verbose=" + VERBOSE + "&format=JSON&";
		url = url.concat(resType + name);
		url = url + resForm + this.key;
		return super.getJSON(url);

	}

	// ICI c'est plutot resultats de type serie qu'on recherche
	public JSONObject getJSONSeries(String name, MediaType qType)
			throws UnsupportedEncodingException {
		String resType = "";
		name = URLEncoder.encode(name, "UTF-8");

		if (qType == MediaType.TVShow)
			resType = "show:";
		else if (qType == MediaType.Movies)
			resType = qType.toString() + ":";

		String url = this.baseURL;
		String resForm = "//shows&verbose=" + VERBOSE + "&format=JSON&";
		url = url.concat(resType + name);
		url = url + resForm + this.key;
		return super.getJSON(url);
	}

	// Cette methode retourne les infos sur le query (le titre cherché)
	// NB: xa prends un JSON en argument et non un name, donc faire getJSON****
	// avant
	public TKSearchResult getMediaInfos(JSONObject jsList) {

		TKSearchResult current = null;

		JSONArray jsMediaList;
		try {
			jsMediaList = jsList.getJSONObject("Similar").getJSONArray("Info");
			JSONObject jsMedia = jsMediaList.getJSONObject(0);
			current = this.GetMediaFeatures(jsMedia);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return current;
	}

	// Cette méthode retourne les resultats des recommendation obtenue pour la
	// requete:
	public ArrayList<TKSearchResult> getRecomMediaInfos(
			JSONObject jsList) {

		ArrayList<TKSearchResult> currents = new ArrayList<TKSearchResult>();
		try {
			JSONArray jsMediaList = jsList.getJSONObject("Similar")
					.getJSONArray("Results");
			for (int i = 0; i < jsMediaList.length(); i++) {
				JSONObject jsMedia = jsMediaList.getJSONObject(i);
				currents.add(this.GetMediaFeatures(jsMedia));

			}

		} catch (ParseException e) {
			this.erreur = "Erreur JSON (parse) :" + e.getMessage();
		} catch (JSONException e) {
			this.erreur = "Erreur JSON : " + e.getMessage();
		} catch (Exception e) {
			this.erreur = "Erreur Inconnue: " + e.getMessage();
		}
		return currents;
	}

	// Methode privée pour retourner un objet de type Tastekid.SearchResult à
	// partir d'un JSON
	private TKSearchResult GetMediaFeatures(JSONObject jsMedia){
		return new TKSearchResult(jsMedia.optString("wTeaser"),
				jsMedia.optString("Type"), jsMedia.optString("Name"),
				jsMedia.optString("wUrl"), jsMedia.optString("yTitle"),
				jsMedia.optString("yUrl"));
	}

}

// Cette classe contient les informations et les méthodes propres aux medias
// retournés par tastekid
class TKSearchResult implements Parcelable, Serializable {
	
	private static final long serialVersionUID = 8605593584797495584L;
	String summary, type, title, webpage, ytbtitle, ytblink;

	public TKSearchResult(String summary, String type, String title,
			String webpage, String ytbtitle, String ytblink) {
		this.type = type;
		this.summary = summary;// .replaceAll("\\", "");
		this.title = title;
		this.webpage = webpage;
		this.ytblink = ytblink;
		this.ytbtitle = ytbtitle;
	}

	public TKSearchResult(Parcel source) {
		this.summary = source.readString();
		this.type = source.readString();
		this.title = source.readString();
		this.webpage = source.readString();
		this.ytblink = source.readString();
		this.ytbtitle = source.readString();
		
	}

	public String getSummary() {
		return this.summary;
	}

	public boolean isMovie() {
		return this.type.equalsIgnoreCase("movie");
	}

	public boolean isShow() {
		return this.type.equalsIgnoreCase("show");
	}

	public String getType() {
		return this.type;
	}

	public String getTitle() {
		return this.title;
	}

	public String getPage() {
		return this.webpage;
	}

	public String getYoutubeLink() {
		return this.ytblink;
	}

	public String toString() {
		return "Type : " + this.type + "\nTitle : " + this.title
				+ "\nWeb Page : " + this.webpage + "\nYoutube Trailer : "
				+ this.ytblink;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(summary);
		dest.writeString(type);
		dest.writeString(title);
		dest.writeString(webpage);
		dest.writeString(ytbtitle);
		dest.writeString(ytblink);

	}

	public static final Parcelable.Creator<TKSearchResult> CREATOR = new Creator<TKSearchResult>() {

		@Override
		public TKSearchResult createFromParcel(Parcel source) {
			return new TKSearchResult(source);
		}

		@Override
		public TKSearchResult[] newArray(int size) {
			return new TKSearchResult[size];
		}

	};

}
