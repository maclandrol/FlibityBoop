package com.maclandrol.flibityboop;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;

public class Utils {
	
	public static String last_query = "";
	
	public static void setLastQuery(String s){
		
		last_query = s;
	}
	
	public static String getLastQuery(){
		
		return last_query;
	}
	
	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	// Fusionne deux arraylists de mediainfos dans leur ordre original en les
	// alternant.
	public static ArrayList<MediaInfos> entrelace(
			ArrayList<? extends MediaInfos> a, ArrayList<? extends MediaInfos> b) {

		ArrayList<MediaInfos> entrelacee = new ArrayList<MediaInfos>();
		if (a != null && b != null) {
			int max = Math.max(a.size(), b.size());

			for (int i = 0; i < max; i++) {
				if (i < a.size())
					entrelacee.add(a.get(i));
				if (i < b.size())
					entrelacee.add(b.get(i));
			}
		} else {

			try {
				entrelacee.addAll(a);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			try {

				entrelacee.addAll(b);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

		}

		return entrelacee;
	}

	
	//Take a screenshot from activity
	public static Bitmap takeScreenShot(Activity activity)
	{
	    View act_view = activity.findViewById(android.R.id.content);
	    act_view.setDrawingCacheEnabled(true); //enable cache drawing
	    act_view.buildDrawingCache();
	    Bitmap b1 = act_view.getDrawingCache(); //retrive the cache builded 
	    Rect frame = new Rect();
	    activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
	    int statusBarHeight = frame.top;

	    int width=  activity.getWindowManager().getDefaultDisplay().getWidth();
	    int height=activity.getWindowManager().getDefaultDisplay().getWidth();
	    /*int width= displaymetrics.widthPixels;
	    int height = displaymetrics.heightPixels;
*/
	    Bitmap b = Bitmap.createBitmap(b1);//, 0, statusBarHeight, width, height  - statusBarHeight);
	    act_view.destroyDrawingCache();
	    return b;
	}
	
	

}