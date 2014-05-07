package com.maclandrol.flibityboop;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
// android.v4.

public class MediaDetails extends BaseActivity {
	
	Media m = null;
	MediaInfos mInfos = null;
	ArrayList<MediaInfos> similar = null;
	int critics_pos = 0;
	CriticFragment cf = null;
	ImageLoader imLoader = null;
	ToggleButton fav ;
	ContentResolver resolver;
	FragmentManager fm;
	SharedPreferences sharedPref = null;
	static ProgressDialog progressDiag;

	//enable share option here only
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem share=menu.findItem(R.id.menu_item_share);
		share.setVisible(true);
		return true;

	}
	 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		fm = getFragmentManager();
		resolver = this.getContentResolver();
		setContentView(R.layout.media_details);
		imLoader = new ImageLoader(getApplicationContext());
		fav= (ToggleButton)findViewById(R.id.fav);
		if (savedInstanceState != null) {
			this.critics_pos = savedInstanceState.getInt("critic");
			this.m = savedInstanceState.getParcelable("media");
			this.mInfos = savedInstanceState.getParcelable("mediainfo");
			this.similar = savedInstanceState.getParcelableArrayList("similar");
			// Toast.makeText(getApplicationContext(), "instance saved",
			// Toast.LENGTH_LONG).show();

			showMedia(m);
		} else {
			if (i != null) {
				mInfos = i.getParcelableExtra("mediainfo");
				m= i.getParcelableExtra("mediaComplete");
				if(m!=null && m instanceof Media){
					this.mInfos=m.mediainfos;
					new LoadMedia(false).execute(mInfos);

				}
				else if (mInfos != null && mInfos instanceof MediaInfos) {
					new LoadMedia(true).execute(mInfos);
				}
			}
		}

		//shareIntent.setType("image/png");
		shareIntent.setType("text/plain");
		
	}

	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("mediainfo", this.mInfos);
		outState.putParcelable("media", this.m);
		outState.putInt("critic", this.critics_pos);
		outState.putParcelableArrayList("similar", this.similar);
	}

	
		
	public void showMedia(Media result) {
		share(m.getShare()+"\n\n"+sharedPref.getString("username","FlibityBoop Team"));
		int in_db= resolver.query(MediaContentProvider.CONTENT_URI, null, DBHelperMedia.M_ID+" LIKE ?", new String [] {Integer.toString(mInfos.hashCode())}, null).getCount();
		fav.setChecked(in_db>0);
		fav.setTextOn("YES");
		fav.setTextOff("NO");
		fav.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton tb,	boolean isChecked) {
				if(isChecked)
					MediaDetails.this.addToDB(m, false);
				else
					MediaDetails.this.delFromDB(m);
								
			}
		
		});
		
		// Mettre les informations basique du media
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

		// Les TV shows n'ont pas de rottenTomatoes score
		if (this.mInfos.isShow()) {
			// Remplacer les scores de rotten tomatoes par ceux de trakt
			int score = this.mInfos.getScore();
			if (score > 0)
				rt_rate.setText(score + "%");
			rt_vote.setVisibility(View.INVISIBLE);
			((ImageView) findViewById(R.id.freshness))
					.setImageResource(R.drawable.trakt_love_red);

			// Mettre les ratings de l'user (rottentomatoes à Gone)
			rtu_vote.setVisibility(View.GONE);
			rtu_rate.setVisibility(View.GONE);
			findViewById(R.id.user_freshness).setVisibility(View.GONE);
			try{
				String date =((TraktTVSearch)mInfos).getAirDay()+", "+((TraktTVSearch)mInfos).getAirTime();
				TextView time =(TextView)findViewById(R.id.airtime);
				time.setText(date);
				findViewById(R.id.fixed_airtime_text).setVisibility(View.VISIBLE);
			}catch(Exception e){
				Log.d("media", "Not TraktTV exception");
			}
			
			String network = result.addInfos.get("network");
			if(network!=null){
				TextView ntw=(TextView)findViewById(R.id.network);
				ntw.setText(network);
				findViewById(R.id.fixed_network_text).setVisibility(View.VISIBLE);

			}
			
			String status = result.addInfos.get("status");
			if(status!=null){
				TextView ntw=(TextView)findViewById(R.id.status);
				ntw.setText(status);
				findViewById(R.id.fixed_status_text).setVisibility(View.VISIBLE);

			}


		} else {
			// Cas d'un film, il faut aller chercher les scores de
			// rottentomatoes
			int rt_rate_value = result.getRTRating();
			d_vote = result.getRTVote();
			if (rt_rate_value > 0) {
				rt_rate.setText(rt_rate_value + "%");
			}
			if (d_vote > 0) {
				rt_vote.setText("(" + d_vote + ")");
			}
			rt_rate_value = result.getRTUserRating();
			d_vote = result.getRTUserVote();
			if (rt_rate_value > 0) {
				rtu_rate.setText( rt_rate_value + "%");
			}
			if (d_vote > 0) {
				rtu_vote.setText("(" + d_vote + ")");
			}
			ImageView rt_fresh = (ImageView) findViewById(R.id.freshness);
			ImageView user_fresh = (ImageView) findViewById(R.id.user_freshness);
			String cert = result.getRTCertification();
			if (cert.contains("cert")) {
				rt_fresh.setImageResource(R.drawable.certified);
			} else if (cert.contains("fresh")) {
				rt_fresh.setImageResource(R.drawable.fresh);
			} else if (cert.contains("rot")) {
				rt_fresh.setImageResource(R.drawable.rotten);
			}

			if (rt_rate_value < 50) {
				user_fresh.setImageResource(R.drawable.user_dislike);
			}
		}

		// Ajout des détails supplémentaires
		((TextView) findViewById(R.id.runtime)).setText(result.getRuntime());
		
		EditText overviewTextView = ((EditText) findViewById(R.id.overview));
		overviewTextView.setText(result.getSynopsys());
		overviewTextView.setKeyListener(null);
		overviewTextView.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (view.getId() == R.id.overview) {
					view.getParent().requestDisallowInterceptTouchEvent(true);
					switch (event.getAction() & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_UP:
						view.getParent().requestDisallowInterceptTouchEvent(
								false);
						break;
					}
				}
				return false;
			}

		});

		((TextView) findViewById(R.id.date)).setText(this.mInfos.getDate());
		((TextView) findViewById(R.id.cast)).setText(result.getCast());
		((TextView) findViewById(R.id.author)).setText(result.getDirectors());

		// Mettre l'image du poster, en profitant de la sauvegarde dans le cache
		ImageView poster = (ImageView) findViewById(R.id.media_poster);
		imLoader.DisplayImage(this.mInfos.getOriginalPosterURL(), poster);

		// Trailer trouvé, creer un intent de visionage au click (web)
		if (result.hasTrailer()) {
			TextView trailerView = (TextView) findViewById(R.id.trailer);
			final String trailer_link = result.getTrailer();
			trailerView.setText(Html.fromHtml("<a href=\"" + trailer_link
					+ "\">" + result.getTrailerTitle() + "</a>"));
			trailerView.setClickable(true);
			trailerView.setOnClickListener(new WebIntentListener(trailer_link));

		}
		// Trailer pas trouvé, cacher le layout
		else {
			findViewById(R.id.trailer_layout).setVisibility(View.GONE);

		}
		// Verifier la présence d'un lien web et cacher ou montrer le layout
		// avec sa valeur selon le cas
		String page = result.getWebLink();
		if (page == null) {
			findViewById(R.id.web_layout).setVisibility(View.GONE);
		} else {
			TextView linkView = (TextView) findViewById(R.id.wikilink);
			linkView.setText(Html.fromHtml("<a href=\"" + page + "\">"
					+ (result.hasWiki() ? "Wikipedia page" : page) + "</a>"));
			linkView.setClickable(true);
			linkView.setOnClickListener(new WebIntentListener(page));
		}

		// On va chercher les recommendations ici
		ArrayList<MediaInfos> sim = this.similar; // inutile de le recopier mais
													// bof...
		if (sim == null || sim.isEmpty()) {
			findViewById(R.id.similar_show_list).setVisibility(View.GONE);
			findViewById(R.id.you_may_like).setVisibility(View.GONE);

		}

		// Lorsque qu'on a moins de 3 recommendations, cacher certains layout et
		// le bouton more.
		// Au besoin cacher le layout parent en entier
		else {
			TextView more = (TextView) findViewById(R.id.show_more);

			final ArrayList<MediaInfos> sim_films = new ArrayList<MediaInfos>();
			final ArrayList<MediaInfos> sim_shows = new ArrayList<MediaInfos>();

			for (int i = 0; i < sim.size(); i++) {

				MediaInfos sim_media = sim.get(i);
				if (sim_media.isMovie())
					sim_films.add(sim_media);
				else
					sim_shows.add(sim_media);
			}
			more.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onSearchRequested();
				}

			});

			Collections.shuffle(sim);
			if (sim.size() < 3)
				more.setVisibility(View.INVISIBLE);
			int i = 0;
			ImageView sim_poster, sim_type, sim_rating;
			TextView sim_note, sim_title;
			LinearLayout sim_layout;

			while (i < 3 && i < sim.size()) {
				if (i == 0) {
					sim_poster = (ImageView) findViewById(R.id.sim_poster1);
					sim_note = (TextView) findViewById(R.id.vote_1);
					sim_title = (TextView) findViewById(R.id.sim_title1);
					sim_type = (ImageView) findViewById(R.id.sim_type_1);
					sim_rating = (ImageView) findViewById(R.id.sim_rating_type1);

				} else if (i == 1) {

					sim_poster = (ImageView) findViewById(R.id.sim_poster2);
					sim_note = (TextView) findViewById(R.id.vote_2);
					sim_title = (TextView) findViewById(R.id.sim_title2);
					sim_type = (ImageView) findViewById(R.id.sim_type_2);
					sim_rating = (ImageView) findViewById(R.id.sim_rating_type2);

				} else {
					sim_poster = (ImageView) findViewById(R.id.sim_poster3);
					sim_note = (TextView) findViewById(R.id.vote_3);
					sim_title = (TextView) findViewById(R.id.sim_title3);
					sim_type = (ImageView) findViewById(R.id.sim_type_3);
					sim_rating = (ImageView) findViewById(R.id.sim_rating_type3);

				}
				imLoader.DisplayImage(sim.get(i).getPosterURL(1), sim_poster);
				sim_poster.setOnClickListener(new MediaDetailsIntentListener(
						sim.get(i)));
				sim_note.setText(sim.get(i).getScore() + "%");
				sim_title.setText(sim.get(i).getDetailedTitle() );
				sim_rating.setImageResource(sim.get(i).isMovie() ? R.drawable.user_like : 
																   R.drawable.trakt_love_red);
				sim_type.setImageResource(sim.get(i).isMovie() ? R.drawable.movie :
																 R.drawable.tvshow);
				i++;

			}

			for (int j = i + 1; j < 4; j++) {
				if (j == 1)
					sim_layout = (LinearLayout) findViewById(R.id.similar1);
				else if (j == 2)
					sim_layout = (LinearLayout) findViewById(R.id.similar2);
				else
					sim_layout = (LinearLayout) findViewById(R.id.similar3);
				sim_layout.setVisibility(View.INVISIBLE);
			}

		}

		// La liste des critiques ici, le plus important c'est la position.
		final ArrayList<Critics> c = result.getCriticsList();
		if (c == null || c.isEmpty()) {
			findViewById(R.id.critics).setVisibility(View.GONE);
			findViewById(R.id.fixed_critic_text).setVisibility(View.GONE);

		} else {
			ImageButton next = (ImageButton) findViewById(R.id.next_critic);
			ImageButton previous = (ImageButton) findViewById(R.id.previous_critic);

			String cc = result.getCriticsConsensus();
			if (cc == null || cc.contains("N/A")) {
				findViewById(R.id.c_consensus).setVisibility(View.GONE);
			} else {
				((TextView) findViewById(R.id.c_consensus)).setText("\"" + cc
						+ "\"");

			}
			cf = CriticFragment.newInstance(critics_pos, c.get(critics_pos));
			fm.beginTransaction().add(R.id.comments, cf).commit();

			if (c.size() == 1) {
				next.setVisibility(View.INVISIBLE);
				previous.setVisibility(View.INVISIBLE);
			}
			next.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int pos = (critics_pos + 1) % c.size();
					if (pos != critics_pos)
						showFragment(CriticFragment.newInstance(pos, c.get(pos)));

				}

			});

			previous.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int pos = (critics_pos - c.size()) % c.size() + c.size()
							- 1;
					if (pos != critics_pos)

						showFragment(CriticFragment.newInstance(pos, c.get(pos)));

				}

			});

		}

	}

	private void showFragment(CriticFragment fragment) {
		if (fragment == null)
			return;
		this.critics_pos = fragment.getArguments().getInt("position", 0);

		FragmentTransaction ft = fm.beginTransaction();

		// Fragment animation, ne marche pas
		// ft.setCustomAnimations(android.R.anim.fade_in,
		// android.R.anim.fade_out,android.R.anim.fade_out,
		// android.R.anim.fade_in);

		// Remplacer par le prochain fragment critique
		ft.replace(R.id.comments, fragment);

		// Commit changes.
		ft.commit();
	}

	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putString("origin", "noRequest");
		appData.putString("titre", this.mInfos.getTitle());
		ArrayList<MediaInfos> sim_films = new ArrayList<MediaInfos>();
		ArrayList<MediaInfos> sim_shows = new ArrayList<MediaInfos>();

		for (int i = 0; i < this.similar.size(); i++) {
			MediaInfos sim_media = this.similar.get(i);
			if (sim_media.isMovie())
				sim_films.add(sim_media);
			else
				sim_shows.add(sim_media);
		}
		appData.putParcelableArrayList("movies", sim_films);
		appData.putParcelableArrayList("shows", sim_shows);
		Intent i = new Intent(getApplicationContext(), SearchActivity.class);
		i.putExtra("Similar", appData);
		startActivity(i);
		return true;
	}

	// List des classes
	class LoadMedia extends AsyncTask<MediaInfos, Void, Media> {

		boolean sendReq=true;
		public LoadMedia(boolean sendReq){
			this.sendReq=sendReq;
		}
		@Override
		protected void onPostExecute(Media result) {
			MediaDetails.progressDiag.dismiss();
			TextView error = (TextView) findViewById(R.id.error_text);
			MediaDetails.this.m = result;

			if (result != null) {
				// Resultat trouvé, afficher ce résultat
				error.setVisibility(View.GONE);
				MediaDetails.this.similar = result.getRecommendations();
				MediaDetails.this.showMedia(result);
				findViewById(R.id.view_content).setVisibility(View.VISIBLE);
			} else {
				// Erreur, aucun media trouvé, afficher ce message d'erreur
				error.setText("We are so sorry that we couldn't find your media, The truth is our 4 APIs sucks!");
				error.setBackgroundResource(R.color.error_color);

			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Demarrer le progress dialog pour l'attente
			((TextView) findViewById(R.id.error_text)).setText("");
			MediaDetails.progressDiag = ProgressDialog.show(MediaDetails.this,
					null, "Getting media infos");
		}

		@Override
		protected Media doInBackground(MediaInfos... params) {
			Media media = MediaDetails.this.m;
			if (sendReq) {
				// Aller chercher le media
				try {
					media = new Media(params[0]);
				} catch (Exception e) {
					Log.e("Media", "Impossible d'acceder aux media");
				}
				// Media.afficheMedia(media);
			}
			return media;
		}

	}

	private class MediaDetailsIntentListener implements OnClickListener {
		MediaInfos infos;

		public MediaDetailsIntentListener(MediaInfos m) {
			this.infos = m;
		}

		@Override
		public void onClick(View v) {
			Intent i = new Intent(MediaDetails.this, MediaDetails.class);
			i.putExtra("mediainfo", (Parcelable)infos);
			startActivity(i);
		}
	}

	// Cette classe permet de demarrer une activité web
	private class WebIntentListener implements OnClickListener {
		private String link;

		public WebIntentListener(String s) {
			this.link = s;
		}

		@Override
		public void onClick(View v) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(this.link)));

		}
	}

	public static class CriticFragment extends Fragment {

		public static CriticFragment newInstance(int pos, Critics c) {
			CriticFragment myFragment = new CriticFragment();

			Bundle b = new Bundle();
			b.putInt("position", pos);
			b.putParcelable("critique", c);
			myFragment.setArguments(b);
			return myFragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.critic_fragment, container,
					false);
			Critics c = (Critics) (this.getArguments()
					.getParcelable("critique"));
			TextView aut = (TextView) view.findViewById(R.id.aut);
			aut.setText("- " + c.getAuthor());
			TextView comment = (TextView) view.findViewById(R.id.comment);
			comment.setText(c.getComment());
			
			String c_domain = c.getDomain();
			TextView url = (TextView) view.findViewById(R.id.critics_url);
			if (c_domain == null){
				url.setVisibility(View.GONE);
			}
			else {
				url.setText(Html.fromHtml("<a href=\"" + c.getURL()
						+ "\">" + c_domain + "</a>"));
				url.setClickable(true);
				url.setMovementMethod(LinkMovementMethod.getInstance());
			}
			
			return view;

		}

	}
	

}
