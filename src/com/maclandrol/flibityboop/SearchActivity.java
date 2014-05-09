package com.maclandrol.flibityboop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.maclandrol.flibityboop.API.MediaType;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SearchActivity extends BaseActivity {

	private ListView myList;
	private ArrayList<? extends MediaInfos> filminfosList;
	private ArrayList<? extends MediaInfos> showinfosList;
	private ArrayList<? extends MediaInfos> mediainfosList;
	final static int TV_ON_AIR=7;
	final static int TV_AIRING_TODAY=8;
	final static int MOVIE_UPCOMMING=4;
	final static int MOVIE_IN_THEATHER=5;
	final static int POPULAR=1;
	final static int TOP_RATED=2;
	// Number of results returned by APIs
	private int nResultsRT;
	private int nResultsTTV;
	private int maxPage;

	private boolean includeMovies = true;
	private boolean includeShows = true;

	private MediaAdapter mAdapter;
	private ProgressDialog progress;
	private ProgressBar toggleProgress;
	private Activity activity;
	private String previous_query = "", search_title="";
	private TextView noResultTextView;

	public void onNewIntent(Intent intent) {
		noResultTextView.setVisibility(View.INVISIBLE);
		myList.setVisibility(View.VISIBLE);

		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			boolean from_main = intent.getBooleanExtra("drawer", false);
			boolean from_genre = intent.getBooleanExtra("from_genre", false);
			String new_query = intent.getStringExtra(SearchManager.QUERY);

			if(from_main){
				previous_query = new_query;
				Utils.setLastQuery(new_query);
				int type=intent.getIntExtra("type",0);
				new DrawerAsyncTask(type).execute();
			}
			else if (from_genre){
				previous_query = new_query;
				Utils.setLastQuery(new_query);
				String genre = intent.getStringExtra("genre");
				new GenreAsyncTask(genre).execute();
			}
			else{
	
			 if (!previous_query.equalsIgnoreCase(new_query)) {

				previous_query = new_query;
				Utils.setLastQuery(new_query);

				new DownloadLoginTask().execute(previous_query);

				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
						this, SearchSuggestionProvider.AUTHORITY,
						SearchSuggestionProvider.MODE);
				suggestions.saveRecentQuery(previous_query, null);
			}
			}
		} else {
			Bundle b = intent.getBundleExtra("Similar");

			String origin = null;
			try {
				origin = b.getString("origin");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if ("noRequest".equals(origin)) {

				filminfosList = b.getParcelableArrayList("movies");
				showinfosList = b.getParcelableArrayList("shows");
				mediainfosList = Utils.entrelace(filminfosList, showinfosList);
				Log.d("search", ""+mediainfosList.size());
				this.search_title = "Recommendations for \""
						+ b.getString("titre") + "\"";
				displaySearchResult();
			}
		}

	}

	private class ListOnItemClick implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> adapter, View view,
				int position, long id) {
			Intent i = new Intent(SearchActivity.this, MediaDetails.class);
			MediaInfos to_send=null;
			if(includeMovies && includeShows)
				to_send=mediainfosList.get(position);
			else if(includeMovies)
				to_send=filminfosList.get(position);
			else
				to_send=showinfosList.get(position);
			i.putExtra("mediainfo",(Parcelable)to_send);
			startActivity(i);

		}
	}

	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("origin","noRequest");
		outState.putParcelableArrayList("movies", filminfosList);
		outState.putParcelableArrayList("shows", showinfosList);
		outState.putBoolean("isSaved", true);
		outState.putString("search_title", this.search_title);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		nResultsRT= Integer.parseInt(sharedPref.getString("max_req", "20"));
		maxPage=Integer.parseInt(sharedPref.getString("maxPage", "1"));
		nResultsTTV= Integer.parseInt(sharedPref.getString("max_req", "20"));
		setContentView(R.layout.activity_search);
		myList = (ListView) findViewById(R.id.searchList);
		myList.setOnItemClickListener(new ListOnItemClick());
		toggleProgress = (ProgressBar) findViewById(R.id.searchToggleProgress2);
		progress = new ProgressDialog(activity);
		progress.setCancelable(true);
		progress.setMessage("Searching for your media");
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        finish();
		    	dialog.dismiss();
		    }
		});
		
		noResultTextView = (TextView) activity.findViewById(R.id.noResult);
		
		if(savedInstanceState!=null && savedInstanceState.getBoolean("search_title")){
			this.filminfosList=savedInstanceState.getParcelableArrayList("movies");
			this.showinfosList=savedInstanceState.getParcelableArrayList("shows");
			this.search_title=savedInstanceState.getString("search_title");
			this.displaySearchResult();
		}
		
		else{
			Intent i = getIntent();
			onNewIntent(i);
		}
		
	}

	@Override
	public void onDestroy() {
		//Toast.makeText(getApplicationContext(), "onCreate()", Toast.LENGTH_LONG).show();
		super.onDestroy();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	private void displaySearchResult() {

		ArrayList<? extends MediaInfos> listToDisplay = null;
		noResultTextView.setVisibility(View.INVISIBLE);
		toggleProgress.setVisibility(View.VISIBLE);

		if (includeMovies && !includeShows)
			listToDisplay = filminfosList;

		else if (includeShows && !includeMovies)
			listToDisplay = showinfosList;

		else if (includeShows && includeMovies) {
			listToDisplay = mediainfosList;
		}

		else {
			myList.setAdapter(null);
			toggleProgress.setVisibility(View.INVISIBLE);
			return;
			}

		if (listToDisplay == null || listToDisplay.isEmpty()) {
			myList.setAdapter(null);
			// myList.setVisibility(View.INVISIBLE);
			String noResult = (getString(R.string.noResult))
					+ " \""
					+ previous_query
					+ "\" "
					+ (includeMovies ? ((includeShows) ? " in movies and TV shows"
							: "in movies")
							: ((includeShows) ? "in TV shows" : ""));

			noResultTextView.setText(noResult);
			noResultTextView.setVisibility(View.VISIBLE);
		}

		else {
			mAdapter = new MediaAdapter(getApplicationContext(), listToDisplay);
			myList.setAdapter(mAdapter);
		}

		toggleProgress.setVisibility(View.INVISIBLE);
		((TextView)findViewById(R.id.search_text)).setText(this.search_title);
		progress.dismiss();
	}



	// Movie Toggle
	public void onMovieToggleClicked(View view) {
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			includeMovies = true;
		} else {
			includeMovies = false;
		}
		displaySearchResult();
	}

	// Show toggle
	public void onShowToggleClicked(View view) {
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			includeShows = true;
		} else {
			includeShows = false;
		}
		displaySearchResult();
	}

	protected class DownloadLoginTask extends AsyncTask<String, String, Void> {

		String query;

		protected void onPreExecute() {

			Log.d("work", "PrexExecute()");

			progress.show();
		}

		protected Void doInBackground(String... params) {

			RottenTomatoes RT = new RottenTomatoes();
			TraktTV TTV = new TraktTV();
			query = params[0];

			ArrayList<RTSearch> a = null;
			ArrayList<TraktTVSearch> b = null;
			try {
				a = RT.searchMovies(query, nResultsRT, maxPage);
			} catch (Exception e) {
				Log.e("asyncError", e.getMessage());
			}
			try {
				b = TTV.searchShow(query, nResultsTTV);
			} catch (Exception e) {
				Log.e("asyncError", e.getMessage());
			}
			filminfosList = a;
			showinfosList = b;
			Log.d("async", "making mediainfoslists");
			mediainfosList = Utils.entrelace(filminfosList, showinfosList);

			return null;
		}

		protected void onPostExecute(Void result) {
			search_title="Search result for \""+query+"\"";
			displaySearchResult();

		}

	}
	
	protected class GenreAsyncTask extends AsyncTask<String, String, Void> {

		String genre="";
		
		public GenreAsyncTask (String genre){
			this.genre = genre;
		}
		
		protected void onPreExecute() {
			progress.show();
		}
		
		protected Void doInBackground(String... params) {

			TheMovieDB tmdb=new TheMovieDB();

			ArrayList<TMDBSearch> a = null;
			
			try {
				a = tmdb.getMoviesByGenres(genre, false, maxPage);

			} catch (Exception e) {
				Log.e("asyncError", e.getMessage());
			}
			
			filminfosList = a;
			mediainfosList = a;

			return null;
		}
		

		protected void onPostExecute(Void result) {
			search_title = genre.substring(0, 1).toUpperCase() + genre.substring(1) + " movies";
			displaySearchResult();

		}

	}
	
	protected class DrawerAsyncTask extends AsyncTask<Void, String, Void> {

		int queryType;
		String message="";
		boolean movie=true;
		boolean mixte=false;
		public DrawerAsyncTask (int queryType){
			this.queryType=queryType;
		}
		
		protected void onPreExecute() {

			Log.d("work", "PrexExecute()");
			progress.show();
		}

		protected Void doInBackground(Void... params) {

			TheMovieDB tmdb=new TheMovieDB();

			ArrayList<TMDBSearch> a = null;
			try {
				switch(queryType){
				case SearchActivity.MOVIE_IN_THEATHER:
					a= tmdb.getInTheaterMovies(maxPage);
					this.message="Movies in theather ";
					movie=true;
					break;
				case SearchActivity.MOVIE_UPCOMMING:
					a= tmdb.getUpcomingMovies(maxPage);
					this.message="Upcoming movies";
					movie=true;
					break;
				case SearchActivity.TV_AIRING_TODAY:
					a= tmdb.getAiringToday(maxPage);
					this.message="TV Show Airing today";
					movie=false;
					break;
					
				case SearchActivity.TV_ON_AIR:
					this.message="TVShow on air";
					a=tmdb.getOnAirTV(maxPage);
					movie=false;
					break;
				case SearchActivity.POPULAR:
					this.message="Really populars now";
					a=tmdb.getPopularMedia(MediaType.Any, maxPage);
					mixte=true;
					break;
					
				case SearchActivity.TOP_RATED:
					this.message="Top rated media";
					a=tmdb.getTopRatedMedia(MediaType.Any, maxPage);
					mixte=true;
					break;
					
				}
				
			} catch (Exception e) {
				Log.e("asyncError", e.getMessage());
		
			}
			
			if (mixte) {
				filminfosList = new ArrayList<TMDBSearch>(a);
				showinfosList = new ArrayList<TMDBSearch>(a);
				Set<MediaInfos> removeShow = new HashSet<MediaInfos>();
				Set<MediaInfos> removeFilm = new HashSet<MediaInfos>();
				for (MediaInfos m : a) {
					if (m.isShow()){
						removeShow.add(m);
					}
					else
						removeFilm.add(m);
				}
				filminfosList.removeAll(removeShow);
				showinfosList.removeAll(removeFilm);
				mediainfosList = Utils.entrelace(filminfosList, showinfosList);
			} else {
				mediainfosList = a;

				if (movie) {
					filminfosList = a;
				} else
					showinfosList = a;
			}
			Log.d("async", "making mediainfoslists");
			return null;
		}

		protected void onPostExecute(Void result) {
			search_title=this.message;
			displaySearchResult();

		}


	}


}
