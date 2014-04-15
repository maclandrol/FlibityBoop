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
		addInfos.putAll(t.getADDdata(addInfos.get("imdb"), infos.getTitle(), true));
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
		if(tk_similar!=null){
			for (Tastekid.TKSearchResult tk_s:tk_similar){
				if(tk_s.isMovie()){
					RottenTomatoes rt= new RottenTomatoes();
					similarMedia.addAll(rt.searchMovies(tk_s.getTitle(),1,1));
				}
				else{
					TraktTV tktv = new TraktTV();
					similarMedia.addAll(tktv.searchShow(tk_s.getTitle(), 1));

				}
			}
		}
		
	}
		
	public String getWriter(){
		return this.addInfos.get("iWriter");

	}
	
	public String getCast(){
		String cast=null;
		if(this.addInfos.containsKey("iActors")){
			cast= addInfos.get("iActors");
		}
		else if (addInfos.containsKey("actors")){
			cast= addInfos.get("actors");
		}
		
		return cast;
	}
	
	public String getDirectors(){
		String directors=null;
		if(this.addInfos.containsKey("iDirector")){
			directors= this.addInfos.get("iDirector");
		}
		else if(this.addInfos.containsKey("directors")){
			directors=this.addInfos.get("directors");
		}
			
		return directors;
	}
	
	
	public String getProduction(){
		return this.addInfos.get("rtProduction");
	}
	
	public String getSynopsys(){
		String resume= "Synopsis not available, We are so, so sorry!";
		if(this.addInfos.containsKey("overview")){
			resume= this.addInfos.get("overview");
		}
		else if(this.tk_info!=null){
			String summary=this.tk_info.getSummary();
			if(summary!=null && !summary.isEmpty())
				resume=summary;
		}
		else if(this.addInfos.containsKey("iPlot")){
			resume=this.addInfos.get("iPlot");
		}
		return resume;
	}
	
	
	public String getCriticsConsensus(){
		String cc= this.addInfos.get("rtConsensus");
		if(cc==null)
			cc= this.mediainfos instanceof RTSearch?((RTSearch)this.mediainfos).getConsensusCritic():null;
		return cc;
	}
	
	public ArrayList<Critics> getCriticsList(){
		return this.critiques;
	}
	
	public String getDetailledTitle(){
		
		String years="";
		if(this.addInfos.containsKey("iYears"))
			years=years+this.addInfos.get("iYears");
		else if(this.mediainfos instanceof RTSearch)
			years=years+((RTSearch)this.mediainfos).years;
		else
			years=years+mediainfos.getDate().split("-")[0];
		
		return years.isEmpty()?this.mediainfos.getTitle():this.mediainfos.getTitle()+" ("+years+")";
		
	}
	
	public double getIMDBRating(){
		String rate =this.addInfos.get("iRating");
		return rate!=null?Double.parseDouble(rate):-1.0;
	}
	
	public double getRTUserRating(){
		String rate =this.addInfos.get("rtUserRating");
		double val=rate!=null?Double.parseDouble(rate):-1.0;
		if(val<0 && this.mediainfos instanceof RTSearch) val= ((RTSearch)this.mediainfos).audience_score;
		return val;	}
	
	public double getRTRating(){
		String rate =this.addInfos.get("rtRating");
		double val=rate!=null?Double.parseDouble(rate):-1.0;
		if(val<0 && this.mediainfos instanceof RTSearch) val= ((RTSearch)this.mediainfos).critics_score;
		return val;
	}
	
	public double getIMDBVote(){
		String vote =this.addInfos.get("iVotes");
		return vote!=null?Double.parseDouble(vote):-1.0;
	}
	
	public double getRTVote(){
		String vote =this.addInfos.get("rtVotes");
		return vote!=null?Double.parseDouble(vote):-1.0;
	}
	
	public double getRTUserVote(){
		String vote =this.addInfos.get("rtUserVotes");
		return vote!=null?Double.parseDouble(vote):-1.0;
	}
	
	public boolean hasTrailer(){
		return this.tk_info!=null && this.tk_info.getYoutubeLink()!=null && !this.tk_info.getYoutubeLink().isEmpty();
	}
	
	public boolean hasWiki(){
		return this.tk_info!=null && this.tk_info.getPage()!=null && !this.tk_info.getPage().isEmpty();
	}
	
	public String getRTCertification(){
		return this.addInfos.get("rtCertification");
	}
	
	public String getTrailer(){
		return this.tk_info.getYoutubeLink();
	}
	
	public String getWiki(){
		return this.tk_info.getPage();
	}
	
	public String getRuntime(){
		String runtime=null; 
		if(this.addInfos.containsKey("iRuntime"))
			runtime=this.addInfos.get("iRuntime");
		else if(!(this.mediainfos instanceof RTSearch)){
			runtime=this.addInfos.get("runtime")+" min";
		}
		return runtime;
	}
	
	public String Awards(){
		return this.addInfos.get("iAwards");
	}
	
	public String getCompleteDate(){
		return this.mediainfos.getDate();
	}
	
	public HashSet<MediaInfos> getRecommendations(){
		return this.similarMedia;
	}
	
		
	public static void afficheMedia(Media m){
		HashMap<String, String> l=m.addInfos;
		HashSet<MediaInfos> similar=m.similarMedia;
		System.out.println("Current MediaInfos : \n");
		System.out.println(m.mediainfos);
		System.out.println("List of features :\n");
		for (String name: l.keySet()){
            String key =name.toString();
            String value = l.get(name);  
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
