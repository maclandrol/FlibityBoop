package com.maclandrol.flibityboop;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONObject;

public class Media {

	MediaInfos mediainfos;
	ArrayList<Critics> critiques;
	API.MediaType type;
	HashMap<String, String> addInfos =null;
	HashSet<MediaInfos> similarMedia;
	Tastekid.TKSearchResult tk_info =null;
	
	public Media(MediaInfos infos){
		this.mediainfos= infos;
		addInfos= this.mediainfos.getAdditionalFeatures();
		type= infos.getType();
		critiques= infos.getCritics();
		Tastekid t= new Tastekid();
		addInfos.putAll(t.getADDdata(addInfos.get("imdb"), true));
		ArrayList<Tastekid.TKSearchResult> tk_similar=null;
		try {
			JSONObject jo= t.getJSONMedia(this.mediainfos.getTitle(), type);
			tk_info = t.getMediaInfos(jo);
			tk_similar= t.getRecomMediaInfos(jo);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		similarMedia= new HashSet<MediaInfos> ();
		similarMedia.addAll(mediainfos.getSimilar());
		RottenTomatoes rt= new RottenTomatoes();
		TraktTV tktv = new TraktTV();
		if(tk_similar!=null){
			for (Tastekid.TKSearchResult tk_s:tk_similar){
				if(tk_s.isMovie()){
					similarMedia.addAll(rt.searchMovies(tk_s.getTitle(),1,1));
				}
				else{
					similarMedia.addAll(tktv.searchShow(tk_s.getTitle(), 1));

				}
			}
		}
		
	}
		
	public static void afficheMedia(Media m){
		HashMap<String, String> l=m.addInfos;
		HashSet<MediaInfos> similar=m.similarMedia;
		System.out.println("Current MediaInfos : \n");
		System.out.println(m.mediainfos);
		System.out.println("List of features :\n");
		for (String name: l.keySet()){
            String key =name.toString();
            String value = l.get(name).toString();  
            System.out.println(key + " : " + value);  
		} 
		
		System.out.println("\nList of Similar movie");
		int i=0;
		for (MediaInfos mi:similar) {
			i++;
			System.out.println("* " + (i) + ") ");
			System.out.println(mi);
			System.out.println();
		}	
	}
	
		
}
