package com.maclandrol.flibityboop;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MovieListFragment extends ListFragment implements	LoaderManager.LoaderCallbacks<Cursor> {

	MovieFavoriteCursorAdapter adapter;
	private static final int LOADER_ID = 7;
	Cursor cursor;

	String affichage;
	static final String[] select = new String[] { DBHelperMedia.M_ID,
			DBHelperMedia.M_INSERT_TIME, DBHelperMedia.M_TITLE,
			DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS, DBHelperMedia.M_SEEN };

	static final String[] from = new String[] { DBHelperMedia.M_TITLE,
			DBHelperMedia.M_SEEN, DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS,
			DBHelperMedia.M_INFOS, DBHelperMedia.M_INFOS };

	static final int[] to = new int[] { R.id.title_fav, R.id.seen_fav,
			R.id.type_icon_fav, R.id.date_fav, R.id.score_fav, R.id.poster_fav };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		Bundle args = getArguments();
		affichage = args.getString("trie");
		View rootView = inflater.inflate(R.layout.fragment_favorites,	container, false);

		getLoaderManager().initLoader(LOADER_ID, null, this);
		adapter= new MovieFavoriteCursorAdapter(this.getActivity(), R.layout.movie_favorite_details, cursor, from, to, 0);
		adapter.setViewBinder(VIEW_BINDER);
	    setListAdapter(adapter);
		return rootView;
	}
	
	private Bundle savedInstanceState; // add this to your code

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Toast.makeText(getActivity(), "onCreate", Toast.LENGTH_LONG).show();
		this.savedInstanceState = savedInstanceState; // add this to your code

	}

	@Override
	public void onResume() {
		super.onResume();
		onCreate(savedInstanceState);        
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cursorLoader = null;
		if ("time".equals(affichage)) {
			cursorLoader = new CursorLoader(this.getActivity(),
					MediaContentProvider.CONTENT_URI, select,
					DBHelperMedia.M_SHOW + "=?", new String[] { "0" },
					DBHelperMedia.M_INSERT_TIME + " DESC");
			
		} else if ("alphabetic".equals(affichage)) {
				cursorLoader = new CursorLoader(this.getActivity(),
						MediaContentProvider.CONTENT_URI, select,
						DBHelperMedia.M_SHOW + "=?", new String[] { "0" },
						DBHelperMedia.M_TITLE+ " ASC");
		}
		else{
			
			cursorLoader = new CursorLoader(this.getActivity(),
					MediaContentProvider.CONTENT_URI, select,
					DBHelperMedia.M_SHOW + "=?", new String[] { "0" }, null);
		}
		return cursorLoader;
	}

	
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
