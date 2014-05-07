package com.maclandrol.flibityboop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.SearchView;
import android.widget.ShareActionProvider;
import android.widget.Toast;


public class BaseActivity extends FragmentActivity{

	ShareActionProvider myShareActionProvider=null;
	Intent shareIntent = new Intent(Intent.ACTION_SEND);

    // This is to set up the same action bar on all the activities.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
	   // Retrieves the action menu.
		getMenuInflater().inflate(R.menu.main_actions, menu);

		// Declares the SearchView for the search bar.
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
				.getActionView();
		if (null != searchView) {
			searchView.setSearchableInfo(searchManager
					.getSearchableInfo(getComponentName()));
			searchView.setIconifiedByDefault(false);
		}
		searchView.setQuery(Utils.getLastQuery(), false);
		
	
		
		menu.findItem(R.id.action_favorites).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {

						Intent in = new Intent(getApplicationContext(),
								FavoriteActivity.class);
						startActivity(in);
						;
						return true;
					}
		});
	   
	   	ActionBar ab = getActionBar();
	    ab.setHomeButtonEnabled(true);
		//share intent
		myShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_item_share).getActionProvider();
		setShareIntent(shareIntent);
		
		
		
       return true;
	    
    }
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
        	case R.id.action_settings:
        		Intent i = new Intent(this, SettingActivity.class);	
				startActivity(i);	
				return true;

        	case android.R.id.home:
        	    Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
        	    startActivity(homeIntent);
        	    break;
        	    
        	case R.id.action_clear_recent:
        		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
        		        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
        		suggestions.clearHistory();
        		return true;
        		
        	case R.id.action_clear_cache:
        		ImageLoader im = new ImageLoader(this);
        		im.clearCache();
        		return true;
       
        		
        		
		}		
		return super.onOptionsItemSelected(item);
	}

