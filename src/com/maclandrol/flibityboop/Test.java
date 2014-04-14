package com.maclandrol.flibityboop;

import java.util.ArrayList;

import org.json.JSONException;

import com.maclandrol.flibityboop.API.MediaType;

public class Test {

	/**
	 * @param args
	 * @throws JSONException
	 */
	//Cette classe permet de tester les api
	public static void main(String[] args) throws JSONException {
		TheMovieDB tmdb = new TheMovieDB();
		RottenTomatoes rt = new RottenTomatoes();
		TraktTV tk = new TraktTV();

		System.out.println("** Start TMDB search");
		long t= System.currentTimeMillis();
		ArrayList<TMDBSearch> a = tmdb.searchMedia(MediaType.Any, "thunder", 2);
		t=System.currentTimeMillis()-t;

		for (int i = 0; i < a.size(); i++) {
			System.out.println("* " + (i + 1) + ") ");
			System.out.println(a.get(i));
			System.out.println();
		}
		System.out.println("** TMDB search end with :" +a.size()+" in t="+t+"\n\n");
		
//		System.out.println("** Start TraktTV search");
//		t= System.currentTimeMillis();
//		ArrayList<TraktTVSearch> a1= tk.searchShow("tropic");
//		t=System.currentTimeMillis()-t;
//
//		for (int i = 0; i < a1.size(); i++) {
//			//System.out.println("* " + (i + 1) + ") ");
//			//System.out.println(a1.get(i));
//			//System.out.println();
//		}
//		System.out.println("** TraktTV search end with :"+a1.size()+" in t="+t+"\n\n");
//		
//		System.out.println("** Start RottenTomatoes search");
//		t= System.currentTimeMillis();
//		ArrayList<RTSearch> a2=rt.searchMovies("tropic", 30, 2);
//		t=System.currentTimeMillis()-t;
//
//		for (int i = 0; i < a2.size(); i++) {
//			//System.out.println("* " + (i + 1) + ") ");
//			//System.out.println(a2.get(i));
//			//System.out.println();
//		}
//		System.out.println("** RottenTomatoes search end with :"+a2.size()+" in t="+t+"\n\n");
//	
//		Media film1=new Media(a.get(1));//tmdb
//		Media film2=new Media(a2.get(0));//rottentomatoes
//		
//		System.out.println("***TMDB SEARCH RESULTAT");
//		Media.afficheMedia(film1);
//		System.out.println("\n\n");
//		System.out.println("***RT SEARCH RESULTAT");
//		Media.afficheMedia(film1);
	}
	
}
