package com.maclandrol.flibityboop;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements OnClickListener {

	SectionListAdapter drawerAdapter;
	ListView drawer;
	TextView noRecommendations;
	DrawerLayout drawerLayout;
	ActionBarDrawerToggle drawerToggle;
	Button rdmButton;
	ArrayList<MediaInfos> sim = null;
	ImageLoader imLoader = null;

	MediaInfos media;
	Item [] items = {new SectionItem("Movies"), new ListItem("UpComming"),  new ListItem("In theather"), new SectionItem("TVShow"), new ListItem("On Air"),new ListItem("Airing Today")};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		rdmButton = (Button) findViewById(R.id.accueil_rdm);
		rdmButton.setOnClickListener(this);
		noRecommendations = (TextView) findViewById(R.id.accueil_empty);
		
		drawer = (ListView)findViewById(R.id.drawer);
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
        showMedia();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.accueil_rdm:
			showMedia();
			break;
		}
	}

	public void showMedia(){
		
		Media media = randomFav();
		if (media == null){
			noRecommendations.setVisibility(View.VISIBLE);
			return;
		}

		sim = media.getRecommendations();
		
		if (sim == null || sim.isEmpty()) {
			noRecommendations.setVisibility(View.VISIBLE);
			return;
		}
		
		// Lorsque qu'on a moins de 3 recommendations, cacher certains layout et
		// le bouton more.
		// Au besoin cacher le layout parent en entier
		else {
			noRecommendations.setVisibility(View.GONE);
			
			final ArrayList<MediaInfos> sim_films = new ArrayList<MediaInfos>();
			final ArrayList<MediaInfos> sim_shows = new ArrayList<MediaInfos>();

			for (int i = 0; i < sim.size(); i++) {

				MediaInfos sim_media = sim.get(i);
				if (sim_media.isMovie())
					sim_films.add(sim_media);
				else
					sim_shows.add(sim_media);
			}
			
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
					sim_layout = (LinearLayout) findViewById(R.id.accueil_similar1);
				else if (j == 2)
					sim_layout = (LinearLayout) findViewById(R.id.accueil_similar2);
				else
					sim_layout = (LinearLayout) findViewById(R.id.accueil_similar3);
				sim_layout.setVisibility(View.INVISIBLE);
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