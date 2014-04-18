package com.maclandrol.flibityboop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MediaDetails extends Activity {
	Media m = null;
	MediaInfos mInfos = null;
	ImageLoader imLoader =null;

	static ProgressDialog progressDiag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		if (savedInstanceState == null) {
			if (i != null) {
				mInfos = i.getParcelableExtra("media");
				imLoader= new ImageLoader(getApplicationContext());

			}
		}
		setContentView(R.layout.media_details);
		if (mInfos != null && mInfos instanceof MediaInfos) {
			new LoadMedia().execute(mInfos);
		}

		TextView t = (TextView) findViewById(R.id.d_title);
		if (this.m != null)
			t.setText("thunder");
		if (mInfos != null && mInfos instanceof MediaInfos) {
			Toast.makeText(getApplicationContext(), t.getText(),
					Toast.LENGTH_LONG).show();

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.media_details, menu);
		return true;
	}

	class LoadMedia extends AsyncTask<MediaInfos, Void, Media> {

		@Override
		protected void onPostExecute(Media result) {
			MediaDetails.progressDiag.dismiss();
			TextView error = (TextView) findViewById(R.id.error_text);
			MediaDetails.this.m = result;

			if (result != null) {
				//Resultat trouvé, afficher ce résultat
				error.setVisibility(View.GONE);
				MediaDetails.this.showMedia(result);
				findViewById(R.id.view_content).setVisibility(View.VISIBLE);
			} else {
				//Erreur, aucun media trouvé, afficher ce message d'erreur
				error.setText("We are so sorry that we couldn't find your media, The truth is our 4 APIs sucks!");
				error.setBackgroundResource(R.color.error_color);

			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Demarrer le progress dialog pour l'attente
			((TextView) findViewById(R.id.error_text)).setText("");
			MediaDetails.progressDiag = ProgressDialog.show(MediaDetails.this,
					null, "You stink, Loser!");//"Getting media infos");
		}

		@Override
		protected Media doInBackground(MediaInfos... params) {
			Media media = null;
			//Aller chercher le media
			try {
				media = new Media(params[0]);
			} catch (Exception e) {
				Log.e("Media", "Impossible d'acceder aux media");
			}
			Media.afficheMedia(media);
			return media;
		}

	}

	public void showMedia(Media result) {

		//Mettre les informations basique du media
		TextView t = (TextView) findViewById(R.id.d_title);
		TextView rt_rate = (TextView) findViewById(R.id.rt_rate);
		TextView rt_vote = (TextView) findViewById(R.id.rt_vote);
		TextView rtu_rate = (TextView) findViewById(R.id.rtu_rate);
		TextView rtu_vote = (TextView) findViewById(R.id.rtu_vote);
		t.setText(m.getDetailledTitle());
		double d_rate = result.getIMDBRating();
		int d_vote = result.getIMDBVote();
		String rate = d_rate > 0 ? "" + d_rate : "?";
		String count = d_vote > 0 ? "(" + d_vote + ")" : "";
		((TextView) findViewById(R.id.imdb_rating)).setText(rate);
		((TextView) findViewById(R.id.imdb_voteCount)).setText(count);

		//Les TV shows n'ont pas de rottenTomatoes score
		if (this.mInfos.isShow()) {
			//Remplacer les scores de rotten tomatoes par ceux de trakt
			double score= this.mInfos.getScore();
			if(score>0)
				rt_rate.setText(score+"%");
			rt_vote.setVisibility(View.INVISIBLE);
			((ImageView)findViewById(R.id.freshness)).setImageResource(R.drawable.trakt_love_red);

			//Mettre les ratings de l'user (rottentomatoes à Gone)
			rtu_vote.setVisibility(View.GONE);
			rtu_rate.setVisibility(View.GONE);
			findViewById(R.id.user_freshness).setVisibility(View.GONE);

		} else {
			//Cas d'un film, il faut aller chercher les scores de rottentomatoes
			d_rate = result.getRTRating();
			d_vote = result.getRTVote();
			if (d_rate > 0) {
				rt_rate.setText(d_rate + "%");
			}
			if (d_vote > 0) {
				rt_vote.setText("(" + d_vote + ")");
			}
			d_rate = result.getRTUserRating();
			d_vote = result.getRTUserVote();
			if (d_rate > 0) {
				rtu_rate.setText(d_rate + "%");
			}
			if (d_vote > 0) {
				rtu_vote.setText("(" + d_vote + ")");
			}
			ImageView rt_fresh=(ImageView)findViewById(R.id.freshness);
			ImageView user_fresh = (ImageView)findViewById(R.id.user_freshness);
			String cert=result.getRTCertification();
			
			if(cert.contains("cert")){
				rt_fresh.setImageResource(R.drawable.certified);
			}
			else if(cert.contains("fresh")){
				rt_fresh.setImageResource(R.drawable.fresh);
			}
			else if(cert.contains("rot")){
				rt_fresh.setImageResource(R.drawable.rotten);
			}
			
			if(d_rate<50){
				user_fresh.setImageResource(R.drawable.user_dislike);
			}
		}
		
		//Ajout des détails supplémentaires
		((TextView) findViewById(R.id.runtime)).setText(result.getRuntime());
		((TextView) findViewById(R.id.overview)).setText(result.getSynopsys());
		((TextView) findViewById(R.id.date)).setText(this.mInfos.getDate());
		((TextView) findViewById(R.id.cast)).setText(result.getCast());
		((TextView) findViewById(R.id.author)).setText(result.getDirectors());
		
		//Mettre l'image du poster, en profitant de la sauvegarde dans le cache
		ImageView poster = (ImageView) findViewById(R.id.media_poster);
		imLoader.DisplayImage(this.mInfos.getOriginalPosterURL(), poster);

		//Trailer trouvé, creer un intent de visionage au click (web)
		if(result.hasTrailer()){
			TextView trailerView =(TextView) findViewById(R.id.trailer);
			final String trailer_link=result.getTrailer();
			trailerView.setText(Html.fromHtml("<a href=\""+ trailer_link+ "\">" + result.getTrailerTitle() + "</a>"));
			trailerView.setClickable(true);
			trailerView.setOnClickListener(new WebIntentListener(trailer_link));
			
		}
		//Trailer pas trouvé, cacher le layout
		else{
			findViewById(R.id.trailer_layout).setVisibility(View.GONE);

		}
		//Verifier la présence d'un lien web et cacher ou montrer le layout avec sa valeur selon le cas
		String page = result.getWebLink();
		if(page==null){
			findViewById(R.id.web_layout).setVisibility(View.GONE);
		}
		else{
			TextView linkView =(TextView) findViewById(R.id.wikilink);
			linkView.setText(Html.fromHtml("<a href=\""+ page+ "\">" + (result.hasWiki()?"Wikipedia page":page) + "</a>"));
			linkView.setClickable(true);
			linkView.setOnClickListener(new WebIntentListener(page));
		}
		
		//On va chercher les recommendations ici
		ArrayList <MediaInfos> sim=result.getRecommendations();
		if(sim==null || sim.isEmpty()){
			findViewById(R.id.similar_show_list).setVisibility(View.GONE);
			findViewById(R.id.you_may_like).setVisibility(View.GONE);

		}

		//Lorsque qu'on a moins de 3 recommendations, cacher certains layout et le bouton more.
		// Au besoin cacher le layout parent en entier
		else{
			 Collections.shuffle(sim);
			if(sim.size()<3)
				findViewById(R.id.show_more).setVisibility(View.INVISIBLE);
			int i=0;
			ImageView sim_poster;
			TextView sim_note, sim_title;
			LinearLayout sim_layout;

			while (i < 3 && i < sim.size()) {
				if (i == 0) {
					sim_poster = (ImageView) findViewById(R.id.sim_poster1);
					sim_note=(TextView) findViewById(R.id.vote_1);
					sim_title=(TextView) findViewById(R.id.sim_title1);

				} else if (i == 1) {
					
					sim_poster = (ImageView) findViewById(R.id.sim_poster2);
					sim_note=(TextView) findViewById(R.id.vote_2);
					sim_title=(TextView) findViewById(R.id.sim_title2);


				} else {
					sim_poster = (ImageView) findViewById(R.id.sim_poster3);
					sim_note=(TextView) findViewById(R.id.vote_3);
					sim_title=(TextView) findViewById(R.id.sim_title3);


				}
				imLoader.DisplayImage(sim.get(i).getPosterURL(1), sim_poster);
				sim_poster.setOnClickListener(new MediaDetailsIntentListener(sim.get(i)));
				sim_note.setText(sim.get(i).getScore()+"%");
				sim_title.setText(sim.get(i).getTitle());
				i++;
				

			}
			
			for(int j=i+1; j<4;j++){
				if(j==1)
					sim_layout=(LinearLayout) findViewById(R.id.similar1);
				else if(j==2)
					sim_layout=(LinearLayout) findViewById(R.id.similar2);
				else
					sim_layout=(LinearLayout) findViewById(R.id.similar3);
				sim_layout.setVisibility(View.INVISIBLE);
			}
			
		}
		

	}
	
	//Cette classe permet de demarrer une activité web
	private class WebIntentListener implements OnClickListener{
		private String link;
		public WebIntentListener(String s){
			this.link=s;
		}
		@Override
		public void onClick(View v) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(this.link)));

		}
	}
	
	private class MediaDetailsIntentListener implements OnClickListener{
		MediaInfos m;
		public MediaDetailsIntentListener(MediaInfos m){
			this.m=m;
		}
		@Override
		public void onClick(View v) {
			Intent i = new Intent(MediaDetails.this, MediaDetails.class);
			i.putExtra("media", m);
			startActivity(i);
		}
	}

}
