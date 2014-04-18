package com.maclandrol.flibityboop;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends BaseActivity{

	Activity activity;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        setContentView(R.layout.activity_main);
        activity = this;
 	   

    }
    
	
}