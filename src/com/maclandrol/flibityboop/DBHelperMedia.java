package com.maclandrol.flibityboop;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class DBHelperMedia extends SQLiteOpenHelper {

	static final int VERSION = 6;
	static final String MEDIA_TABLE = "fav_media";
	static final String SHOW_DATE_TABLE = "show_date";

	/*
	 * Liste des colonnes de la tables
	 */
	static final String M_ID = "_id";
	static final String M_INSERT_TIME = "insert_time";
	static final String M_TITLE = "titre";
	static final String M_SHOW = "is_show";
	static final String M_INFOS = "mediainfo";
	static final String M_SEEN = "seen";
	static final String M_DAY = "day";
	static final String M_DAY_CORR = "_id";

	static final HashMap<String, Integer> Date_sort = new HashMap<String, Integer>() {

		{
			put("Unknown", 8);
			put("Ended", 7);
			put("Monday", 1);
			put("Tuesday", 2);
			put("Wednesday", 3);
			put("Thursday", 4);
			put("Friday", 5);
			put("Saturday", 6);
			put("Sunday", 0);
		}
	};

	Context context;

	public DBHelperMedia(Context context) {
		super(context, "media.db", null, VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Toast.makeText(context, "Création BDD", Toast.LENGTH_LONG).show();
		Log.d("DBHelper", "Création BDD mediainfos");

		// Appel standard pour créer une table dans la base de données.
		String sql = "create table " + MEDIA_TABLE + " (" + M_ID
				+ " integer primary key, " + M_INSERT_TIME
				+ " integer not null, " + M_TITLE + " text, " + M_SHOW
				+ " bool not null default 0, " + M_INFOS + " blob, " + M_DAY
				+ " text, " + M_SEEN + " bool not null default 0 )";

		String sql2 = "create table " + SHOW_DATE_TABLE + " (" + M_DAY_CORR
				+ " integer primary key, " + M_DAY + " text )";

		// ExecSQL prend en entrée une commande SQL et l'exécute
		// directement sur la base de données.
		db.execSQL(sql);
		db.execSQL(sql2);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int ancienneVersion,
			int nouvelleVersion) {
		Toast.makeText(context, "Mise à jour BDD", Toast.LENGTH_LONG).show();
		Log.d("DBHelper", "Mise à jour BDD");

		// Efface l'ancienne base de données
		db.execSQL("drop table if exists " + MEDIA_TABLE);
		db.execSQL("drop table if exists " + SHOW_DATE_TABLE);

		// Appelle onCreate, qui recrée la base de données
		onCreate(db);
	}

	public void addNewEntry(MediaInfos m, boolean seen, byte[] media_bytes) {

		HashMap<String, String> addInfos = m.getAdditionalFeatures();

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues val = new ContentValues();
		val.clear();
		String date = null;
		if (m instanceof TraktTVSearch) {
			TraktTVSearch tv = (TraktTVSearch) m;
			date = tv.air_day;

		}
		String status = addInfos.get("status");

		val.put(M_ID, m.hashCode());
		val.put(M_INSERT_TIME, System.currentTimeMillis());
		val.put(M_TITLE, m.getTitle());
		val.put(M_SHOW, m.isMovie() ? 0 : 1);
		String day = (date != null && !date.isEmpty()) ? date : ("ended"
				.equals(status) ? "Ended" : "Unknown");
		System.out.println(day);
		val.put(M_INFOS, media_bytes);
		val.put(M_DAY, day);

		val.put(M_SEEN, seen ? 1 : 0);

		try {
			db.insertOrThrow(MEDIA_TABLE, null, val);
			String s_query = "insert or replace into " + SHOW_DATE_TABLE + "("
					+ M_DAY_CORR + ", " + M_DAY + ") values( "
					+ Date_sort.get(day) + ", \"" + day + "\" )";
			System.out.println(s_query);
			db.execSQL(s_query);

		} catch (SQLException e) {
			Log.d("DBHelper", "Erreur BDD: " + e.getMessage());
		}

		db.close();
	}

	public int querySize(String table) {

		SQLiteDatabase db = this.getReadableDatabase();

		// Cursor est un objet très utilisé qui permet de stocker
		// le résultat d'une requête SQL. Ce résultat peut être,
		// dans le cas présent, la colonne ID de toutes les lignes
		// de la base de donnée, ce qui nous permet de les compter.
		Cursor c = db.query(SHOW_DATE_TABLE.equals("table") ? SHOW_DATE_TABLE
				: MEDIA_TABLE, new String[] { M_ID }, null, null, null, null,
				null);
		int size = c.getCount();
		db.close();
		return size;
	}

	public void addNewEntry(ContentValues values) {

		MediaInfos m = null;
		byte[] bytes = values.getAsByteArray(DBHelperMedia.M_INFOS);
		boolean seen = values.getAsBoolean(DBHelperMedia.M_SEEN);
		try {
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(bytes));
			m = (MediaInfos) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (m != null) {
			addNewEntry(m, seen, bytes);
		}

	}

	public void updateDate() {
		SQLiteDatabase db = this.getReadableDatabase();
		HashSet<String> in_day = new HashSet<String>();
		Cursor c = db.query(true, MEDIA_TABLE, new String[] { M_ID, M_DAY },M_SHOW+" =?", new String [] {"1"}, M_DAY, null, null, null);
		
		boolean success = c.moveToFirst();
		int count = c.getCount();
		while (success && count > 0) {
			in_day.add(c.getString(c.getColumnIndex(M_DAY)));
			c.moveToNext();
			count--;
		}
		String sql = String.format("DELETE FROM %s WHERE %s  NOT IN (%s);",
				SHOW_DATE_TABLE, M_DAY,
				("\"" + TextUtils.join("\", \"", in_day.toArray()) + "\""));
		System.out.println(sql);

		db.execSQL(sql);
		db.close();

	}

}
