package com.rinko.practicafinalstewardle;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
        //Obtiene las preferencias y la configuración de las preferencias para el idioma y modo oscuro
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration configuration = this.getResources().getConfiguration();
        if(prefs.getBoolean("dark_mode", false)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (!prefs.getString("language", "es").equals(configuration.getLocales().get(0).getLanguage())){
            Locale locale = new Locale(prefs.getString("language", "es"));
            Locale.setDefault(locale);
            configuration.locale = locale;
            getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
            recreate();
        }
        //Crea un listener para cambiar el modo oscuro y el idioma cuando cambien las preferencias
        SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
            if (key.equals("dark_mode")) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                if(preferences.getBoolean("dark_mode", false)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                recreate();
            }
            if (key.equals("language")){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                Locale locale = new Locale(preferences.getString("language", "es"));
                Configuration config = new Configuration();
                config.setLocale(locale);
                getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                recreate();
            }
        };
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
    @Override
    //Crea el menú
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    //Maneja las opciones del menú
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            case R.id.action_game:
                startActivity(new Intent(this, GameActivity.class));
                finish();
                return true;
            case R.id.action_stats:
                startActivity(new Intent(this, StatsActivity.class));
                finish();
                return true;
            case R.id.action_ranking:
                startActivity(new Intent(this, RankingActivity.class));
                finish();
                return true;
            case R.id.action_tutorial:
                startActivity(new Intent(this, TutorialActivity.class));
                finish();
                return true;
            case R.id.action_logout:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            default:
                return false;
        }
    }
}