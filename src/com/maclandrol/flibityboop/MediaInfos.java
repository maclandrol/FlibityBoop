/**
 * IFT2905 : Interface personne machine
 * Projet de session: FlibityBoop.
 * Team: Vincent CABELI, Henry LIM, Pamela MEHANNA, Emmanuel NOUTAHI, Olivier TASTET
 * @author Emmanuel Noutahi, Vincent Cabeli
 */

package com.maclandrol.flibityboop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Interface MediaInfos, implémenté par résultats de recherche de chaque API 
 */
public interface MediaInfos extends Parcelable, Serializable {

	public String getTitle();
	
	public String getDetailedTitle();

	public int getScore();

	public boolean isMovie();

	public boolean isShow();

	public API.MediaType getType();

	public int getID();

	public String getDate();

	public String getOriginalPosterURL();

	public HashMap<String, String> getAdditionalFeatures();

	public ArrayList<Critics> getCritics();

	public ArrayList<? extends MediaInfos> getSimilar();

	public String getPosterURL(int i);

	public static final Parcelable.Creator<MediaInfos> CREATOR = new Creator<MediaInfos>() {

		@Override
		public MediaInfos createFromParcel(Parcel source) {
			int type = source.readInt();
			if (type == 1)
				return new RTSearch(source, false);
			else if (type == 2){
				return new TraktTVSearch(source, false);
				
			}
			else if (type == 3)
				return new TMDBSearch(source, false);
			return null;
		}

		@Override
		public MediaInfos[] newArray(int size) {
			return new MediaInfos[size];
		}

	};
}
