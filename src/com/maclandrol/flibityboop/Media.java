package com.maclandrol.flibityboop;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Media implements Parcelable, Serializable {

	private static final long serialVersionUID = -1709498292286584203L;
	MediaInfos mediainfos;
	ArrayList<Critics> critiques;
	API.MediaType type;
	HashMap<String, String> addInfos = null;
	HashSet<MediaInfos> similarMedia;
	TKSearchResult tk_info = null;

	public Media(MediaInfos infos) {
		this.mediainfos = infos;
		addInfos = this.mediainfos.getAdditionalFeatures();
		type = infos.getType();
		critiques = infos.getCritics();
		Tastekid t = new Tastekid();
		addInfos.putAll(t.getADDdata(addInfos.get("imdb"), infos.getTitle(),
				true));
		ArrayList<TKSearchResult> tk_similar = null;
		try {
			JSONObject jo = t.getJSONMedia(this.mediainfos.getTitle(), type);
			jo.toString(4);
			tk_info = t.getMediaInfos(jo);
			tk_similar = t.getRecomMediaInfos(jo);

		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();

		} catch (JSONException e) {
			Log.i("TasteKid","Maximum request Reached");
		}
		similarMedia = new HashSet<MediaInfos>();
		similarMedia.addAll(mediainfos.getSimilar());
		if (tk_similar != null) {
			for (TKSearchResult tk_s : tk_similar) {
				if (tk_s.isMovie()) {
					RottenTomatoes rt = new RottenTomatoes();
					similarMedia.addAll(rt.searchMovies(tk_s.getTitle(), 1, 1));
				} else {
					TraktTV tktv = new TraktTV();
					similarMedia.addAll(tktv.searchShow(tk_s.getTitle(), 1));

				}
			}
		}

	}

	public Media(Parcel source) {

		Bundle bundle = source.readBundle();
		@SuppressWarnings("unchecked")
		HashMap<String, String> ser_map = (HashMap<String, String>) bundle
				.getSerializable("map");
		addInfos = ser_map;
		this.mediainfos = source.readParcelable(MediaInfos.class
				.getClassLoader());
		this.tk_info = source.readParcelable(TKSearchResult.class
				.getClassLoader());
		ArrayList <MediaInfos>sim_media = new ArrayList<MediaInfos>();
		source.readTypedList(sim_media, MediaInfos.CREATOR);
		this.similarMedia = new HashSet<MediaInfos>(sim_media);
		this.critiques = new ArrayList<Critics>();
		source.readTypedList(this.critiques, Critics.CREATOR);
	}

	public String getWriter() {
		return this.addInfos.get("iWriter");

	}

	public String getCast() {
		String cast = "Not Available!";
		if (this.addInfos.containsKey("iActors")) {
			cast = addInfos.get("iActors");
		} else if (addInfos.containsKey("actors")) {
			cast = addInfos.get("actors");
		}

		return cast;
	}

	public String getDirectors() {
		String directors = "Not Available!";
		if (this.addInfos.containsKey("iDirector")) {
			directors = this.addInfos.get("iDirector");
		} else if (this.addInfos.containsKey("directors")) {
			directors = this.addInfos.get("directors");
		}

		return directors;
	}

	public String getProduction() {
		return this.addInfos.get("rtProduction");
	}

	public String getSynopsys() {
		String resume = "Synopsis not available, We are so, so sorry!";
		if (this.addInfos.get("overview")!=null && !this.addInfos.get("overview").isEmpty()) {
			resume = this.addInfos.get("overview");
		} else if (this.tk_info != null) {
			String summary = this.tk_info.getSummary();
			if (summary != null && !summary.isEmpty())
				resume = summary;
		} else if (this.addInfos.get("iPlot")!=null && !this.addInfos.get("iPlot").isEmpty()) {
			resume = this.addInfos.get("iPlot");
		}
		return resume;
	}

	public String getCriticsConsensus() {
		String cc = this.addInfos.get("rtConsensus");
		if (cc == null)
			cc = this.mediainfos instanceof RTSearch ? ((RTSearch) this.mediainfos)
					.getConsensusCritic() : null;
		return cc;
	}

	public ArrayList<Critics> getCriticsList() {
		return this.critiques;
	}

	public String getDetailledTitle() {

		String years = "";
		if (this.addInfos.containsKey("iYears"))
			years = years + this.addInfos.get("iYears");
		else if (this.mediainfos instanceof RTSearch)
			years = years + ((RTSearch) this.mediainfos).years;
		else
			years = years + mediainfos.getDate().split("-")[0];

		return years.isEmpty() ? this.mediainfos.getTitle() : this.mediainfos
				.getTitle() + " (" + years + ")";

	}

	public double getIMDBRating() {
		String rate = this.addInfos.get("iRating");
		double val = -1;
		try {
			val = rate != null ? Double.parseDouble(rate) : -1.0;

		} catch (Exception e) {

		}
		return val;
	}

	public int getRTUserRating() {
		String rate = this.addInfos.get("rtUserMeter");
		double val = -1;
		if (val < 0 && this.mediainfos instanceof RTSearch)
			val = ((RTSearch) this.mediainfos).audience_score;
		else {
			try {
				val = rate != null ? Double.parseDouble(rate) : -1.0;

			} catch (Exception e) {

			}
		}

		return (int) val;
	}

	public int getRTRating() {
		String rate = this.addInfos.get("rtMeter");
		double val = -1;
		if (val < 0 && this.mediainfos instanceof RTSearch)
			val = ((RTSearch) this.mediainfos).critics_score;
		else {
			try {
				val = rate != null ? Double.parseDouble(rate) : -1.0;

			} catch (Exception e) {

			}
		}
		return (int) val;
	}

	public int getIMDBVote() {
		String vote = this.addInfos.get("iVotes");
		try {
			return vote != null ? Integer.parseInt(vote.replace(",", "")) : -1;
		} catch (Exception e) {
			return -1;
		}
	}

	public int getRTVote() {
		String vote = this.addInfos.get("rtVotes");
		try{
		return vote != null ? Integer.parseInt(vote.replace(",", "")) : -1;
		}catch (Exception e){
			return -1;
		}
	}

	public int getRTUserVote() {
		String vote = this.addInfos.get("rtUserVotes");
		try {
			return vote != null ? Integer.parseInt(vote.replace(",", "")) : -1;
		} catch (Exception e) {
			return -1;
		}
	}

	public boolean hasTrailer() {
		return this.tk_info != null && this.tk_info.getYoutubeLink() != null
				&& !this.tk_info.getYoutubeLink().isEmpty();
	}

	public boolean hasWiki() {
		return this.tk_info != null && this.tk_info.getPage() != null
				&& !this.tk_info.getPage().isEmpty();
	}

	public String getRTCertification() {
		String cert=this.addInfos.get("rtCertification");
		return cert!=null?cert:"";
	}

	public String getTrailer() {
		return this.tk_info.getYoutubeLink();
	}
	
	public String getTrailerTitle(){
		return this.tk_info.ytbtitle;
	}

	public String getWiki() {
		return this.tk_info.getPage();
	}
	
	public String getWebLink(){
		String link=null;
		if(this.hasWiki())
			link= this.getWiki();
		else{
			if(this.addInfos.containsKey("homepage"))
				link=this.addInfos.get("homepage");
			else
				link=this.addInfos.get("rtWebsite");
			
		}
		return link;
	}

	public String getRuntime() {
		String runtime = "N/A";
		if (this.addInfos.containsKey("iRuntime"))
			runtime = this.addInfos.get("iRuntime");
		else if (this.addInfos.containsKey("runtime")) {
			runtime = this.addInfos.get("runtime");
			if (!(this.mediainfos instanceof RTSearch)) {
				runtime += " min";
			}
		}
		return runtime;
	}

	public String Awards() {
		return this.addInfos.get("iAwards");
	}

	public String getCompleteDate() {
		return this.mediainfos.getDate();
	}

	public String getPoster(){
		String poster_url= this.mediainfos.getOriginalPosterURL();
		if(poster_url==null || poster_url.isEmpty()){
			poster_url=this.addInfos.get("iPoster");
		}
		return poster_url;
	}
	
	public ArrayList<MediaInfos> getRecommendations() {
		return new ArrayList<MediaInfos>(this.similarMedia);
	}

	public static void afficheMedia(Media m) {
		HashMap<String, String> l = m.addInfos;
		HashSet<MediaInfos> similar = m.similarMedia;
		System.out.println("Current MediaInfos : \n");
		System.out.println(m.mediainfos);
		System.out.println("List of features :\n");
		for (String name : l.keySet()) {
			String key = name.toString();
			String value = l.get(name);
			System.out.println(key + " : " + value);
		}

		System.out.println("\nList of Similar movie");
		int i = 0;
		for (MediaInfos mi : similar) {
			i++;
			System.out.println("* " + (i) + ") ");
			System.out.println(mi);
			System.out.println();
		}
	}
	
	public String getShare(){
		return "Hey Dude, I'm using FlibityBoop and I find this great "+this.mediainfos.getType().toString()+
				(this.mediainfos.isShow()?" show":"") +" : " + this.getDetailledTitle() +". It's rated " +
				this.mediainfos.getScore()+"%.\n"+(this.hasTrailer()?"Here is the trailer: "+this.getTrailer()+
						".\n":"")+(this.hasWiki()?"You can also find all the informations on the wiki page: "+
				this.getWiki()+".\n":"")+"Check it if you haven't seen it yet!!"; 		
	}
	
	@Override
	public int hashCode(){
		return this.mediainfos.hashCode();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle bundle = new Bundle();
		bundle.putSerializable("map", addInfos);
		dest.writeBundle(bundle);
		dest.writeParcelable(mediainfos, PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeParcelable(this.tk_info, PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeTypedList(new ArrayList<MediaInfos>(this.similarMedia));
		dest.writeTypedList(this.critiques);
	}

	public static final Parcelable.Creator<Media> CREATOR = new Creator<Media>() {

		@Override
		public Media createFromParcel(Parcel source) {
			return new Media(source);
		}

		@Override
		public Media[] newArray(int size) {
			return new Media[size];
		}

	};

}
