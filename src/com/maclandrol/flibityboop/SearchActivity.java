package com.maclandrol.flibityboop;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.flibityboop.R;
import com.maclandrol.flibityboop.API.MediaType;

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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.activity_main);
		myList = (ListView)findViewById(R.id.mainList);
		
		Intent i = getIntent();
		if (i != null){
			filminfosList = i.getParcelableArrayListExtra("films");
			showinfosList = i.getParcelableArrayListExtra("shows");
			mediainfosList = i.getParcelableArrayListExtra("all");
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
	public Intent getIntent() {
		// TODO Auto-generated method stub
		return super.getIntent();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	
}
