package com.da_grupo9.ronda.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.da_grupo9.ronda.R;
import com.google.android.material.tabs.TabLayout;

public class SavedItemsFragment extends Fragment {

    private static final String SELECTED_TAB = "selected_tab";
    private int selectedTab;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved_items, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedTab = savedInstanceState == null
                ? 0
                : savedInstanceState.getInt(SELECTED_TAB, 0);

        TabLayout tabs = view.findViewById(R.id.savedItemsTabs);
        tabs.addTab(tabs.newTab().setText("Favoritos"));
        tabs.addTab(tabs.newTab().setText("Búsquedas guardadas"));

        TabLayout.Tab initialTab = tabs.getTabAt(selectedTab);
        if (initialTab != null) {
            initialTab.select();
        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mostrarPestana(tab.getPosition());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        mostrarPestana(selectedTab);
    }

    private void mostrarPestana(int position) {
        selectedTab = position;
        Fragment fragment = position == 0
                ? new FavoritesFragment()
                : new SavedSearchesFragment();

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.savedItemsContent, fragment)
                .commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(SELECTED_TAB, selectedTab);
        super.onSaveInstanceState(outState);
    }
}
