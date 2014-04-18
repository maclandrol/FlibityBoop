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
import com.maclandrol.flibityboop.API.MediaType;

public class MainActivity2 extends Activity {
	private ListView myList;
	private ArrayList<? extends MediaInfos> filminfosList;
	private ArrayList<? extends MediaInfos> showinfosList;
	private ArrayList<? extends MediaInfos> mediainfosList;
	private MediaAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.activity_main);
//		myList = (ListView)findViewById(R.id.mainList);
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
			MainActivity2.this.setProgressBarIndeterminateVisibility(true);

		}
		
		protected ArrayList<? extends MediaInfos> doInBackground(String... params) {

			RottenTomatoes RT = new RottenTomatoes();
			TraktTV TTV = new TraktTV();
			
			ArrayList<RTSearch> a = null;
			ArrayList<TraktTVSearch> b = null;			
			try{
				a = RT.searchMovies(params[0], 30, 1);				
			}catch(Exception e){
				Log.e("asyncError", e.getMessage());
			}
			try{
				b = TTV.searchShow(params[0],30);
			}catch(Exception e){
				Log.e("asyncError", e.getMessage());
			}
			filminfosList = a;
			showinfosList = b;
			mediainfosList = Utils.entrelace(a,b);
			
			return mediainfosList;
		}
		

		protected void onProgressUpdate(String... s) {
	
			
		}
		

		protected void onPostExecute(ArrayList<? extends MediaInfos> c) {
			MainActivity2.this.setProgressBarIndeterminateVisibility(false);

			if( c == null) {
				Toast.makeText(MainActivity2.this, "Error", Toast.LENGTH_SHORT).show();
				return;
			}
			
			mAdapter= new MediaAdapter(getApplicationContext(),mediainfosList);
			myList.setAdapter(mAdapter);			
		}

	}
    
}