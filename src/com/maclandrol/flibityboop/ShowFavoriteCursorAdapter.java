package com.maclandrol.flibityboop;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.TimeZone;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Reminders;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ShowFavoriteCursorAdapter extends SimpleCursorTreeAdapter {

	Context c;
	Cursor cur;
	Cursor calendar_cur;
    Calendar cal = Calendar.getInstance();  
    TimeZone timeZone = TimeZone.getDefault();
	ImageLoader im;
	Uri eventsUri, remindersUri;
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
		if (android.os.Build.VERSION.SDK_INT <= 7) {
			remindersUri =Uri.parse("content://calendar/reminders");
	         eventsUri = Uri.parse("content://calendar/events");
	     } else {

	         eventsUri = Uri.parse("content://com.android.calendar/events");
	         remindersUri = Uri.parse("content://com.android.calendar/events");
	     }
		 
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
		Media media = null;
		MediaInfos m =null;
		final int id = cursor.getInt(cursor.getColumnIndex(DBHelperMedia.M_ID));

		TextView date = (TextView) view.findViewById(R.id.date_fav);
		TextView score_fav = (TextView) view.findViewById(R.id.score_fav);
		ImageView poster_fav = (ImageView) view.findViewById(R.id.poster_fav);
		TextView title = (TextView) view.findViewById(R.id.title_fav);
		CheckBox seen = (CheckBox) view.findViewById(R.id.seen_fav);
		TextView rel_time = (TextView) view.findViewById(R.id.rel_time);
		boolean s = cursor.getInt(cursor.getColumnIndex(DBHelperMedia.M_SEEN)) > 0 ? true
				: false;
		ImageView icon = (ImageView) view.findViewById(R.id.type_icon_fav);

		int pos = cursor.getColumnIndex(DBHelperMedia.M_INFOS);
		if (pos > -1) {

			try {
				ObjectInputStream ois = new ObjectInputStream(
						new ByteArrayInputStream(cursor.getBlob(pos)));
				media = (Media) ois.readObject();
			} catch (Exception e) {

			}
			if (media != null) {
				m = media.mediainfos;
				date.setText(m.getDate());
				score_fav.setText(m.getScore() + "%");
				im.DisplayImage(m.getPosterURL(1), poster_fav);
				if(m instanceof TraktTVSearch){
					rel_time.setText(((TraktTVSearch) m).getTimeUntilNextAirTime());
					if(s){
						addEvent((TraktTVSearch) m);
					}
				}

			}
		}
		title.setText(cursor.getString(cursor
				.getColumnIndex(DBHelperMedia.M_TITLE)));
		icon.setImageResource(cursor.getInt(cursor
				.getColumnIndex(DBHelperMedia.M_SHOW)) <= 0 ? R.drawable.movie
				: R.drawable.tvshow);
		seen.setChecked(s);
        final Media to_send =media;

        final ContentResolver resolver = ShowFavoriteCursorAdapter.this.c
                .getContentResolver();
        ImageButton del = (ImageButton) view.findViewById(R.id.del_fav);
        del.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                resolver.delete(MediaContentProvider.CONTENT_URI,
                        DBHelperMedia.M_ID + "=?",
                        new String[] { Integer.toString(id) });
				deleteEvent((TraktTVSearch) to_send.mediainfos);

                ShowFavoriteCursorAdapter.this.notifyDataSetChanged(true); 
           
                
            }

        });
        seen.setOnClickListener(new OnClickListener() {

            @Override
			public void onClick(View v) {
			       ContentValues val = new ContentValues();
			       boolean isChecked=((CheckBox)v).isChecked();
	                val.clear();
	                val.put(DBHelperMedia.M_SEEN, isChecked);
	                val.put(DBHelperMedia.M_INSERT_TIME, System.currentTimeMillis());
	               resolver.update(MediaContentProvider.CONTENT_URI, val,	DBHelperMedia.M_TITLE + "=?", new String[] {to_send.mediainfos.getTitle() });
	               if (isChecked && to_send.mediainfos instanceof TraktTVSearch) {
						addEvent((TraktTVSearch) to_send.mediainfos);
			               Log.d("sfca", to_send.mediainfos.getTitle()+ " "+isChecked);

					} else if(!isChecked) {
						deleteEvent((TraktTVSearch) to_send.mediainfos);
					}
			}

        });
        
        poster_fav.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(c, MediaDetails.class);
                i.putExtra("mediaComplete", (Parcelable) to_send);
                c.startActivity(i);

            }

        });


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

	private void addEvent(TraktTVSearch tk) {

		Cursor event_cur = this.c.getContentResolver().query(eventsUri,
				new String[] { "calendar_id", "title" },
				"calendar_id=? and title = ? ",
				new String[] { String.valueOf(1), tk.getTitle() }, null);
		if (event_cur.getCount() <= 0 && tk != null
				&& !tk.getTimeUntilNextAirTime().isEmpty()) {
			ContentValues event = new ContentValues();
			// Calendar in which you want to add Event
			event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
			Log.d("calendar", "evenement created");

			event.put("calendar_id", 1);
			// Title of the Event
			event.put("title", tk.getTitle());
			event.put("eventLocation", "FlibityBoop");
			// Description of the Event
			event.put("description", tk.getTitle() + " is playing soon...");
			// Start Date of the Event
			event.put("dtstart", tk.getTimeToGoMillis());
			// End Date of the Event
			String rrule = "FREQ=WEEKLY;WKST=SU;BYDAY="
					+ tk.getAirDay().substring(0, 2).toUpperCase();
			if (tk.getHours() >= 0) {
				rrule += ";BYHOUR=" + tk.getHours();
			}
			if (tk.getMinutes() >= 0) {
				rrule += ";BYMINUTE=" + tk.getMinutes();
			}
			event.put("rrule", rrule);
			event.put("eventStatus", 1);

			event.put("duration", "P" + tk.getDuration() * 60 + "S");
			// Set alarm on this Event
			event.put("hasAlarm", 1);
			Log.d("calendar", "evenement created");

			// add reminder for the event
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(this.c);
			boolean reminder = sharedPref.getBoolean("calendar_notifications",
					true);
			int minutes = Integer.parseInt(sharedPref.getString("notif_time",
					"15"));
			if (reminder) {

				Uri uri = this.c.getContentResolver().insert(eventsUri, event);
				long eventID = Long.parseLong(uri.getLastPathSegment());
				ContentValues reminders = new ContentValues();
				reminders.put(Reminders.EVENT_ID, eventID);
				reminders.put(Reminders.METHOD, Reminders.METHOD_DEFAULT);
				reminders.put(Reminders.MINUTES, minutes);
				Log.d("calendar", ""+minutes);

				this.c.getContentResolver().insert(remindersUri, reminders);
				boolean alert = sharedPref.getBoolean("alert", false);
				if (alert) {
					reminders.clear();
					Log.d("calendar", "alert on");

					reminders = new ContentValues();
					reminders.put(Reminders.EVENT_ID, eventID);
					reminders.put(Reminders.METHOD, Reminders.METHOD_ALERT);
					reminders.put(Reminders.MINUTES, minutes);
					this.c.getContentResolver().insert(remindersUri, reminders);
				}
				boolean mail = sharedPref.getBoolean("mail", false);
				if (mail) {
					Log.d("calendar", "mail on");

					reminders.clear();
					reminders = new ContentValues();
					reminders.put(Reminders.EVENT_ID, eventID);
					reminders.put(Reminders.METHOD, Reminders.METHOD_EMAIL);
					reminders.put(Reminders.MINUTES, minutes);
					this.c.getContentResolver().insert(remindersUri, reminders);
				}

			}
		}
		try {
			event_cur.close();
		} catch(Exception e){
			
		}
	}

	private void deleteEvent(TraktTVSearch tk) {
		try {
			this.c.getContentResolver().delete(
					eventsUri,
					"calendar_id=? and title=? and eventLocation=? ",
					new String[] { String.valueOf(1), tk.getTitle(),
							"FlibityBoop" });
			Log.d("calendar", "evenement deleted");

		} catch (Exception e) {
			Toast.makeText(this.c, "Unable to delete event", Toast.LENGTH_SHORT)
					.show();
		}

	}

}