protected void addToDB(Media media, boolean seen){
		
		ContentResolver resolver = getContentResolver();
		ContentValues val = new ContentValues();
		if(media!=null){
		val.clear();
		val.put(DBHelperMedia.M_SEEN, seen);
		ByteArrayOutputStream bos =null;
		try {
			bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(media);
			oos.flush();
			oos.close();
			bos.close();
			val.put(DBHelperMedia.M_INFOS, bos.toByteArray());

		} catch (Exception e) {
			e.printStackTrace();
		}
		resolver.insert(MediaContentProvider.CONTENT_URI, val);
	
		int sz = resolver.query(MediaContentProvider.CONTENT_URI, null, null, null, null).getCount();
		Toast.makeText(getApplicationContext(), "AddToDB (" + sz + " elements)",Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(getApplicationContext(), "not AddedToDB",Toast.LENGTH_LONG).show();

		}
	}


	protected void delFromDB(Media mediaInfos) {

		int hash = mediaInfos.hashCode();
		ContentResolver resolver = this.getContentResolver();
		;
		resolver.delete(MediaContentProvider.CONTENT_URI, DBHelperMedia.M_ID
				+ "=?", new String[] { Integer.toString(hash) });
		Toast.makeText(getApplicationContext(), "Media deleted",
				Toast.LENGTH_LONG).show();

	}

	protected boolean emptyDB() {
		ContentResolver resolver = this.getContentResolver();
		resolver.delete(MediaContentProvider.CONTENT_URI, null, null);
		resolver.delete(MediaContentProvider.SHOW_URI, null, null);

		int sz = resolver.query(MediaContentProvider.CONTENT_URI, null, null,
				null, null).getCount();
		Toast.makeText(getApplicationContext(),
				"All row deleted (" + sz + " elements)", Toast.LENGTH_LONG)
				.show();

		return sz == 0;

	}
	
	public void share(String text){
		//shareIntent.putExtra(Intent.EXTRA_STREAM, new FileCache(this).saveAndGetBitMapPath(Utils.takeScreenShot(this)));
		shareIntent.putExtra(Intent.EXTRA_TEXT, text);

	}
	
	public void setShareIntent(Intent shareIntent) {
	    if (myShareActionProvider != null) {
	    	myShareActionProvider.setShareIntent(shareIntent);
	    }
	}
	
	
	protected Media randomFav() {

		Media media = null;
		
		String[] select = new String[] { DBHelperMedia.M_ID,
				DBHelperMedia.M_INSERT_TIME, DBHelperMedia.M_TITLE,
				DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS, DBHelperMedia.M_SEEN };

		
		Cursor mCursor = getContentResolver().query(
			    MediaContentProvider.CONTENT_URI,  // The content URI of the words table
			    select,                  	       // The columns to return for each row
			    null,			        	       // Either null, or the word the user entered
			    null,       	       			   // Either empty, or the string the user entered
			    "RANDOM()");              		   // The sort order for the returned rows
		
		mCursor.moveToFirst();
		int index = mCursor.getColumnIndex(DBHelperMedia.M_INFOS);
		int fav_count = mCursor.getCount();
		int pos = mCursor.getPosition();
			
		while (media == null && pos < fav_count) {
			
			if (null == mCursor) {
			    // Insert code here to handle the error. Be sure not to use the cursor!
			     	Log.e("baseactivity","null cursor");
			     	// If the Cursor is empty, the provider found no matches
			} else if (mCursor.getCount() < 1) {
	
			    /*
			     * Insert code here to notify the user that the search was unsuccessful. This isn't necessarily
			     * an error. You may want to offer the user the option to insert a new row, or re-type the
			     * search term.
			     */
	
			} else {
			    // Insert code here to do something with the results
				
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new ByteArrayInputStream(mCursor.getBlob(index)));
					media= (Media) ois.readObject();
				} catch (Exception e) {
	
				}
				if (media != null) {
	
					// si le media a une liste de recommendations on le retourne
					if (! media.similarMedia.isEmpty())
						return media;
					// sinon on essaye le suivant
					else
						media = null;
				}
			}
			
			mCursor.moveToNext();
			pos = mCursor.getPosition();
		}
		
		return media;
	}
	
	protected ArrayList<TraktTVSearch> upcomingShows() {

		ArrayList<MediaInfos> upcoming = new ArrayList<MediaInfos>();
		Media media = null;
		
		String[] select = new String[] { DBHelperMedia.M_ID,
				DBHelperMedia.M_INSERT_TIME, DBHelperMedia.M_TITLE,
				DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS, DBHelperMedia.M_SEEN };

		
		Cursor mCursor = getContentResolver().query(
			    MediaContentProvider.CONTENT_URI,  // 
			    select,                  	       // 
			    DBHelperMedia.M_SHOW + " = ?",	   // 
			    new String[]{"1"},       	       // Only ask for TV shows
			    null);               			   // Retrieve all of them to sort them later
		
		int index = mCursor.getColumnIndex(DBHelperMedia.M_INFOS);

		
		while (mCursor.moveToNext()) {
			
			if (null == mCursor) {
			    // Insert code here to handle the error. Be sure not to use the cursor!
			     	Log.e("baseactivity","null cursor");
			     	// If the Cursor is empty, the provider found no matches
			} else if (mCursor.getCount() < 1) {
	
			    /*
			     * Insert code here to notify the user that the search was unsuccessful. This isn't necessarily
			     * an error. You may want to offer the user the option to insert a new row, or re-type the
			     * search term.
			     */
	
			} else {
			    // Insert code here to do something with the results
				
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new ByteArrayInputStream(mCursor.getBlob(index)));
					media= (Media) ois.readObject();
				} catch (Exception e) {
	
				}
				// If there is a media and it is followed, add it to the list
				if (media != null && mCursor.getString(mCursor.getColumnIndex(DBHelperMedia.M_SEEN)).equals("1")) {
					
					upcoming.add(media.mediainfos);
				}
			}
			
		}
		
		ArrayList<TraktTVSearch> sorted_shows = new ArrayList<TraktTVSearch>();

		// Find the next show to air from now.
		while (sorted_shows.size() < 3 && upcoming.size() > 0){
			
			int min_index = 0;
			long min = Long.MAX_VALUE;	
			
			for (int i=0; i<upcoming.size(); i++ ){
				
				if ( ! ((TraktTVSearch)upcoming.get(i)).ended && 
						((TraktTVSearch)upcoming.get(i)).getTimeToGoMillis() < min){
					
					min_index = i;
					min = ((TraktTVSearch)upcoming.get(i)).getTimeToGoMillis();
				}
				
			}
			sorted_shows.add( (TraktTVSearch)upcoming.get(min_index) );
			upcoming.remove(min_index);
		}
		
		// Return the 3 next followed shows to air 
		return sorted_shows;
	}

	protected boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if (ni == null) {
		   // There are no active networks.
		   return false;
		  } else
		   return true;
	}
	
	

}
