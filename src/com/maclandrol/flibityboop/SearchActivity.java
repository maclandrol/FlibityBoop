package com.maclandrol.flibityboop;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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

import com.maclandrol.flibityboop.API.MediaType;

public class SearchActivity extends BaseActivity {

	private ListView myList;
	private ArrayList<? extends MediaInfos> filminfosList;
	private ArrayList<? extends MediaInfos> showinfosList;
	private ArrayList<? extends MediaInfos> mediainfosList;

	// Number of results returned by APIs
	private int nResultsRT = 10;
	private int nResultsTTV = 10;

	private boolean includeMovies = true;
	private boolean includeShows = true;

	private MediaType type = MediaType.Any;
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
			String new_query = intent.getStringExtra(SearchManager.QUERY);
	 
	
			 if (!previous_query.equalsIgnoreCase(new_query)) {

				previous_query = new_query;
				new DownloadLoginTask().execute(previous_query);

				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
						this, SearchSuggestionProvider.AUTHORITY,
						SearchSuggestionProvider.MODE);
				suggestions.saveRecentQuery(previous_query, null);
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
				type = MediaType.Any;
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
			i.putExtra("media",(Parcelable)to_send);
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
		setContentView(R.layout.activity_search);
		myList = (ListView) findViewById(R.id.searchList);
		myList.setOnItemClickListener(new ListOnItemClick());
		toggleProgress = (ProgressBar) findViewById(R.id.searchToggleProgress2);
		progress = new ProgressDialog(activity);
		progress.setCancelable(false);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
			listToDisplay=null;
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
				a = RT.searchMovies(query, nResultsRT, 1);
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

}
