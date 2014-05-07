package com.maclandrol.flibityboop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

public class FileCache {
    
    private File cacheDir;
    
    public FileCache(Context context){
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"LazyList");
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }
    
    public File getFile(String url){
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename=String.valueOf(url.hashCode());
        //Another possible solution (thanks to grantland)
        //String filename = URLEncoder.encode(url);
        File f = new File(cacheDir, filename);
        return f;
        
    }
    
    public void clear(){
        File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        for(File f:files)
            f.delete();
    }
    
    public Uri saveAndGetBitMapPath(Bitmap b){
    	File imageFile = new File(cacheDir,"xxMediaSharexx"+ ".png");
    	FileOutputStream fileOutPutStream;
		try {
			fileOutPutStream = new FileOutputStream(imageFile);
	    	b.compress(Bitmap.CompressFormat.PNG, 80, fileOutPutStream);

	    	fileOutPutStream.flush();
	    	fileOutPutStream.close();
	    	return Uri.parse("file://" + imageFile.getAbsolutePath());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
    }

}