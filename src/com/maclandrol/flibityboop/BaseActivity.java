/**
 * IFT2905 : Interface personne machine
 * Projet de session: FlibityBoop.
 * Team: Vincent CABELI, Henry LIM, Pamela MEHANNA, Emmanuel NOUTAHI, Olivier TASTET
 * @author: Emmanuel Noutahi, Vincent Cabeli
 */
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
import android.net.Uri;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.SearchView;
import android.widget.ShareActionProvider;

/**
 * Classe BaseActivity, Superclasse de toutes les activités. 
 * BaseActivity se charge de l'affichage des menus et contient des méthodes utilisables par toutes les activité filles
 */
public class BaseActivity extends FragmentActivity implements OnFocusChangeListener{

	//Share intention
	ShareActionProvider myShareActionProvider=null;
	Intent shareIntent = new Intent(Intent.ACTION_SEND);
	
	MenuItem searchMenuItem=null;

	// Menu bar dans toutes les activités filles
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.main_actions, menu);

		// Déclarer le searchview pour le menu bar et ses actions
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchMenuItem = menu.findItem(R.id.action_search);

		SearchView searchView = (SearchView) searchMenuItem.getActionView();
		if (null != searchView) {
			searchView.setSearchableInfo(searchManager
					.getSearchableInfo(getComponentName()));
			searchView.setIconifiedByDefault(false);
		}
		searchView.setQuery(Utils.getLastQuery(), false);
		searchView.setOnQueryTextFocusChangeListener(this); 
		
		//Action du menu favoris, intent vers l'activité Favoris
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

		//Activer le bouton home de l'action bar		
	   	ActionBar ab = getActionBar();
	    ab.setDisplayHomeAsUpEnabled(true);

	    // Activer l'option de partage
		myShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_item_share).getActionProvider();
		setShareIntent(shareIntent);
		return true;
	    
    }
	
	@Override
	protected void onStop() {
		searchMenuItem.collapseActionView();
		super.onStop();
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {

		if (!hasFocus) {
			searchMenuItem.collapseActionView();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
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

	/*
	 * Ajout d'un média à la base de donnée
	 */
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
		Log.d("database", "taille actuelle = "+sz);
		}
	}

/*
 * Effacer un média de la base de données
 */
	protected void delFromDB(Media media){

		int hash = media.hashCode();
		ContentResolver resolver = this.getContentResolver();
		
		resolver.delete(MediaContentProvider.CONTENT_URI, DBHelperMedia.M_ID
				+ "=?", new String[] { Integer.toString(hash) });
		
		if(media.mediainfos instanceof TraktTVSearch){

			Uri eventsUri=null;
			if (android.os.Build.VERSION.SDK_INT <= 7) {
		         eventsUri = Uri.parse("content://calendar/events");

		     } else {

		         eventsUri = Uri.parse("content://com.android.calendar/events");
		     }

			try{
				resolver.delete(eventsUri,	"calendar_id=? and title=? and eventLocation=? ",	new String[] { String.valueOf(1), media.mediainfos.getTitle(),((TraktTVSearch)media.mediainfos).getNetwork() });
			}catch(Exception e){

				e.printStackTrace();
				Log.d("baseactivity", "Event delete echec");
			}
		}


	}

	/*
	 * Drop database
	 */
	protected boolean emptyDB() {
		ContentResolver resolver = this.getContentResolver();
		resolver.delete(MediaContentProvider.CONTENT_URI, null, null);
		resolver.delete(MediaContentProvider.SHOW_URI, null, null);

		int sz = resolver.query(MediaContentProvider.CONTENT_URI, null, null,
				null, null).getCount();

		return sz == 0;

	}
	
	/*
	 * Partager un text
	 */
	public void share(String text){
		//shareIntent.putExtra(Intent.EXTRA_STREAM, new FileCache(this).saveAndGetBitMapPath(Utils.takeScreenShot(this)));
		shareIntent.putExtra(Intent.EXTRA_TEXT, text);

	}
	
	public void setShareIntent(Intent shareIntent) {
	    if (myShareActionProvider != null) {
	    	myShareActionProvider.setShareIntent(shareIntent);
	    }
	}
	
	/*
	 * Selection de recommendations au hasard dans la liste de favoris
	 */
	protected ArrayList<MediaInfos> randomRecommendations(int n) {

		ArrayList<MediaInfos> recommendations = new ArrayList<MediaInfos>();
		Media media = null;
		
		String[] select = new String[] { DBHelperMedia.M_ID,
				DBHelperMedia.M_INSERT_TIME, DBHelperMedia.M_TITLE,
				DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS, DBHelperMedia.M_SEEN };

		
		Cursor mCursor = getContentResolver().query(
			    MediaContentProvider.CONTENT_URI,  
			    select,                  	       
			    null,			        	       
			    null,       	       			   
			    "RANDOM()");
		
		mCursor.moveToFirst();
		int index = mCursor.getColumnIndex(DBHelperMedia.M_INFOS);
		int fav_count = mCursor.getCount();
		int pos = mCursor.getPosition();
			
		while (pos < fav_count && recommendations.size() < n) {
			
			if (null == mCursor || mCursor.getCount() < 1) {
				//cursor null, pas de match
			    Log.e("baseactivity","null cursor");
			    
			} else {
				
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new ByteArrayInputStream(mCursor.getBlob(index)));
					media= (Media) ois.readObject();
				} catch (Exception e) {
	
				}
				if (recommendations != null) {
	
					// si le media a une liste de recommendations on le retourne
					if (! media.similarMedia.isEmpty())
						recommendations.addAll(media.similarMedia);
						recommendations = checkSeen(recommendations);
					// sinon on essaye le suivant
				}
			}
			
			mCursor.moveToNext();
			pos = mCursor.getPosition();
		}
		
		return recommendations;
	}
	
	/*
	 * Récupérer la liste des derniers favoris ajoutés
	 */
	protected ArrayList<MediaInfos> lastAddedFav(int n) {

		ArrayList<MediaInfos> lastAdded = new ArrayList<MediaInfos>();
		Media media = null;
		
		String[] select = new String[] { DBHelperMedia.M_ID,
				DBHelperMedia.M_INSERT_TIME, DBHelperMedia.M_TITLE,
				DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS, DBHelperMedia.M_SEEN };

		
		Cursor mCursor = getContentResolver().query(
			    MediaContentProvider.CONTENT_URI,   
			    select,                  	        
			    null,							   
			    null,       	       			    
			    DBHelperMedia.M_INSERT_TIME + " DESC"); 
		
		int index = mCursor.getColumnIndex(DBHelperMedia.M_INFOS);

		
		while (mCursor.moveToNext() && lastAdded.size() < n ) {
			
			if (null == mCursor || mCursor.getCount() < 1) {
		     	Log.e("baseactivity","null cursor");
	
			} else {
				
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new ByteArrayInputStream(mCursor.getBlob(index)));
					media= (Media) ois.readObject();
				} catch (Exception e) {
	
				}
				// If there is a media and it is followed, add it to the list
				if (media != null) {
					lastAdded.add(media.mediainfos);					
				}
			}
			
		}
		
		return lastAdded;
		
	}
	
	/*
	 * Récuperer la liste des qui jouent bientôt
	 */
	protected ArrayList<TraktTVSearch> upcomingShows() {

		ArrayList<TraktTVSearch> upcoming = new ArrayList<TraktTVSearch>();
		Media media = null;
		
		String[] select = new String[] { DBHelperMedia.M_ID,
				DBHelperMedia.M_INSERT_TIME, DBHelperMedia.M_TITLE,
				DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS, DBHelperMedia.M_SEEN };
		
		String q_select=DBHelperMedia.M_SHOW + " = ? and "+DBHelperMedia.M_SEEN+ " = ? and " +DBHelperMedia.M_DAY+" not in (?, ?)";
		
		Cursor mCursor = getContentResolver().query(
			    MediaContentProvider.CONTENT_URI,  
			    select,         
			    q_select,	   
			    new String[]{"1", "1", "Ended", "Unknown"},       	       // Only ask for TV shows
			    null);               			   // Retrieve all of them to sort them later
		
		int index = mCursor.getColumnIndex(DBHelperMedia.M_INFOS);

		
		while (mCursor.moveToNext()) {
			
			if (null == mCursor || mCursor.getCount() < 1) {
		     	Log.e("baseactivity","null cursor");
	
			}else {
				
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new ByteArrayInputStream(mCursor.getBlob(index)));
					media= (Media) ois.readObject();
				} catch (Exception e) {
	
				}
				// If there is a media and it is followed, add it to the list
				if (media != null && media.mediainfos instanceof TraktTVSearch ) {
					
					upcoming.add((TraktTVSearch)media.mediainfos);
				}
			}
			
		}
		
		ArrayList<TraktTVSearch> sorted_shows = new ArrayList<TraktTVSearch>();

		// Find the next show to air from now.
		while (sorted_shows.size() < 3 && upcoming.size() >0){
			
			int min_index = 0;
			long min = Long.MAX_VALUE;	
			
			for (int i=0; i<upcoming.size(); i++ ){
				
				if (upcoming.get(i).getTimeToGoMillis() < min){
					Log.d("upcomings", ""+((TraktTVSearch)upcoming.get(i)).ended);
					min_index = i;
					min = ((TraktTVSearch)upcoming.get(i)).getTimeToGoMillis();
				}
				
			}
			sorted_shows.add(upcoming.get(min_index));
			upcoming.remove(min_index);
		}
		
		mCursor.close();
		// Return the 3 next followed shows to air 
		return sorted_shows;
	}
	
	/*
	 * Récuperer la liste des films déja vus
	 */
	protected ArrayList<MediaInfos> checkSeen(ArrayList<MediaInfos> list) {

		Media media = null;
		
		String[] select = new String[] { DBHelperMedia.M_ID,
				DBHelperMedia.M_INSERT_TIME, DBHelperMedia.M_TITLE,
				DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS, DBHelperMedia.M_SEEN };
		
		String q_select = DBHelperMedia.M_SEEN+ " = ?";

		
		Cursor mCursor = getContentResolver().query(
			    MediaContentProvider.CONTENT_URI,  // 
			    select,                  	       // 
			    q_select,	   // 
			    new String[]{"1"},       	       // 
			    null);               			   // 
		
		int index = mCursor.getColumnIndex(DBHelperMedia.M_INFOS);

		
		while (mCursor.moveToNext()) {
			
			if (null == mCursor || mCursor.getCount() < 1) {
		     	Log.e("baseactivity","null cursor");
	
			}else {
				
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new ByteArrayInputStream(mCursor.getBlob(index)));
					media= (Media) ois.readObject();
				} catch (Exception e) {
	
				}
				// If there is a media and it is followed, add it to the list
				if (list.contains(media.mediainfos)) {
					
					list.remove(media.mediainfos);
				}
			}
			
		}
		
		return list;
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
