package com.maclandrol.flibityboop;

import java.util.ArrayList;
import java.util.Collections;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends BaseActivity implements OnClickListener {

	SectionListAdapter drawerAdapter;
	ListView drawer;
	DrawerLayout drawerLayout;
	ActionBarDrawerToggle drawerToggle;
	Button searchButton, db_add;
	ImageView star;
	EditText searchText;
	TextView titre;
	TextView date;
	TextView score;
	CheckBox seen;
	MediaInfos media;
	Item [] items = {new SectionItem("Movies"), new ListItem("UpComming"),  new ListItem("In theather"), new SectionItem("TVShow"), new ListItem("On Air"),new ListItem("Airing Today")};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		searchButton = (Button) findViewById(R.id.search_button);
		db_add = (Button) findViewById(R.id.fav_db_add);
		
		star = (ImageView) findViewById(R.id.star);
		titre = (TextView)findViewById(R.id.fav_ex_titre);
		score = (TextView)findViewById(R.id.fav_ex_score);
		date = (TextView)findViewById(R.id.fav_ex_date);
		seen = (CheckBox)findViewById(R.id.fav_ex_seen);
		searchText = (EditText) findViewById(R.id.search_bar);
		searchButton.setOnClickListener(this);
		drawer = (ListView)findViewById(R.id.drawer);
		db_add.setOnClickListener(this);
		
		star.setOnClickListener(this);
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
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_button:
			new SearchMyMedia().execute(searchText.getText().toString());
			break;
		case R.id.star:
			this.emptyDB();
			break;
		case R.id.fav_db_add:
			addToDB(this.media, seen.isChecked());
			break;
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
	
	private class SearchMyMedia extends AsyncTask<String, String, MediaInfos> {
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}
		
		protected MediaInfos doInBackground(String... params) {
			RottenTomatoes RT = new RottenTomatoes();
			TraktTV TTV = new TraktTV();
			String query = params[0];

			ArrayList<RTSearch> a = null;
			ArrayList<TraktTVSearch> b = null;
			try {
				a = RT.searchMovies(query, 2, 1);
			} catch (Exception e) {
				Log.e("asyncError", e.getMessage());
			}
			try {
				b = TTV.searchShow(query, 2);
			} catch (Exception e) {
				Log.e("asyncError", e.getMessage());
			}
			ArrayList <? extends MediaInfos> medias= Utils.entrelace(a, b);
			Collections.shuffle(medias);
			media= medias.size()>0?medias.get(0):null;
			return media;
		}
		
		protected void onPostExecute(MediaInfos m) {
			setProgressBarIndeterminateVisibility(false);
			if( m == null ) {
				((TextView)findViewById(R.id.fav_db_text)).setText("No Result Found");
				MainActivity.this.seen.setVisibility(View.INVISIBLE);
				MainActivity.this.db_add.setVisibility(View.INVISIBLE);
				MainActivity.this.titre.setVisibility(View.INVISIBLE);
				MainActivity.this.date.setVisibility(View.INVISIBLE);
				MainActivity.this.score.setVisibility(View.INVISIBLE);

			}
			else{
				((TextView)findViewById(R.id.fav_db_text)).setText("First seach result (after list shuffle), press search to shuffle again with the same query");

				MainActivity.this.seen.setVisibility(View.VISIBLE);
				MainActivity.this.db_add.setVisibility(View.VISIBLE);
				MainActivity.this.titre.setVisibility(View.VISIBLE);
				MainActivity.this.date.setVisibility(View.VISIBLE);
				MainActivity.this.score.setVisibility(View.VISIBLE);
				seen.setChecked(false);
				MainActivity.this.titre.setText(media.getTitle());
				MainActivity.this.date.setText(media.getDate());
				MainActivity.this.score.setText(media.getScore()+"%");

			}



		}

	}	
	

}