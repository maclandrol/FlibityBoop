/**
 * IFT2905 : Interface personne machine
 * Projet de session: FlibityBoop.
 * Team: Vincent CABELI, Henry LIM, Pamela MEHANNA, Emmanuel NOUTAHI, Olivier TASTET
 * @author Emmanuel Noutahi, Vincent Cabeli
 */
package com.maclandrol.flibityboop;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;


public class MediaContentProvider  extends ContentProvider{
	
	DBHelperMedia dbh;
	private static final String AUTHORITY = "com.maclandrol.flibityboop";
	public static final int MEDIA = 10;
	public static final int DATE = 11; 

	private static final String MEDIA_PATH = "media";
	private static final String DAY_PATH = "day";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY+ "/" + MEDIA_PATH);
	public static final Uri SHOW_URI = Uri.parse("content://" + AUTHORITY+ "/" + DAY_PATH);
	/*
	 * L'UriMatcher est ce qui associe le ContentProvider avec des URIs.
	 */
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, MEDIA_PATH, MEDIA);
		sURIMatcher.addURI(AUTHORITY, DAY_PATH, DATE);

	}
	
	@Override
	public boolean onCreate() {
		dbh = new DBHelperMedia(getContext());
		Log.d("contentprovider","created.");
		return true;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = dbh.getWritableDatabase();
		int rowsDeleted = 0;
		Log.d("contentprovider","delete "+uri);

		switch (uriType) {
		case MEDIA:
			rowsDeleted = sqlDB.delete(DBHelperMedia.MEDIA_TABLE, selection,
					selectionArgs);
			dbh.updateDate();
			break;
		case DATE:
			rowsDeleted = sqlDB.delete(DBHelperMedia.SHOW_DATE_TABLE, selection,
					selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d("contentprovider","insert "+uri);
		
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case MEDIA:
			try {
				dbh.addNewEntry(values);
			} catch ( SQLException e ) {
				Log.d("contentprovider","insert failed");
				return null; 
				}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(MEDIA_PATH + "/" + values.get(DBHelperMedia.M_ID));
	}
	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {

		Log.d("contentprovider","query "+uri);
		
		// SQLiteQueryBuilder s'occupe d'automatiser certains éléments de la création d'une requête SQL
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Vérifie si toutes les colonnes demandées dans le tableau projection existent bien dans la base de données
		checkColumns(projection);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case MEDIA:
			queryBuilder.setTables(DBHelperMedia.MEDIA_TABLE);
			break;
		case DATE:
			queryBuilder.setTables(DBHelperMedia.SHOW_DATE_TABLE);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = dbh.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	/*
	 * Appelé pour modifier des entrées existantes, mais sans les supprimer.
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.d("contentprovider","update "+uri);

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = dbh.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case MEDIA:
			rowsUpdated = sqlDB.update(DBHelperMedia.MEDIA_TABLE,values,selection,selectionArgs);
			break;
		case DATE:
			rowsUpdated = sqlDB.update(DBHelperMedia.SHOW_DATE_TABLE,values,selection,selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
	
	
	/*
	 * Vérifie si les colonnes demandées dans une requête de type query
	 * sont bien définies dans la table de la base de données, et lance
	 * une exception si ce n'est pas le cas.
	 */
	private void checkColumns(String[] projection) {
		
		String[] available = {
				DBHelperMedia.M_ID,
				DBHelperMedia.M_INSERT_TIME,
				DBHelperMedia.M_TITLE,
				DBHelperMedia.M_SHOW,
				DBHelperMedia.M_INFOS,
				DBHelperMedia.M_SEEN,
				DBHelperMedia.M_DAY,
				DBHelperMedia.M_DAY_CORR};
				
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}
	

}
