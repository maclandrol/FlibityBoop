package com.maclandrol.flibityboop;

import java.util.ArrayList;
import java.util.Collections;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements OnClickListener {

	Button searchButton, db_add;
	ImageView star;
	EditText searchText;
	TextView titre;
	TextView date;
	TextView score;
	CheckBox seen;
	MediaInfos media;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		searchButton = (Button) findViewById(R.id.search_button);
		db_add = (Button) findViewById(R.id.fav_db_add);

		star = (ImageView) findViewById(R.id.star);
		titre = (TextView)findViewById(R.id.fav_ex_titre);
		score = (TextView)findViewById(R.id.fav_ex_score);
		date = (TextView)findViewById(R.id.fav_ex_date);
		seen = (CheckBox)findViewById(R.id.fav_ex_seen);
		searchText = (EditText) findViewById(R.id.search_bar);
		searchButton.setOnClickListener(this);
		db_add.setOnClickListener(this);

		star.setOnClickListener(this);
		int sz = getContentResolver().query(MediaContentProvider.CONTENT_URI,null, null, null, null).getCount();
		Toast.makeText(getApplicationContext(), "(" + sz + " elements)",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_button:
			new SearchMyMedia().execute(searchText.getText().toString());
			break;
		case R.id.star:
			this.emptyDB();
			break;
		case R.id.fav_db_add:
			addToDB(this.media, seen.isChecked());
			break;
		}
			
			
	}
	
	private class SearchMyMedia extends AsyncTask<String, String, MediaInfos> {
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}
		
		protected MediaInfos doInBackground(String... params) {
			RottenTomatoes RT = new RottenTomatoes();
			TraktTV TTV = new TraktTV();
			String query = params[0];

			ArrayList<RTSearch> a = null;
			ArrayList<TraktTVSearch> b = null;
			try {
				a = RT.searchMovies(query, 2, 1);
			} catch (Exception e) {
				Log.e("asyncError", e.getMessage());
			}
			try {
				b = TTV.searchShow(query, 2);
			} catch (Exception e) {
				Log.e("asyncError", e.getMessage());
			}
			ArrayList <? extends MediaInfos> medias= Utils.entrelace(a, b);
			Collections.shuffle(medias);
			media= medias.size()>0?medias.get(0):null;
			return media;
		}
		
		protected void onPostExecute(MediaInfos m) {
			setProgressBarIndeterminateVisibility(false);
			if( m == null ) {
				((TextView)findViewById(R.id.fav_db_text)).setText("No Result Found");
				MainActivity.this.seen.setVisibility(View.INVISIBLE);
				MainActivity.this.db_add.setVisibility(View.INVISIBLE);
				MainActivity.this.titre.setVisibility(View.INVISIBLE);
				MainActivity.this.date.setVisibility(View.INVISIBLE);
				MainActivity.this.score.setVisibility(View.INVISIBLE);

			}
			else{
				((TextView)findViewById(R.id.fav_db_text)).setText("First seach result (after list shuffle), press search to shuffle again with the same query");

				MainActivity.this.seen.setVisibility(View.VISIBLE);
				MainActivity.this.db_add.setVisibility(View.VISIBLE);
				MainActivity.this.titre.setVisibility(View.VISIBLE);
				MainActivity.this.date.setVisibility(View.VISIBLE);
				MainActivity.this.score.setVisibility(View.VISIBLE);
				seen.setChecked(false);
				MainActivity.this.titre.setText(media.getTitle());
				MainActivity.this.date.setText(media.getDate());
				MainActivity.this.score.setText(media.getScore()+"%");

			}



		}

	}	
	

}