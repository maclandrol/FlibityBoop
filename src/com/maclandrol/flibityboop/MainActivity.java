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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.flibityboop.R;
import com.maclandrol.flibityboop.API.MediaType;

public class MainActivity extends Activity {
	private class ListOnItemClick implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				Intent i = new Intent(MainActivity.this, MediaDetails.class);
				startActivity(i);

			
		}
	}
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
		myList = (ListView)findViewById(R.id.mainList);
		Button search = (Button)findViewById(R.id.search_button);
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d("click", "buttonclick");
				new DownloadLoginTask().execute(((TextView)findViewById(R.id.search_bar)).getText().toString());

			}
		});
		myList.setOnItemClickListener(new ListOnItemClick());


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

			RottenTomatoes RT = new RottenTomatoes();
			TraktTV TTV = new TraktTV();
			
			ArrayList<RTSearch> a = null;
			ArrayList<TraktTVSearch> b = null;			
			try{
				a = RT.searchMovies(params[0], 5, 1);				
			}catch(Exception e){
				Log.e("asyncError", e.getMessage());
			}
			try{
				b = TTV.searchShow(params[0],5);
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
			MainActivity.this.setProgressBarIndeterminateVisibility(false);

			if( c == null) {
				Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
				return;
			}
			
			/*Intent i = new Intent(getApplicationContext(), SearchActivity.class);
			i.putParcelableArrayListExtra("films",filminfosList);
			i.putParcelableArrayListExtra("show",showinfosList);
			i.putParcelableArrayListExtra("all",mediainfosList);
			i.putExtra("media type",API.MediaType.Any );
			
			startActivity(i);
			*/
			
			mAdapter= new MediaAdapter(getApplicationContext(),mediainfosList);
			myList.setAdapter(mAdapter);			

			}

	}
    
}