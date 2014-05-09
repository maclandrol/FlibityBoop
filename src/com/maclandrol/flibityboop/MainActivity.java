package com.maclandrol.flibityboop;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements OnClickListener {

	SectionListAdapter drawerAdapter;
	ListView drawer;
	TextView noRecommendations, noConnection;
	DrawerLayout drawerLayout;
	ActionBarDrawerToggle drawerToggle;
	Button rdmButton;
	ArrayList<MediaInfos> sim = null;
	
	
	ImageLoader imLoader = null;

	MediaInfos media;
	Item [] items = {new SectionItem("Media"), new ListItem("Popular"),new ListItem("TopRated"), 
			new SectionItem("Movies"), new ListItem("UpComing"),  new ListItem("In theather"), 
			new SectionItem("TVShow"), new ListItem("On Air"),new ListItem("Airing Today")};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
		
		rdmButton = (Button) findViewById(R.id.accueil_rdm);
		rdmButton.setOnClickListener(this);
		noRecommendations = (TextView) findViewById(R.id.accueil_empty);
		
		noConnection = (TextView) findViewById(R.id.no_connection);
		noConnection.setVisibility(isNetworkConnected()? View.GONE : View.VISIBLE);
		
		
		drawer = (ListView)findViewById(R.id.drawer);
		drawer.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d("sectionlist", items[arg2].toString());
				if(!items[arg2].isSection()){
					Intent i = new Intent(getApplicationContext(), SearchActivity.class);
					i.putExtra("drawer", true);
					i.putExtra(SearchManager.QUERY, "");
					i.putExtra("type", arg2);
					i.setAction(Intent.ACTION_SEARCH);
					startActivity(i);
				}
				
			}
			
		});
		

		
		imLoader = new ImageLoader(getApplicationContext());
		
		drawerAdapter = new SectionListAdapter(getApplicationContext(), items);
		drawer.setAdapter(drawerAdapter);
		
			
		drawerLayout = (DrawerLayout) findViewById(R.id.container);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer_am, // L'icône indiquant qu'un drawer existe
                R.string.drawer_open,
                R.string.drawer_close
                ) {

            /**
             * 
             * Lorsque le drawer se referme, cette méthode est appelée.
             * On y change le titre pour le titre de l'activité et on
             * réinitialise le menu dans l'action bar.
             * 
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu();
            }

            /**
             * 
             * Lorsque le drawer est complètement ouvert, cette méthode est appelée.
             * On y change le titre, normalement pour le nom de l'application entière
             * et on réinitialise le menu dans l'action bar.
             * 
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle("FlibityBoop");
                invalidateOptionsMenu();
            }
            
            
        };

        // On associe le listener ci-haut au DrawerLayout
        drawerLayout.setDrawerListener(drawerToggle);

        // Ces deux commandes permettent d'afficher le bouton
        // du drawer dans l'action bar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        // On first run
        
        if (firstrun){
        	
	        new AlertDialog.Builder(this).setTitle("Beefore you start").setMessage("Thank you for using FlibityBoop! \n \n"
	        		+ "Since some of the assets provided by the media databases can be quite big"
	        		+ " we recommend using Wi-Fi over mobile data. For any complaints"
	        		+ " please contact Henry Lim, Montreal.\n \nHave fun!").setNeutralButton("OK", null).show();
	        
	        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("firstrun", false)
	        .commit();
	    }
        
        //showMedia();
        
        
	}


	@Override
	protected void onResume() {
		super.onResume();
		noConnection.setVisibility(isNetworkConnected()? View.GONE : View.VISIBLE);
		showMedia();
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.accueil_rdm:
			showRecommendations();
			break;
		}
	}
	
	public void quickSearch(View view){

		String genre = (String) view.getContentDescription();
		Log.d("genre","genre : " + genre);
		
		Intent i = new Intent(getApplicationContext(), SearchActivity.class);
		i.putExtra("from_genre", true);
		i.putExtra(SearchManager.QUERY, "");
		i.putExtra("genre", genre);
		i.setAction(Intent.ACTION_SEARCH);
		startActivity(i);

	}
	
	public void showMedia(){
		
		showRecommendations();
		showUpcomingShows();
		showLastAddedFav();
		return;
	}
	
	
	public void showUpcomingShows(){
		
		ArrayList<TraktTVSearch> upcoming = upcomingShows();
		LinearLayout upcoming_layout = (LinearLayout) findViewById(R.id.accueil_upcoming);
		upcoming_layout.setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.accueil_string_upcoming_show1)).setVisibility(View.GONE);
		((TextView) findViewById(R.id.accueil_string_upcoming_show2)).setVisibility(View.GONE);
		((TextView) findViewById(R.id.accueil_string_upcoming_show3)).setVisibility(View.GONE);
		
        if (upcoming.isEmpty()){
        	
        	upcoming_layout.setVisibility(View.GONE);
			return;
        }
        
        
        int i =0;
        TextView upcoming_show;
        
        while (i < 3 && i < upcoming.size()) {

        	if (i == 0) {
				upcoming_show = (TextView) findViewById(R.id.accueil_string_upcoming_show1);
			
        	} else if (i == 1) {
				upcoming_show = (TextView) findViewById(R.id.accueil_string_upcoming_show2);

			} else {
				upcoming_show = (TextView) findViewById(R.id.accueil_string_upcoming_show3);
			}
        	TraktTVSearch show = upcoming.get(i);
        	upcoming_show.setText( "- " + show.getTitle() + " will air " +  show.getAirDay()+" at "+ show.getAirTime()+
        			" on " + show.getNetwork());
        	upcoming_show.setVisibility(View.VISIBLE);
        	i++;

		}
        
	}
	
	public void showRecommendations(){
		
		sim = randomRecommendations(3);
		noRecommendations.setVisibility(View.GONE);
		rdmButton.setVisibility(View.VISIBLE);
		((LinearLayout) findViewById(R.id.accueil_similar3)).setVisibility(View.VISIBLE);
		((LinearLayout) findViewById(R.id.accueil_similar2)).setVisibility(View.VISIBLE);
		((LinearLayout) findViewById(R.id.accueil_similar3)).setVisibility(View.VISIBLE);
		
		if (sim.isEmpty()){
			noRecommendations.setVisibility(View.VISIBLE);
			rdmButton.setVisibility(View.GONE);
			return;
		}
	
		else {
				
			Collections.shuffle(sim);
			int i = 0;
			ImageView sim_poster, sim_type, sim_rating;
			TextView sim_note, sim_title;
			LinearLayout sim_layout;

			while (i < 3 && i < sim.size()) {
				if (i == 0) {
					sim_poster = (ImageView) findViewById(R.id.accueil_sim_poster1);
					sim_note = (TextView) findViewById(R.id.accueil_vote_1);
					sim_title = (TextView) findViewById(R.id.accueil_sim_title1);
					sim_type = (ImageView) findViewById(R.id.accueil_sim_type_1);
					sim_rating = (ImageView) findViewById(R.id.accueil_sim_rating_type1);

				} else if (i == 1) {

					sim_poster = (ImageView) findViewById(R.id.accueil_sim_poster2);
					sim_note = (TextView) findViewById(R.id.accueil_vote_2);
					sim_title = (TextView) findViewById(R.id.accueil_sim_title2);
					sim_type = (ImageView) findViewById(R.id.accueil_sim_type_2);
					sim_rating = (ImageView) findViewById(R.id.accueil_sim_rating_type2);

				} else {
					sim_poster = (ImageView) findViewById(R.id.accueil_sim_poster3);
					sim_note = (TextView) findViewById(R.id.accueil_vote_3);
					sim_title = (TextView) findViewById(R.id.accueil_sim_title3);
					sim_type = (ImageView) findViewById(R.id.accueil_sim_type_3);
					sim_rating = (ImageView) findViewById(R.id.accueil_sim_rating_type3);

				}
				imLoader.DisplayImage(sim.get(i).getPosterURL(1), sim_poster);
				sim_poster.setOnClickListener(new IntentListener(
						sim.get(i)));
				sim_note.setText(sim.get(i).getScore()>0?sim.get(i).getScore() + "%":"?");
				sim_title.setText(sim.get(i).getDetailedTitle() );
				sim_rating.setImageResource(sim.get(i).isMovie() ? R.drawable.user_like : 
																   R.drawable.trakt_love_red);
				sim_type.setImageResource(sim.get(i).isMovie() ? R.drawable.movie :
																 R.drawable.tvshow);
				i++;

			}

			for (int j = i + 1; j < 4; j++) {
				if (j == 1)
					sim_layout = (LinearLayout) findViewById(R.id.accueil_similar1);
				else if (j == 2)
					sim_layout = (LinearLayout) findViewById(R.id.accueil_similar2);
				else
					sim_layout = (LinearLayout) findViewById(R.id.accueil_similar3);
				sim_layout.setVisibility(View.INVISIBLE);
			}
		}
		
		
		
		
		
	}
	
	public void showLastAddedFav(){
		
		ArrayList<MediaInfos> last_added = lastAddedFav(3);
		LinearLayout last_added_layout = (LinearLayout) findViewById(R.id.accueil_last_added_list);
		TextView last_added_string = (TextView) findViewById(R.id.accueil_string_2);

		
		if (last_added.isEmpty()){
			last_added_string.setVisibility(View.GONE);
			last_added_layout.setVisibility(View.GONE);
			return;
		}
		
		
		else {
			last_added_layout.setVisibility(View.VISIBLE);
			last_added_string.setVisibility(View.VISIBLE);	

			((LinearLayout) findViewById(R.id.accueil_last1)).setVisibility(View.VISIBLE);
			((LinearLayout) findViewById(R.id.accueil_last2)).setVisibility(View.VISIBLE);
			((LinearLayout) findViewById(R.id.accueil_last3)).setVisibility(View.VISIBLE);
			
			int i = 0;
			ImageView last_poster, last_type, last_rating;
			TextView last_note, last_title;
			LinearLayout last_layout;

			while (i < 3 && i < last_added.size()) {
				if (i == 0) {
					last_poster = (ImageView) findViewById(R.id.accueil_last_poster1);
					last_note = (TextView) findViewById(R.id.accueil_last_vote_1);
					last_title = (TextView) findViewById(R.id.accueil_last_title1);
					last_type = (ImageView) findViewById(R.id.accueil_last_type_1);
					last_rating = (ImageView) findViewById(R.id.accueil_last_rating_type1);

				} else if (i == 1) {

					last_poster = (ImageView) findViewById(R.id.accueil_last_poster2);
					last_note = (TextView) findViewById(R.id.accueil_last_vote_2);
					last_title = (TextView) findViewById(R.id.accueil_last_title2);
					last_type = (ImageView) findViewById(R.id.accueil_last_type_2);
					last_rating = (ImageView) findViewById(R.id.accueil_last_rating_type2);

				} else {
					last_poster = (ImageView) findViewById(R.id.accueil_last_poster3);
					last_note = (TextView) findViewById(R.id.accueil_last_vote_3);
					last_title = (TextView) findViewById(R.id.accueil_last_title3);
					last_type = (ImageView) findViewById(R.id.accueil_last_type_3);
					last_rating = (ImageView) findViewById(R.id.accueil_last_rating_type3);

				}
				imLoader.DisplayImage(last_added.get(i).getPosterURL(1), last_poster);
				last_poster.setOnClickListener(new IntentListener(
						last_added.get(i)));
				last_note.setText(last_added.get(i).getScore()>0?last_added.get(i).getScore() + "%":"?");
				last_title.setText(last_added.get(i).getDetailedTitle() );
				last_rating.setImageResource(last_added.get(i).isMovie() ? R.drawable.user_like : 
																   R.drawable.trakt_love_red);
				last_type.setImageResource(last_added.get(i).isMovie() ? R.drawable.movie :
																 R.drawable.tvshow);
				i++;

			}

			for (int j = i + 1; j < 4; j++) {
				if (j == 1)
					last_layout = (LinearLayout) findViewById(R.id.accueil_last1);
				else if (j == 2)
					last_layout = (LinearLayout) findViewById(R.id.accueil_last2);
				else
					last_layout = (LinearLayout) findViewById(R.id.accueil_last3);
				last_layout.setVisibility(View.INVISIBLE);
			}
		}
		
		
		
		
		
	}
	
	
	private class IntentListener implements OnClickListener {
		MediaInfos infos;

		public IntentListener(MediaInfos m) {
			this.infos = m;
		}

		@Override
		public void onClick(View v) {
			Intent i = new Intent(MainActivity.this, MediaDetails.class);
			i.putExtra("mediainfo", (Parcelable)infos);
			startActivity(i);
		}
	}
	
	
	/**
     * 
     * Cette méthode est appelée lorsqu'on appelle 
     * invalidateOptionsMenu. On y détermine alors
     * l'état du drawer et on cache ou affiche les éléments
     * selon le cas.
     * 
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawer);
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        menu.findItem(R.id.action_search).collapseActionView();
        return super.onPrepareOptionsMenu(menu);
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Si drawerToggle.onOptionsItemSelected retourne
		// true, c'est que l'item faisait partie de la barre
		// titre contrôlant le drawer et on peut retourner
		// immédiatement.
		if (drawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // On synchronise l'état du drawer avec l'état de l'activité restorée
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
     // On synchronise la configuration du drawer avec celle de l'activité restorée
        drawerToggle.onConfigurationChanged(newConfig);
    }
	

}