package com.maclandrol.flibityboop;

import java.util.ArrayList;

import com.maclandrol.flibityboop.API.MediaType;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.os.Build;

public class SearchActivity extends Activity {
	
	private ListView myList;
	private ArrayList<? extends MediaInfos> filminfosList;
	private ArrayList<? extends MediaInfos> showinfosList;
	private ArrayList<? extends MediaInfos> mediainfosList;
	private MediaType type;
	private MediaAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
	    setContentView(R.layout.activity_search);
		myList = (ListView)findViewById(R.id.searchList);
			
		Intent i = getIntent();

		if (i != null){
			filminfosList = i.getParcelableArrayListExtra("films");
			showinfosList = i.getParcelableArrayListExtra("shows");
			mediainfosList = Utils.entrelace(filminfosList, showinfosList);
			type = (MediaType) i.getExtras().get("media type");
		}

		if(type == MediaType.TVShow)
			mAdapter= new MediaAdapter(getApplicationContext(),showinfosList);
			
		else if (type == MediaType.Movies)
			mAdapter= new MediaAdapter(getApplicationContext(),filminfosList);
				
		else
			mAdapter= new MediaAdapter(getApplicationContext(),mediainfosList);
	
		myList.setAdapter(mAdapter);
	    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_search,
					container, false);
			return rootView;
		}
	}

}
