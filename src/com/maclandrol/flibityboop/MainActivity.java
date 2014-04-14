package com.maclandrol.flibityboop;

import java.util.ArrayList;

import android.app.Activity;
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

public class MainActivity extends Activity {
	private ListView myList;
	private ArrayList<? extends MediaInfos> mediainfosList;
	private MediaAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.activity_main);
		myList = (ListView)findViewById(R.id.mainList);
		Button search = (Button)findViewById(R.id.search_button);
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d("click", "buttonclick");
				new DownloadLoginTask().execute(((TextView)findViewById(R.id.search_bar)).getText().toString());

			}
		});

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    private class DownloadLoginTask extends AsyncTask<String, String, ArrayList<? extends MediaInfos>> {
	
		protected void onPreExecute() {
			MainActivity.this.setProgressBarIndeterminateVisibility(true);

		}
		
		protected ArrayList<? extends MediaInfos> doInBackground(String... params) {

			TheMovieDB tmdb= new TheMovieDB();
			ArrayList<TMDBSearch> a=null;
			try{
				a= tmdb.searchMedia(MediaType.Any, params[0], 2);
			}catch(Exception e){
				Log.e("asyncError", e.getMessage());
			}
			return a;
		}
		

		protected void onProgressUpdate(String... s) {
	
			
		}
		

		protected void onPostExecute(ArrayList<? extends MediaInfos> a) {
			MainActivity.this.setProgressBarIndeterminateVisibility(false);

			if( a == null ) {
				Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
				return;
			}
			mediainfosList=a;
			mAdapter= new MediaAdapter(getApplicationContext(),mediainfosList);
			myList.setAdapter(mAdapter);

			
		}

	}	
    
}
