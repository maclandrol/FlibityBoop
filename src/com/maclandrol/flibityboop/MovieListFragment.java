/**
 * IFT2905 : Interface personne machine
 * Projet de session: FlibityBoop.
 * Team: Vincent CABELI, Henry LIM, Pamela MEHANNA, Emmanuel NOUTAHI, Olivier TASTET
 * @author Emmanuel Noutahi, Vincent Cabeli
 */

package com.maclandrol.flibityboop;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Classe MovieListFragment, 
 * Fragment de type List pour les Films favoris
 */
public class MovieListFragment extends ListFragment implements	LoaderManager.LoaderCallbacks<Cursor> {

	MovieFavoriteCursorAdapter adapter; //adapter pour le listview
	private static final int LOADER_ID = 10;
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
		//Récuperer les arguments, 
		Bundle args = getArguments();
		affichage = args.getString("trie");
		View rootView = inflater.inflate(R.layout.fragment_favorites,	container, false);

		getLoaderManager().initLoader(LOADER_ID, null, this);
		adapter= new MovieFavoriteCursorAdapter(this.getActivity(), R.layout.movie_favorite_details, cursor, from, to, 0);
	    setListAdapter(adapter);
		return rootView;
	}
	
	private Bundle savedInstanceState;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.savedInstanceState = savedInstanceState;

	}

	@Override
	public void onResume() {
		super.onResume();
		onCreate(savedInstanceState);        
	}
	
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
			adapter.swapCursor(cursor);
			break;
		}
	}

	/*
	 * Cette méthode gère le cas où le Loader n'a plus accès aux données. On
	 * indique donc au CursorAdapter de se déconnecter du Cursor.
	 */
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}

}
