/**
 * IFT2905 : Interface personne machine
 * Projet de session: FlibityBoop.
 * Team: Vincent CABELI, Henry LIM, Pamela MEHANNA, Emmanuel NOUTAHI, Olivier TASTET
 * @author: Emmanuel Noutahi, Vincent Cabeli
 */
package com.maclandrol.flibityboop;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

/**
 * API, superclasse de toutes les classes de requêtes vers une API spécifique
 */
public class API {

	
	public String baseURL;
	public String key;
	String erreur;
	
	/*
	 * Enum
	 * MediaType specifie le type de media (Film ou TVShow) pour les requêtes 
	 */
	public enum MediaType {
		Movies("movie"), TVShow("tv"), Any("");

		private String name = "";

		MediaType(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

	public API(String baseURL, String key) {
		this.baseURL = baseURL;
		this.key = key;
		erreur = null;
	}

	public API() {
		baseURL = "";
		key = "";
		erreur = null;
	}

	/*
	 * getBitmapPoster, retourne l'image Bitmap à partit d'un url 
	 */
	public static Bitmap getBitmapPoster(String poster_url) {
		Bitmap webposter = null;
		Log.d("url", poster_url);
		try {
			InputStream in = new java.net.URL(poster_url).openStream();
			BufferedInputStream buf = new BufferedInputStream(in);
			webposter = BitmapFactory.decodeStream(buf);
			if (in != null) {
				in.close();
			}
			if (buf != null) {
				buf.close();
			}
		} catch (MalformedURLException e) {
			Log.e("url", "malformedURL");
		} catch (IOException e) {
			Log.e("Error", "IO exception");
		}
		return webposter;
	}

	/*
	 * getADDdata, retourne les données additionnelles d'un mediainfo par une requête sur OMDBAPI 
	 */
	public HashMap<String, String> getADDdata(String imdb_id, String title,
			boolean tomatoes) {

		JSONObject js = this.getJSON("http://www.omdbapi.com/?i=" + imdb_id
				+ "&tomatoes=" + tomatoes);
		HashMap<String, String> imdb_res = new HashMap<String, String>();
		if (js == null || !js.optBoolean("Response")) {
			title = Uri.encode(title);
			js = this.getJSON("http://www.omdbapi.com/?t=" + title
					+ "&tomatoes=" + tomatoes);
		}
		if (js != null && js.optBoolean("Response")) {
			imdb_res.put("iTitle", js.optString("Title"));
			imdb_res.put("iYears", js.optString("Year"));
			imdb_res.put("iRated", js.optString("Rated"));
			imdb_res.put("iRuntime", js.optString("Runtime"));
			imdb_res.put("iGenre", js.optString("Genre"));
			imdb_res.put("iDirector", js.optString("Director"));
			imdb_res.put("iWriter", js.optString("Writer"));
			imdb_res.put("iActors", js.optString("Actors"));
			imdb_res.put("iPlot", js.optString("Plot"));
			imdb_res.put("iPoster", js.optString("Poster"));
			imdb_res.put("iAwards", js.optString("Awards"));
			imdb_res.put("iRating", js.optString("imdbRating"));
			imdb_res.put("iVotes", js.optString("imdbVotes"));
			imdb_res.put("iMetascore", js.optString("Metascore"));

			String type = js.optString("Type");
			if (type.equals("movie") && js.opt("tomatoMeter") != null) {
				imdb_res.put("rtUserMeter", js.optString("tomatoUserMeter"));
				imdb_res.put("rtUserRating", js.optString("tomatoUserRating"));
				imdb_res.put("rtMeter", js.optString("tomatoMeter"));
				imdb_res.put("rtRating", js.optString("tomatoRating"));
				imdb_res.put("rtUserVotes", js.optString("tomatoUserReviews"));
				imdb_res.put("rtVotes", js.optString("tomatoReviews"));
				imdb_res.put("rtCertification", js.optString("tomatoImage"));
				imdb_res.put("rtConsensus", js.optString("tomatoConsensus"));
				imdb_res.put("rtProduction", js.optString("Production"));
				imdb_res.put("rtWebsite", js.optString("Website"));
				imdb_res.put("rtBoxOffice", js.optString("BoxOffice"));
			}
		}
		return imdb_res;

	}

	/*
	 * getJSON, retourne l'objet json à partir d'une url
	 */
	public JSONObject getJSON(String url) {

		JSONObject js = null;
		try {
			HttpEntity page = GetReq(url);
			js = new JSONObject(EntityUtils.toString(page, HTTP.UTF_8));
		} catch (ClientProtocolException e) {
			erreur = "Erreur HTTP (protocole) :" + e.getMessage();
		} catch (IOException e) {
			erreur = "Erreur HTTP (IO) :" + e.getMessage();
		} catch (ParseException e) {
			erreur = "Erreur JSON (parse) :" + e.getMessage();
		} catch (JSONException e) {
			erreur = "Erreur JSON :" + e.getMessage();
		}

		return js;
	}
	
	/*
	 * getJSONArray, retourne l'objet JsonArray à partir d'un url
	 */
	public JSONArray getJSONArray(String url) {
		JSONArray jarray = null;
		try {
			HttpEntity page = GetReq(url);
			jarray = new JSONArray(EntityUtils.toString(page, HTTP.UTF_8));
		} catch (ClientProtocolException e) {
			erreur = "Erreur HTTP (protocole) :" + e.getMessage();
		} catch (IOException e) {
			erreur = "Erreur HTTP (IO) :" + e.getMessage();
		} catch (ParseException e) {
			erreur = "Erreur JSON (parse) :" + e.getMessage();
		} catch (JSONException e) {
			erreur = "Erreur JSON :" + e.getMessage();
		}

		return jarray;
	}

	/*
	 * getReq, retourne le résultat de la requête effectué à partir de l'url
	 */
	public HttpEntity GetReq(String url) throws ClientProtocolException,
			IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet http = new HttpGet(url);
		HttpResponse response = httpClient.execute(http);
		return response.getEntity();
	}
}
