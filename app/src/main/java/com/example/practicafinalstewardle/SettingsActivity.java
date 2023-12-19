package com.example.practicafinalstewardle;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;


public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, fragment,
                            fragment.getClass().getSimpleName())
                    .commit();
        }
        SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
            if (key.equals("dark_mode")) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                if(preferences.getBoolean("dark_mode", false))AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                recreate();
            }
            if (key.equals("language")){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                // Actualiza la configuración del idioma
                Locale locale = new Locale(preferences.getString("language", "es"));
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getResources().updateConfiguration(config, getResources().getDisplayMetrics());

                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        };
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_game:
                startActivity(new Intent(this, GameActivity.class));
                return true;
            case R.id.action_stats:
                startActivity(new Intent(this, StatsActivity.class));
                return true;
            case R.id.action_ranking:
                startActivity(new Intent(this, RankingActivity.class));
                return true;
            default:
                return false;
        }
    }
}