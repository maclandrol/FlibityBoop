package com.maclandrol.flibityboop;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;


public class BaseActivity extends Activity{

	
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
	   
	   return true;
	    
    }

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
        	case R.id.action_settings:
        		//start activity settings
        		break;
        	case R.id.action_favorites:
        		//start activity favorites
        		break;
		}
	
		return super.onOptionsItemSelected(item);
	}


}

