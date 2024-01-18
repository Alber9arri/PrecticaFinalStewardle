package com.rinko.practicafinalstewardle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;


public class StatsActivity extends AppCompatActivity {

    private String currentUsername;
    private int played, won, lost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
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

        //Obtenemos el nombre del usuario actual del archivo de preferencias compartidas
        currentUsername = prefs.getString("UsuarioActual", "");

        UserRepository userRepository = new UserRepository(this);
        userRepository.open();

        //Creamos una variable de tipo User la cual se pasará como parámetro a métodos get para obtener el número de partidas jugadas, ganadas y perdidas
        User currentUser = new User(currentUsername, "");
        played = userRepository.getPlayed(currentUser);
        won = userRepository.getWon(currentUser);
        lost = userRepository.getLost(currentUser);

        //Se muestra en TextView las partidas jugadas, ganadas y perdidas
        TextView play = findViewById(R.id.played);
        play.setText(getString(R.string.stats_played) + "     "+ (String.valueOf(played)));

        TextView win = findViewById(R.id.won);
        win.setText(getString(R.string.stats_won) + "     "+ String.valueOf(won));

        TextView lose = findViewById(R.id.lost);
        lose.setText(getString(R.string.stats_lost) + "     "+ String.valueOf(lost));

    }

    @Override
    //Crea el menú
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

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