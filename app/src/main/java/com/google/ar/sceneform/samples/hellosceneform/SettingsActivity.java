package com.google.ar.sceneform.samples.hellosceneform;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.support.v7.preference.PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if(savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction()
                                       .add(android.R.id.content, fragment, fragment.getClass().getSimpleName())
                                       .commit();
        }
    }
}
