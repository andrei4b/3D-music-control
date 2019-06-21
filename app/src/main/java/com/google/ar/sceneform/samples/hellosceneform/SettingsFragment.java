package com.google.ar.sceneform.samples.hellosceneform;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v7.preference.PreferenceManager;
import android.widget.ToggleButton;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences prefs;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.preferences, s);
    }

    @Override
    public void onStart() {
        super.onStart();

        EditTextPreference ip_etp = (EditTextPreference) findPreference("ip_preference");
        EditTextPreference port_opt1_etp = (EditTextPreference) findPreference("port_opt1_preference");
        EditTextPreference port_opt2_etp = (EditTextPreference) findPreference("port_opt2_preference");
        EditTextPreference port_opt3_etp = (EditTextPreference) findPreference("port_opt3_preference");
        EditTextPreference x_range_etp = (EditTextPreference) findPreference("x_range_preference");
        EditTextPreference y_range_etp = (EditTextPreference) findPreference("y_range_preference");
        EditTextPreference z_range_etp = (EditTextPreference) findPreference("z_range_preference");
        EditTextPreference delay_etp = (EditTextPreference) findPreference("delay_preference");

        ip_etp.setSummary(ip_etp.getText());
        port_opt1_etp.setSummary(port_opt1_etp.getText());
        port_opt2_etp.setSummary(port_opt2_etp.getText());
        port_opt3_etp.setSummary(port_opt3_etp.getText());
        x_range_etp.setSummary(x_range_etp.getText());
        y_range_etp.setSummary(y_range_etp.getText());
        z_range_etp.setSummary(z_range_etp.getText());
        delay_etp.setSummary(delay_etp.getText());

        SharedPreferences.OnSharedPreferenceChangeListener spChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                ip_etp.setSummary(ip_etp.getText());
                port_opt1_etp.setSummary(port_opt1_etp.getText());
                port_opt2_etp.setSummary(port_opt2_etp.getText());
                port_opt3_etp.setSummary(port_opt3_etp.getText());
                x_range_etp.setSummary(x_range_etp.getText());
                y_range_etp.setSummary(y_range_etp.getText());
                z_range_etp.setSummary(z_range_etp.getText());
                delay_etp.setSummary(delay_etp.getText());
            }
        };

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(spChanged);
    }
}
