/**
 * IFT2905 : Interface personne machine
 * Projet de session: FlibityBoop.
 * Team: Vincent CABELI, Henry LIM, Pamela MEHANNA, Emmanuel NOUTAHI, Olivier TASTET
 * @author Emmanuel Noutahi, Vincent Cabeli
 */

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
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Classe MovieFavoriteCursorAdapter, adapter pour les favoris de type Film (voir le fragment correspondant)
 */
public class MovieFavoriteCursorAdapter extends SimpleCursorAdapter {

	Context c;
	ImageLoader im;

	public MovieFavoriteCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.c=context;
		im=new ImageLoader(this.c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		//super.bindView(view, context, cursor);
        
		final int id = cursor.getInt(cursor.getColumnIndex(DBHelperMedia.M_ID));
        final ContentResolver resolver = MovieFavoriteCursorAdapter.this.c.getContentResolver();
        
        ImageButton del = (ImageButton) view.findViewById(R.id.del_fav);
        ImageView poster_fav =(ImageView) view.findViewById(R.id.poster_fav);
        
        TextView date = (TextView)view.findViewById(R.id.date_fav);
        TextView score = (TextView)view.findViewById(R.id.score_fav);
        TextView title = (TextView)view.findViewById(R.id.title_fav);
        ImageView icon_fav =(ImageView) view.findViewById(R.id.type_icon_fav);
		CheckBox seen = (CheckBox) view.findViewById(R.id.seen_fav);

        final int media_pos= cursor.getColumnIndex(DBHelperMedia.M_INFOS);
        
		title.setText(cursor.getString(cursor.getColumnIndex(DBHelperMedia.M_TITLE)));
		icon_fav.setImageResource(cursor.getInt(cursor.getColumnIndex(DBHelperMedia.M_SHOW)) <= 0 ? R.drawable.movie: R.drawable.tvshow);
		boolean checked=cursor.getInt(cursor.getColumnIndex(DBHelperMedia.M_SEEN)) > 0 ? true: false;
		
		seen.setChecked(checked);
        //retrieving media 
        Media media=null;
        MediaInfos m=null;
        try {
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(cursor.getBlob(media_pos)));
			media = (Media) ois.readObject();
		} catch (Exception e) {

		}
		if (media != null) {
			m= media.mediainfos;
			im.DisplayImage(m.getPosterURL(1), poster_fav);
			date.setText(m.getDate());
			score.setText(m.getScore()>0?m.getScore() + "%":"?");
		}
        
		final Media to_send = media;

		//click sur le bouton delete
        del.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                resolver.delete(MediaContentProvider.CONTENT_URI,
                        DBHelperMedia.M_ID + "=?",
                        new String[] { Integer.toString(id) });
                MovieFavoriteCursorAdapter.this.notifyDataSetChanged();

            }

        });

     
        //click sur le poster, on doit avoir les détails du media
		poster_fav.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (to_send != null) {
					Intent i = new Intent(c, MediaDetails.class);
					i.putExtra("mediaComplete", (Parcelable) to_send);
					c.startActivity(i);
				}
			}

		});
		
		//click sur le checkbox seen
        seen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ContentValues val = new ContentValues();
                val.clear();
                val.put(DBHelperMedia.M_SEEN, ((CheckBox)v).isChecked());
                //val.put(DBHelperMedia.M_INSERT_TIME, System.currentTimeMillis());
                resolver.update(MediaContentProvider.CONTENT_URI, val,	DBHelperMedia.M_ID + "=?",new String[] { Integer.toString(id) });
				
			}

        });

	}

}
