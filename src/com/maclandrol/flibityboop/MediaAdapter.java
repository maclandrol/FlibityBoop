/**
 * IFT2905 : Interface personne machine
 * Projet de session: FlibityBoop.
 * Team: Vincent CABELI, Henry LIM, Pamela MEHANNA, Emmanuel NOUTAHI, Olivier TASTET
 * @author Emmanuel Noutahi, Vincent Cabeli
 */

package com.maclandrol.flibityboop;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * MediaAdapter est l'adapter du listview de SearchActivity qui permet d'afficher une liste de résultat de recherche
 */

public class MediaAdapter extends BaseAdapter {	
	
	ImageLoader imageLoader = null;
	private Context _context;
	private List<? extends MediaInfos> _data;

	public MediaAdapter(Context context, List<? extends MediaInfos> data) {
		this._context = context;
		this._data = data;
		this.imageLoader = new ImageLoader(context);
	}

	@Override
	public int getCount() {
		if (_data != null)
			return _data.size();
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return _data != null && position >= 0 && position < this.getCount() ? _data
				.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		
		//Set view content
		MediaInfos media = _data.get(position);
		
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) _context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.film_display, parent, false);
		}
		
		TextView title = (TextView) view.findViewById(R.id.title);
		TextView date = (TextView) view.findViewById(R.id.date);
		TextView score = (TextView) view.findViewById(R.id.score);
		ImageView score_icon = (ImageView) view.findViewById(R.id.score_icon);
		
		score.setText(media.getScore()>0?media.getScore() + "%":"?");
		title.setText(media.getTitle());
		title.setTextColor(0xFF000000);
		if (!media.getDate().contains("null") || media.getDate().isEmpty())
			date.setText(media.getDate());
		else
			date.setText("N/A");
		
		ImageView type = (ImageView) view.findViewById(R.id.type_icon);
		type.setImageResource(media.isMovie() ? R.drawable.movie
				: R.drawable.tvshow);
		ImageView poster= (ImageView) view.findViewById(R.id.poster);

		imageLoader.DisplayImage(media.getOriginalPosterURL(), poster);

		if (position % 2 == 0){
			view.setBackgroundColor(_context.getResources().getColor(
					R.color.alt_back));
					}
		else{
			view.setBackgroundColor(_context.getResources().getColor(
					R.color.back));
		}

		if (media.isMovie()){
			score_icon.setImageResource(R.drawable.user_like);
		}
		else {
			score_icon.setImageResource(R.drawable.trakt_love_red);
		}
		return view;
	}

}