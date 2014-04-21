package com.maclandrol.flibityboop;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;


/**
 * 
 * Cette activité utilise un ViewPager, un élément faisant
 * partie de l'Android Support Library, qui permet d'afficher
 * plusieurs "pages" ou "tabs" dans une seule activité et qui
 * permet à l'usager de glisser (swipe) d'un à l'autre de gauche
 * à droite.
 * 
 * Cette activité implante un cas où on a beaucoup de pages, nécessitant
 * ainsi l'usage d'un indicateur de tabs dynamique qui n'affiche qu'une
 * partie des tabs.
 * 
 */
public class FavoriteActivity extends BaseActivity {
	
	SimplePagerAdapter adapter;
	ViewPager pager;
	TextView t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_favorites);
        
        // On utilise un PagerAdapter pour insérer des pages dans le ViewPager
        adapter = new SimplePagerAdapter(getSupportFragmentManager());
        t = (TextView) findViewById(R.id.fav_type_titre);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        
        // On utilise les tabs de l'action bar, donc il faut manuellement
        // initialiser ces tabs
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        	/*
        	 * onTabSelected est appelé lorsque l'usager appuie sur
        	 * l'un des tabs de l'action bar. On doit donc changer
        	 * la page du ViewPager.
        	 * 
        	 */
        	@Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                pager.setCurrentItem(tab.getPosition());
                if(tab.getPosition()==1)
                t.setText(R.string.fav_mov);
                else
                	t.setText(R.string.fav_show);
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };

        // On ajoute manuellement les tabs, mais on pourrait aussi
        // combiner la création des tabs avec le ViewPager.
        // On utilise getPageTitle pour déterminer les titres
        // de chaque page automatiquement.
        for (int i = 0; i < adapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(adapter.getPageTitle(i))
                            .setTabListener(tabListener));
        }

		// À l'inverse de ci-haut, on veut aussi mettre à jour
		// le tab sélectionné de l'action bar lorsqu'on change
		// la page dans le ViewPager.
		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
				if (position == 1)
					t.setText(R.string.fav_mov);
				else
					t.setText(R.string.fav_show);
			}
		});

	}

    /*
     * 
     * Le SimplePagerAdapter est le contrôleur principal du ViewPager.
     * C'est ici qu'on détermine le contenu du ViewPager.
     * 
     * Comme nous avons peu de pages, on utilise un FragmentPagerAdapter.
     * Cette variante du PagerAdapter conserve en mémoire les fragments au
     * complet, y compris les layouts. Ceci augmente les performances, mais
     * aussi l'usage mémoire.
     * 
     */
    private class SimplePagerAdapter extends FragmentPagerAdapter
    {
		public SimplePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/*
		 * La méthode getItem est la méthode la plus importante du PagerAdapter.
		 * C'est ici qu'on initialise les fragments sur demande.
		 * 
		 */
		@Override
		public Fragment getItem(int i) {
			if(i==1){
				MovieListFragment f = new MovieListFragment();
				Bundle args = new Bundle();
				args.putString("trie", "alphabetic");
				f.setArguments(args);
				return f;
			}
			
			return new TVListFragment();
		}

		/*
		 * Donne le nombre de pages. Ceci pourrait être variable, mais
		 * est généralement fixe. Réduire le nombre de pages peut
		 * être problématique.
		 * 
		 */
		@Override
		public int getCount() {

			return 2;
		}
		
		/*
		 * Retourne le titre d'une page. Ce titre peut être utilisé pour,
		 * par exemple, afficher le titre de la page dans l'action bar.
		 * Le PagerTabStrip/PagerTitleStrip utilise getPageTitle pour
		 * afficher les titres.
		 * 
		 */
		@Override
		public CharSequence getPageTitle(int position) {
			
			if(position==1)
				return "Movies";
			else
				return "TV Shows";
		}
    	
    }
}

