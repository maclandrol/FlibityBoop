package com.maclandrol.flibityboop;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.SimpleCursorTreeAdapter;

public class ShowFavoriteCursorAdapter extends SimpleCursorTreeAdapter {

	Context c;
	Cursor cur;
	ImageLoader im;

	static final String[] select = new String[] { DBHelperMedia.M_ID,
		DBHelperMedia.M_INSERT_TIME, DBHelperMedia.M_TITLE,
		DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS, DBHelperMedia.M_SEEN };
	
	public ShowFavoriteCursorAdapter(Context context, Cursor cursor,
			int groupLayout, String[] groupFrom, int[] groupTo,
			int childLayout, String[] childFrom, int[] childTo) {
		super(context, cursor, groupLayout, groupFrom, groupTo, childLayout,
				childFrom, childTo);
		this.c = context;
		im= new ImageLoader(this.c);

	}

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {

		String day = groupCursor.getString(groupCursor
				.getColumnIndex(DBHelperMedia.M_DAY));
		ContentResolver resolver = c.getContentResolver();
		Cursor c= resolver.query(MediaContentProvider.CONTENT_URI,	select, DBHelperMedia.M_DAY + "=? AND "+DBHelperMedia.M_SHOW+"=?",
				new String[] { day, "1" }, null);
		return c.getCount()>0 ?c:null;
		

	}

	@Override
	protected void bindChildView(View view, Context context, Cursor cursor,
			boolean isLastChild) {
		MediaInfos m = null;
		final int id = cursor.getInt(cursor.getColumnIndex(DBHelperMedia.M_ID));

		TextView date = (TextView) view.findViewById(R.id.date_fav);
		TextView score_fav = (TextView) view.findViewById(R.id.score_fav);
		ImageView poster_fav = (ImageView) view.findViewById(R.id.poster_fav);
		TextView title = (TextView) view.findViewById(R.id.title_fav);
		CheckBox seen = (CheckBox) view.findViewById(R.id.seen_fav);
		TextView rel_time = (TextView) view.findViewById(R.id.rel_time);

		ImageView icon = (ImageView) view.findViewById(R.id.type_icon_fav);

		int pos = cursor.getColumnIndex(DBHelperMedia.M_INFOS);
		if (pos > -1) {

			try {
				ObjectInputStream ois = new ObjectInputStream(
						new ByteArrayInputStream(cursor.getBlob(pos)));
				m = (MediaInfos) ois.readObject();
			} catch (Exception e) {

			}
			if (m != null) {
				date.setText(m.getDate());
				score_fav.setText(m.getScore() + "%");
				im.DisplayImage(m.getPosterURL(1), poster_fav);
				if(m instanceof TraktTVSearch){
					rel_time.setText(((TraktTVSearch) m).getTimeUntilNextAirTime());
				}

			}
		}
		title.setText(cursor.getString(cursor
				.getColumnIndex(DBHelperMedia.M_TITLE)));
		icon.setImageResource(cursor.getInt(cursor
				.getColumnIndex(DBHelperMedia.M_SHOW)) <= 0 ? R.drawable.movie
				: R.drawable.tvshow);
		boolean s = cursor.getInt(cursor.getColumnIndex(DBHelperMedia.M_SEEN)) > 0 ? true
				: false;
		seen.setChecked(s);
        final MediaInfos to_send = m;

        final ContentResolver resolver = ShowFavoriteCursorAdapter.this.c
                .getContentResolver();
        ImageButton del = (ImageButton) view.findViewById(R.id.del_fav);
        del.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                resolver.delete(MediaContentProvider.CONTENT_URI,
                        DBHelperMedia.M_ID + "=?",
                        new String[] { Integer.toString(id) });
                ShowFavoriteCursorAdapter.this.notifyDataSetChanged(true); 
           
                
            }

        });


        poster_fav.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(c, MediaDetails.class);
                i.putExtra("media", (Parcelable) to_send);
                c.startActivity(i);

            }

        });


	}


	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View v= super.getChildView(groupPosition, childPosition, isLastChild,
				convertView, parent);
		
	       final TextView title = (TextView) v.findViewById(R.id.title_fav);
	        CheckBox seen = (CheckBox) v.findViewById(R.id.seen_fav);
	        final ContentResolver resolver = ShowFavoriteCursorAdapter.this.c.getContentResolver();
	  	    
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
	        return v;

		}

	protected void bindGroupView(View view, Context context, Cursor cursor,
			boolean isExpanded) {
		CheckedTextView ct = (CheckedTextView) view.findViewById(R.id.ctView1);
		ct.setChecked(isExpanded);
		if (!isExpanded)
			ct.setTextColor(c.getResources().getColor(R.color.cat_title));
		else
			ct.setTextColor(c.getResources().getColor(R.color.black));
		ct.setText(cursor.getString(cursor.getColumnIndex(DBHelperMedia.M_DAY)));
		
		//ContentResolver resolver = ShowFavoriteCursorAdapter.this.c.getContentResolver();
        //cur=resolver.query(MediaContentProvider.SHOW_URI, null, null, null, null);
        //ShowFavoriteCursorAdapter.this.notifyDataSetChanged(true); 


	}


}
