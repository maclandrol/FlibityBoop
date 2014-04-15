package com.maclandrol.flibityboop;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.flibityboop.R;
import com.maclandrol.flibityboop.API.MediaType;


public class Utility {
	
	// Fusionne deux arraylists de mediainfos dans leur ordre original en les alternant.
	public static ArrayList<MediaInfos> entrelace(ArrayList<? extends MediaInfos> a,
													ArrayList<? extends MediaInfos> b) {
		
		ArrayList<MediaInfos> entrelacee = new ArrayList<MediaInfos>();
		int max = Math.max(a.size(), b.size());
				
		for(int i=0; i<max; i++){
			if(i < a.size())
				entrelacee.add(a.get(i));
			if(i < b.size())
				entrelacee.add(b.get(i));				
		}
		
		return entrelacee;
	}
	
	
	// Rapetisse une image Bitmap a une nouvelle hauteur en fonction de son contexte.
	// stackoverflow.com/questions/3528735/
	public static Bitmap MinimiseBitmap(Bitmap bm, int hauteur, Context context){
		
		final float d = context.getResources().getDisplayMetrics().density;        
		
		int h = (int) (hauteur*d);
		int w = (int) (h * bm.getWidth()/((double)bm.getHeight()));
		
		bm = Bitmap.createScaledBitmap(bm, w, h, true);
		
		return bm;
	}
	
	
}


class BitMapSync extends AsyncTask<String, String, Bitmap> {
	
	ImageView view = null;
	
	public BitMapSync(ImageView v){
		
		this.view = v;
	}
	
	@Override
	protected Bitmap doInBackground(String... arg0) {

		Bitmap webposter = API.getBitmapPoster(arg0[0]);
		return webposter;
	}

	protected void onPostExecute(Bitmap bm){
		
		if (bm != null && view != null)
			view.setImageBitmap(bm);
		
	}
}	
