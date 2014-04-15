package com.maclandrol.flibityboop;

import com.example.flibityboop.R;
import com.example.flibityboop.R.layout;
import com.example.flibityboop.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MediaDetails extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_details);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.media_details, menu);
		return true;
	}

}
