/**
 * IFT2905 : Interface personne machine
 * Projet de session: FlibityBoop.
 * Team: Vincent CABELI, Henry LIM, Pamela MEHANNA, Emmanuel NOUTAHI, Olivier TASTET
 * @author Emmanuel Noutahi, Vincent Cabeli
 */

package com.maclandrol.flibityboop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter pour la listView dans le drawer du MainActivity
 * Différentiation entre les sections et les items cliquables
 */
public class SectionListAdapter  extends ArrayAdapter<Item>{


	Context context;
	private  Item[] items;
	private LayoutInflater inflater;
	
	public SectionListAdapter(Context context,Item[] items) {
		super(context, 0,items);
		this.items=items;
		inflater= (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {

		final Item item = items[position];

		if (v == null) {
			//Si le view est null, choisir le bon layout à inflater
			if (item.isSection()) {
				//S'il s'agit d'une section, le rendre non cliquable, selectionnable ou focusable
				v = inflater.inflate(R.layout.item_section, parent, false);
				v.setClickable(false);
				v.setFocusable(false);
				v.setEnabled(false);
				v.setOnClickListener(null);
			} else {
				v = inflater.inflate(R.layout.item_list, null);
			}
		}
	    
		TextView t = (TextView)v.findViewById(R.id.item_title);
		t.setText(item.getTitle());
	  return v;
	 }
}

/*
 * Classe pour différencier le type d'item
 */

abstract class Item {

	final String title;

	public Item(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public abstract boolean isSection();

}

class ListItem extends Item {

	public ListItem(String title) {
		super(title);
	}

	@Override
	public boolean isSection() {
		return false;
	}

}

class SectionItem extends Item {

	public SectionItem(String title) {
		super(title);
	}

	@Override
	public boolean isSection() {
		return true;
	}

}
