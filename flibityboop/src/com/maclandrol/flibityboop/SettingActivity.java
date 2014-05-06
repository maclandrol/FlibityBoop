package com.maclandrol.flibityboop;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Une SettingsActivity présente des paramètres à l'usager.
 * C'est un système complètement différent des interfaces standards,
 * car l'apparence des panneaux de configuration est unifiée à travers
 * Android. On se contente donc de gérer les paramètres et d'utiliser un
 * format de données spécifique aux paramètres dans les xml.
 */
public class SettingActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Au lieu de charger un layout, on doit initialiser le fragment de préférences
		getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
	}

	/**
	 * 
	 * Un OnPreferenceChangeListener permet de gérer les événements liés
	 * aux changements des propriétés par l'usager. C'est ici qu'on
	 * mettra à jour les "summaries" de chaque préférence en fonction
	 * de la valeur choisie.
	 * 
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// Dans le cas d'une liste, on détermine l'entrée choisie et
				// on affiche le texte correspondant
				
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
		
			} else {
				// En général, on se contente d'utiliser la représentation
				// sous forme de texte de la valeur définie
				
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Associe la préférence à son sommaire automatiquement.
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// On définit le listener de changement de cette préférence
		// comme étant celui de toute l'activité
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// On associe la préférence à une clé dans les préférences pour
		// que le listener puisse faire le pont entre l'objet et la valeur
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
				PreferenceManager.getDefaultSharedPreferences(preference.getContext())
								 .getString(preference.getKey(), "")
						);
	}

	/**
	 * 
	 * Un simple fragment qui contient toutes les propriétés de l'activité.
	 * 
	 * Une interface plus complexe aurait plusieurs fragments et une série
	 * de headers pour passer d'un fragment à un autre, mais dans notre cas
	 * il est peu probable que vous ayez tant de paramètres.
	 * 
	 */
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			// Le système est simple: on lit les préférences d'un fichier XML
			addPreferencesFromResource(R.xml.pref_general);

			// ... puis on associe les préférences à leur valeur
			bindPreferenceSummaryToValue(findPreference("max_req"));
			
			// Et on répète pour chaque XML
			addPreferencesFromResource(R.xml.pref_notification);
			bindPreferenceSummaryToValue(findPreference("notif_time"));
			/*bindPreferenceSummaryToValue(findPreference("calendar_notifications"));
			
			bindPreferenceSummaryToValue(findPreference("alert"));

			bindPreferenceSummaryToValue(findPreference("mail"));*/
		}
	}
}