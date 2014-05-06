package com.maclandrol.flibityboop;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;


public class TVListFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	int layout;
	Cursor cursor;
	ExpandableListView flv;
	// final static ImageLoader im = new ImageLoader(getApplicationContext());
	ShowFavoriteCursorAdapter adapter;
	static final String[] select = new String[] { DBHelperMedia.M_ID,
			DBHelperMedia.M_INSERT_TIME, DBHelperMedia.M_TITLE,
			DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS, DBHelperMedia.M_SEEN };

	static final String[] child_from = new String[] { DBHelperMedia.M_TITLE,
			DBHelperMedia.M_SEEN, DBHelperMedia.M_SHOW, DBHelperMedia.M_INFOS,
			DBHelperMedia.M_INFOS, DBHelperMedia.M_INFOS };

	static final int[] child_to = new int[] { R.id.title_fav, R.id.seen_fav,
			R.id.type_icon_fav, R.id.date_fav, R.id.score_fav, R.id.poster_fav };

	static final String[] group_from = new String[] { DBHelperMedia.M_SEEN };
	static final int[] group_to = new int[] { R.id.ctView1 };
	private static final int LOADER_ID = 8;

	private Bundle savedInstanceState; // add this to your code
	TextView empty;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.savedInstanceState = savedInstanceState; // add this to your code

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.expendable_fragment_favorites,container, false);
		flv = (ExpandableListView) rootView.findViewById(R.id.ExpendableFragmentListView);
		empty=(TextView)rootView.findViewById(R.id.empty1);
		this.getLoaderManager().initLoader(LOADER_ID, null, this);
		adapter = new ShowFavoriteCursorAdapter(this.getActivity(), cursor,R.layout.layout_group, group_from, group_to,	R.layout.show_favorite_details, child_from, child_to);
		flv.setAdapter(adapter);
		flv.setEmptyView(empty);
		return rootView;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cursorLoader = new CursorLoader(this.getActivity(),MediaContentProvider.SHOW_URI, new String[] {DBHelperMedia.M_ID, DBHelperMedia.M_DAY }, null, null,	null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case LOADER_ID:
			adapter.setGroupCursor(cursor);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.setGroupCursor(null);
	}
}
