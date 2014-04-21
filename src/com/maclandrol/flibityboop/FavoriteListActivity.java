package com.maclandrol.flibityboop;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import android.app.ExpandableListActivity;
import android.app.ListActivity;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.content.CursorLoader;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.ImageView;

public class FavoriteListActivity extends ExpandableListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	String affichage;
	Cursor cursor;
	//final static ImageLoader im = new ImageLoader(getApplicationContext()); 
	ShowFavoriteCursorAdapter adapter;
	static final String[] select = new String[] { DBHelperMedia.M_ID,
			DBHelperMedia.M_INSERT_TIME, DBHelperMedia.M_TITLE,
			DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS, DBHelperMedia.M_SEEN };

	static final String[] child_from = new String[] { DBHelperMedia.M_TITLE,
			DBHelperMedia.M_SEEN, DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS,
			DBHelperMedia.M_INFOS,DBHelperMedia.M_INFOS };

	static final int[] child_to = new int[] { R.id.title_fav, R.id.seen_fav,
			R.id.type_icon_fav, R.id.date_fav, R.id.score_fav, R.id.poster_fav };
	
	static final String [] group_from = new String[] {DBHelperMedia.M_SEEN};
	static final int [] group_to = new int [] {R.id.ctView1};
	private static final int LOADER_ID = 6;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		affichage = getIntent().getStringExtra("affichage");
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Notez l'absence des appels à DBHelper.query

		// Cet appel initialise le loader, qui va charger les données sur un
		// thread séparé
		getLoaderManager().initLoader(LOADER_ID, null, this);

		adapter = new ShowFavoriteCursorAdapter(this, cursor, R.layout.layout_group, group_from, group_to, R.layout.show_favorite_details, 
		child_from, child_to );
		//adapter.setViewBinder(VIEW_BINDER);
		setListAdapter(adapter);
	}

	static final ViewBinder VIEW_BINDER = new ViewBinder() {
		@Override
		public boolean setViewValue(View v, Cursor c, int index) {
			MediaInfos m = null;

			switch (v.getId()) {
			case R.id.date_fav:
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new ByteArrayInputStream(c.getBlob(index)));
					m = (MediaInfos) ois.readObject();
				} catch (Exception e) {

				}
				if (m != null) {
					((TextView) v).setText(m.getDate());
				}
				return true;

			case R.id.score_fav:
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new ByteArrayInputStream(c.getBlob(index)));
					m = (MediaInfos) ois.readObject();
				} catch (Exception e) {

				}
				if (m != null) {
					((TextView) v).setText(m.getScore() + "%");
				}
				return true;
				
			case R.id.poster_fav:
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new ByteArrayInputStream(c.getBlob(index)));
					m = (MediaInfos) ois.readObject();
				} catch (Exception e) {

				}
				if (m != null) {
					//ImageLoader im = new ImageLoader();
					//im.DisplayImage(m.getPosterURL(1), (ImageView)v);
				}
				return true;
				
			case R.id.title_fav:
				return false; // on laisse android afficher directement
			case R.id.type_icon_fav:
				((ImageView) v)
						.setImageResource(c.getInt(index) <= 0 ? R.drawable.movie
								: R.drawable.tvshow);
				return true;
			case R.id.seen_fav:
				boolean seen = c.getInt(index) > 0 ? true : false;
				((CheckBox) v).setChecked(seen);
				return true;
			}

			return false;
		}
	};

	/*
	 * onCreateLoader est appelée par initLoader indirectement. On se situe sur
	 * un thread séparé et on doit créer un objet de type CursorLoader, qui
	 * s'occupe de charger un Cursor depuis un URI particulier. C'est une
	 * version avancée d'un ContentResolver.
	 * 
	 * Les spécificités de chaque type d'affichage sont exactement les mêmes que
	 * pour le pendant BDDMétéo.
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cursorLoader = null;
		if (affichage.equals("complet")) {
			// Lecture de tous les éléments sans tri
			cursorLoader = new CursorLoader(this,
					MediaContentProvider.CONTENT_URI, select, null, null, null);
		} else if (affichage.equals("show")) {
			cursorLoader = new CursorLoader(this,
					MediaContentProvider.CONTENT_URI, select,
					DBHelperMedia.M_SHOW + "=?", new String[] { "1" }, null);
		} else if (affichage.equals("movie")) {
			cursorLoader = new CursorLoader(this,
					MediaContentProvider.CONTENT_URI, select,
					DBHelperMedia.M_SHOW + "=?", new String[] { "0" }, null);
		} 
		else if (affichage.equals("show_trie")) {
			cursorLoader = new CursorLoader(this,
					MediaContentProvider.CONTENT_URI, select,
					DBHelperMedia.M_SHOW + "=?", new String[] { "0" }, DBHelperMedia.M_INSERT_TIME+ " DESC");
		}
		else {
			cursorLoader = new CursorLoader(this,
					MediaContentProvider.SHOW_URI, new String [] {DBHelperMedia.M_ID,DBHelperMedia.M_DAY}, null, null,
					null);
		}
		return cursorLoader;
	}

	/*
	 * Lorsque le chargement des données est complété, il faut indiquer au
	 * SimpleCursorAdapter d'utiliser notre nouveau Cursor. Ceci se fait à
	 * l'aide de swapCursor.
	 * 
	 * Notez qu'il peut y avoir plusieurs loaders, d'où l'intérêt d'utiliser un
	 * loader ID unique.
	 */
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case LOADER_ID:
			adapter.changeCursor(cursor);
			break;
		}
	}

	/*
	 * Cette méthode gère le cas où le Loader n'a plus accès aux données. On
	 * indique donc au SimpleCursorAdapter de se déconnecter du Cursor.
	 */
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.changeCursor(cursor);
	}

}
