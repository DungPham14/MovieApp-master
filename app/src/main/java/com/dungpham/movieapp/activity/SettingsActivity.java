package com.dungpham.movieapp.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.dungpham.movieapp.R;


public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        getFragmentManager()
                .beginTransaction()
                .replace( android.R.id.content, new SettingsFragment() )
                .commit();
    }

    // hiện list sắp xếp
    public static class SettingsFragment extends PreferenceFragment{
        @Override
        public void onCreate( final Bundle savedInstanceState ){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource( R.xml.preferences );
        }
    }
}
