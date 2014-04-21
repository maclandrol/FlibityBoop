package com.maclandrol.flibityboop;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
	   
	   menu.findItem(R.id.action_favorites).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				
				Intent in = new Intent(getApplicationContext(), FavoriteActivity.class);
				startActivity(in);;
				return true;
			}
		});
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

protected void addToDB(MediaInfos media, boolean seen){
		
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

	protected void delFromDB(MediaInfos mediaInfos) {

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

}
