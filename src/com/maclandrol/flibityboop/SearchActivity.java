package com.maclandrol.flibityboop;

import java.util.ArrayList;

import com.maclandrol.flibityboop.API.MediaType;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Build;
import android.provider.SearchRecentSuggestions;

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
	private String previous_query = "";
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
			if (! previous_query.equalsIgnoreCase(new_query)){ 
				
				previous_query = new_query;
				new DownloadLoginTask().execute(previous_query);
				
				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,SearchSuggestionProvider.AUTHORITY, 
						SearchSuggestionProvider.MODE);
				suggestions.saveRecentQuery(previous_query, null);
			}
		}

	}
	@Override
	public void onDestroy() {
	    Log.d("intent", "onDestroy()");
	    super.onDestroy();
	}
	
	private class ListOnItemClick implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> adapter, View view,
				int position, long id) {
			Intent i = new Intent(SearchActivity.this, MediaDetails.class);
			i.putExtra("media", mediainfosList.get(position));
			startActivity(i);

		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  setContentView(R.layout.activity_search);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = this;
	    setContentView(R.layout.activity_search);
	    myList = (ListView)findViewById(R.id.searchList);
	    myList.setOnItemClickListener(new ListOnItemClick());
		
	    toggleProgress = (ProgressBar) findViewById(R.id.searchToggleProgress2);
	   
		progress = new ProgressDialog(activity);
		progress.setCancelable(false);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		Toast.makeText(getApplicationContext(), "onCreate()", Toast.LENGTH_LONG).show();
		
		
		noResultTextView = (TextView) activity.findViewById(R.id.noResult);
		
		Intent i = getIntent();

		if (i != null && !Intent.ACTION_SEARCH.equals(i.getAction())) {
			filminfosList = i.getParcelableArrayListExtra("films");
			showinfosList = i.getParcelableArrayListExtra("shows");
			mediainfosList = Utils.entrelace(filminfosList, showinfosList);
			type = (MediaType) i.getExtras().get("media type");
			displaySearchResult();
		}

		
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
	
	private void displaySearchResult(){

		System.out.println("Displaysearchresult");
		ArrayList<? extends MediaInfos> listToDisplay = null;
		noResultTextView.setVisibility(View.INVISIBLE);
	    toggleProgress.setVisibility(View.VISIBLE);
		
		if (includeMovies && ! includeShows)
			listToDisplay = filminfosList;
		
		else if (includeShows && ! includeMovies)
			listToDisplay = showinfosList;
	
		else if (includeShows &&  includeMovies){
			listToDisplay = mediainfosList;
		}	
		
		else {
			myList.setAdapter(null);
		}
		
		if(listToDisplay != null && listToDisplay.isEmpty()){
			myList.setAdapter(null);
			//myList.setVisibility(View.INVISIBLE);
			String noResult = (getString(R.string.noResult)) + " \""  + previous_query + "\" in " + 
			((includeMovies) ? ((includeShows) ? " movies and TV shows" : " movies") :
				((includeShows) ? " TV shows" : "")) ;
			
			noResultTextView.setText(noResult);
			noResultTextView.setVisibility(View.VISIBLE);
		}
		
		else{
			mAdapter= new MediaAdapter(getApplicationContext(),listToDisplay);
			myList.setAdapter(mAdapter);	
		}	

	    toggleProgress.setVisibility(View.INVISIBLE);
		progress.dismiss();
	}
	
	public void checkMediaType(){
		
		
		
		
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

		Log.d("work","PrexExecute()");
		
		progress.show();
	}
	
	protected Void doInBackground(String... params) {

		RottenTomatoes RT = new RottenTomatoes();
		TraktTV TTV = new TraktTV();
		query = params[0];
		
		ArrayList<RTSearch> a = null;
		ArrayList<TraktTVSearch> b = null;			
		try{
			a = RT.searchMovies(query, nResultsRT, 1);				
		}catch(Exception e){
			Log.e("asyncError", e.getMessage());
		}
		try{
			b = TTV.searchShow(query,nResultsTTV);
		}catch(Exception e){
			Log.e("asyncError", e.getMessage());
		}
		filminfosList = a;
		showinfosList = b;
		Log.d("async","making mediainfoslists");
		mediainfosList = Utils.entrelace(filminfosList, showinfosList);
		
		return null;
	}
	
	protected void onPostExecute(Void result){
		
		displaySearchResult();
		
	}
	
	
}

}
