package com.maclandrol.flibityboop;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Parcelable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MovieFavoriteCursorAdapter extends SimpleCursorAdapter {

	Context c;
	ImageLoader im;

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		View view= super.getView(arg0, arg1, arg2);
		final ContentResolver resolver = MovieFavoriteCursorAdapter.this.c.getContentResolver();
		CheckBox seen = (CheckBox) view.findViewById(R.id.seen_fav);
	     final TextView title = (TextView) view.findViewById(R.id.title_fav);

        seen.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                ContentValues val = new ContentValues();
                val.clear();
                val.put(DBHelperMedia.M_SEEN, isChecked);
                val.put(DBHelperMedia.M_INSERT_TIME, System.currentTimeMillis());
                resolver.update(MediaContentProvider.CONTENT_URI, val,	DBHelperMedia.M_TITLE + "=?", new String[] { title.getText().toString() });

            }

        });
		return view;
	}

	public MovieFavoriteCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.c=context;
		im=new ImageLoader(this.c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
        
		final int id = cursor.getInt(cursor.getColumnIndex(DBHelperMedia.M_ID));
        final ContentResolver resolver = MovieFavoriteCursorAdapter.this.c
                .getContentResolver();
        ImageButton del = (ImageButton) view.findViewById(R.id.del_fav);
        ImageView poster_fav =(ImageView) view.findViewById(R.id.poster_fav);
        del.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                resolver.delete(MediaContentProvider.CONTENT_URI,
                        DBHelperMedia.M_ID + "=?",
                        new String[] { Integer.toString(id) });
                MovieFavoriteCursorAdapter.this.notifyDataSetChanged();

            }

        });

        
        final int media_pos= cursor.getColumnIndex(DBHelperMedia.M_INFOS);
        
        MediaInfos m=null;
        try {
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(cursor.getBlob(media_pos)));
			m = (MediaInfos) ois.readObject();
		} catch (Exception e) {

		}
		if (m != null) {
			im.DisplayImage(m.getPosterURL(1), poster_fav);
		}
		final MediaInfos to_send = m;

		poster_fav.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (to_send != null) {
					Intent i = new Intent(c, MediaDetails.class);
					i.putExtra("media", (Parcelable) to_send);
					c.startActivity(i);
				}
			}

		});

	}

}