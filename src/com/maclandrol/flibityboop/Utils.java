package com.maclandrol.flibityboop;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

public class Utils {
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

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