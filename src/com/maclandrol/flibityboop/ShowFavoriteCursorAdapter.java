/**
 * IFT2905 : Interface personne machine
 * Projet de session: FlibityBoop.
 * Team: Vincent CABELI, Henry LIM, Pamela MEHANNA, Emmanuel NOUTAHI, Olivier TASTET
 * @author Emmanuel Noutahi, Vincent Cabeli
 */

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
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Classe ShowFavoriteCursorAdapter, adapter pour les favoris de type Film (voir le fragment correspondant)
 */
public class ShowFavoriteCursorAdapter extends SimpleCursorTreeAdapter {

	Context c;
	Cursor cur;
	Cursor calendar_cur;
    Calendar cal = Calendar.getInstance();  //récupérer une instance du calendrier
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
		//L'URI change en fonction de la version, même si notre version min est 14
		//nous gardons ce code comme failsafe
		if (android.os.Build.VERSION.SDK_INT <= 7) {
			remindersUri =Uri.parse("content://calendar/reminders");
	         eventsUri = Uri.parse("content://calendar/events");
	     } else {

	         eventsUri = Uri.parse("content://com.android.calendar/events");
	         remindersUri = Uri.parse("content://com.android.calendar/events");
	     }
		 
	}

	/*
	 * @see android.widget.CursorTreeAdapter#getChildrenCursor(android.database.Cursor)
	 * Récupérer le curseur des enfant du groupview
	 */
	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {

		//Aller chercher uniquement les shows qui diffusent ce jour précis
		String day = groupCursor.getString(groupCursor.getColumnIndex(DBHelperMedia.M_DAY));
		ContentResolver resolver = c.getContentResolver();
		Cursor c= resolver.query(MediaContentProvider.CONTENT_URI,	select, DBHelperMedia.M_DAY + "=? AND "+DBHelperMedia.M_SHOW+"=?",
				new String[] { day, "1" }, null);
		return c.getCount()>0 ?c:null;
		

	}


	@Override
	public View newChildView(Context context, Cursor cursor,
			boolean isLastChild, ViewGroup parent) {
		return super.newChildView(context, cursor, isLastChild, parent);
	}

	/*
	 * @see android.widget.SimpleCursorTreeAdapter#bindChildView(android.view.View, android.content.Context, android.database.Cursor, boolean)
	 * Lier les childviews de l'expendablelistview à leur informations provenant du cursor 
	 */
	@Override
	protected void bindChildView(View view, Context context, Cursor cursor,
			boolean isLastChild) {
		Media media = null;
		MediaInfos m =null;
		Log.d("binding view", "this must'nt be recursive"); //debug
		final int id = cursor.getInt(cursor.getColumnIndex(DBHelperMedia.M_ID));
		//Aller chercher chaque view
		TextView date = (TextView) view.findViewById(R.id.date_fav);
		TextView score_fav = (TextView) view.findViewById(R.id.score_fav);
		ImageView poster_fav = (ImageView) view.findViewById(R.id.poster_fav);
		TextView title = (TextView) view.findViewById(R.id.title_fav);
		CheckBox seen = (CheckBox) view.findViewById(R.id.seen_fav);
		TextView rel_time = (TextView) view.findViewById(R.id.rel_time);
		boolean s = cursor.getInt(cursor.getColumnIndex(DBHelperMedia.M_SEEN)) > 0 ? true: false;
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
				//Mettre les valeurs pour toutes les views qui dépendent de média
				m = media.mediainfos;
				date.setText(m.getDate());
				score_fav.setText(m.getScore() + "%");
				im.DisplayImage(m.getPosterURL(1), poster_fav);
				if(m instanceof TraktTVSearch){
					rel_time.setText(((TraktTVSearch) m).getTimeUntilNextAirTime() + " on "
							+((TraktTVSearch) m).getNetwork());
					if(s){
						addEvent((TraktTVSearch) m);
					}
				}

			}
		}
		//Titre du média
		title.setText(cursor.getString(cursor.getColumnIndex(DBHelperMedia.M_TITLE)));
		//Type d'icon
		icon.setImageResource(cursor.getInt(cursor.getColumnIndex(DBHelperMedia.M_SHOW)) <= 0 ? R.drawable.movie: R.drawable.tvshow);
		//Seen or not seen
		seen.setChecked(s);
        final Media to_send =media;

        final ContentResolver resolver = ShowFavoriteCursorAdapter.this.c
                .getContentResolver();
        ImageButton del = (ImageButton) view.findViewById(R.id.del_fav);
        //Listener pour delete
        del.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                resolver.delete(MediaContentProvider.CONTENT_URI,
                        DBHelperMedia.M_ID + "=?",
                        new String[] { Integer.toString(id) });
                if(to_send.mediainfos instanceof TraktTVSearch){

                	Log.d("calendar", "should delete now");
                 	deleteEvent((TraktTVSearch)to_send.mediainfos);
                }
                //Notifier un changement dans la base de donnée à chaque click pour mettre à jour
                ShowFavoriteCursorAdapter.this.notifyDataSetChanged(true); 
                
            }

        });
        
        //click sur seen
        seen.setOnClickListener(new OnClickListener() {

            @Override
			public void onClick(View v) {
			       ContentValues val = new ContentValues();
			       boolean isChecked=((CheckBox)v).isChecked();
	               val.clear();
	               val.put(DBHelperMedia.M_SEEN, isChecked);
	               //val.put(DBHelperMedia.M_INSERT_TIME, System.currentTimeMillis());
	               resolver.update(MediaContentProvider.CONTENT_URI, val,	DBHelperMedia.M_TITLE + "=?", new String[] {to_send.mediainfos.getTitle() });
	               if (isChecked && to_send.mediainfos instanceof TraktTVSearch) {
						addEvent((TraktTVSearch) to_send.mediainfos);
			               Log.d("sfca", to_send.mediainfos.getTitle()+ " "+isChecked);

					} else if(!isChecked) {
						deleteEvent((TraktTVSearch) to_send.mediainfos);
					}
	                //ShowFavoriteCursorAdapter.this.notifyDataSetChanged(true); 
			}

        });
        
        //Click sur un favoris, on souhaite ouvrir l'activité de détail
        poster_fav.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(c, MediaDetails.class);
                i.putExtra("mediaComplete", (Parcelable) to_send);
                c.startActivity(i);

            }

        });

	}

	/*
	 * @see android.widget.SimpleCursorTreeAdapter#bindGroupView(android.view.View, android.content.Context, android.database.Cursor, boolean)
	 * Bind du group au données du cursor
	 */
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

	/*
	 * Ajout d'un nouvel event dans le calendrier
	 * Le calendrier avec pour ID =1  est utilisé (primary calendrier)
	 */
	private void addEvent(TraktTVSearch tk) {

		Cursor event_cur = this.c.getContentResolver().query(eventsUri,
				new String[] { "calendar_id", "title" },
				"calendar_id=? and title = ? ",
				new String[] { String.valueOf(1), tk.getTitle() }, null);
		//on s'assure qu'il s'agit d'un show venant de TraktTV
		// Les shows de tmdb n'ont pas de date
		if (event_cur.getCount() <= 0 && tk != null	&& !tk.getTimeUntilNextAirTime().isEmpty()) {
			ContentValues event = new ContentValues();
			// Calendar in which you want to add Event
			event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
			Log.d("calendar", "evenement created");
			event.put("calendar_id", 1);
			// Title of the Event
			event.put("title", tk.getTitle());
			event.put("eventLocation", tk.getNetwork());
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

			// add reminder for the event, récuperer les préférences de l'utilisateur
			//s'il y a en a ou utiliser les valeurs par défauts
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.c);
			boolean reminder = sharedPref.getBoolean("calendar_notifications",	true);
			int minutes = Integer.parseInt(sharedPref.getString("notif_time", "15"));
			//mettre un reminder, par defaut la notification android
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
				//ajouter les alerts comme reminder
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
				//ajouter les mails comme reminder (non-testé)
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
			event_cur.close(); //fermer le cursor
		} catch(Exception e){
			
		}
	}

	/*
	 * Effacer un event du calendrier.
	 * Si le show n'est plus suivi (follow=false)
	 * Ou le show a été effacé, on essaye d'enlever l'event du calendrier
	 * Il arrive parfois que l'event n'est pas effacé...??
	 */
	private void deleteEvent(TraktTVSearch tk) {
		try {
			this.c.getContentResolver().delete(	eventsUri,	"calendar_id=? and title=? and eventLocation=? ",
					new String[] { String.valueOf(1), tk.getTitle(), tk.getNetwork()});

			Log.d("calendar", "evenement deleted");

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this.c, "Unable to delete event", Toast.LENGTH_SHORT)
					.show();
		}

	}

}
