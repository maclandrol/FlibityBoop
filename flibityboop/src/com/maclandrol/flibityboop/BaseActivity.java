package com.maclandrol.flibityboop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.database.Cursor;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.SearchView;
import android.widget.Toast;


public class BaseActivity extends FragmentActivity{

	
	@Override
    // This is to set up the same action bar on all the activities.
	public boolean onCreateOptionsMenu(Menu menu) {
		
	   // Retrieves the action menu.
	   getMenuInflater().inflate(R.menu.main_actions, menu);

	   // Declares the SearchView for the search bar.
	   SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	   SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
	   if (null != searchView) {
		   searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	       searchView.setIconifiedByDefault(false);
	    }	    
	   searchView.setQuery(Utils.getLastQuery(), false);
	   
	   menu.findItem(R.id.action_favorites).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				
				Intent in = new Intent(getApplicationContext(), FavoriteActivity.class);
				startActivity(in);;
				return true;
			}
		});
	   
	   	ActionBar ab = getActionBar();
	    ab.setHomeButtonEnabled(true);
	   
       return true;
	    
    }

	/**
	 * 
	 * Comme l'activité SettingsActivity cause
	 * un arrêt partiel de cette activité, lorsque
	 * nous retrouvons le focus, il faut vérifier
	 * si les préférences ont changé.
	 * 
	 */
	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
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
        		break;
        		
        	case R.id.action_clear_cache:
        		ImageLoader im = new ImageLoader(this);
        		im.clearCache();
        		break;
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
			    "RANDOM() LIMIT 1");               // The sort order for the returned rows
		
		mCursor.moveToFirst();
		int index = mCursor.getColumnIndex(DBHelperMedia.M_INFOS);
		
		
		if (null == mCursor) {
		    /*
		     * Insert code here to handle the error. Be sure not to use the cursor! You may want to
		     * call android.util.Log.e() to log this error.
		     *
		     */
			Toast.makeText(getApplicationContext(), "null cursor",Toast.LENGTH_LONG).show();
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

				return media;
			}
		}
		
		return media;
	}

}
