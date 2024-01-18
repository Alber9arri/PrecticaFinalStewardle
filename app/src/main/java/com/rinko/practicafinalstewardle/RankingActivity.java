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


import java.util.List;
import java.util.Locale;

public class RankingActivity extends AppCompatActivity {
    private String currentUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
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

        //Guardamos en currentUsername el usuario actual, el cual hemos obtenido del archivo de preferencias
        currentUsername = prefs.getString("UsuarioActual", "");

        //Abrimos el repositorio de usuarios y creamos un Array para guardar el top de mejores usuarios
        UserRepository userRepository = new UserRepository(this);
        List<User> topUsers = userRepository.getTopFiveUsers();

        //Recorremos el array del top de usuarios (anteriormente ordenado por victorias) hasta un máximo de 5 usuarios
        for (int i = 0; i < 5 && i < topUsers.size(); i++) {
            User user = topUsers.get(i);

            //Obtener los identificadores de los TextView dinámicamente
            int playerTextViewId = getResources().getIdentifier("player" + (i + 1), "id", getPackageName());
            int winTextViewId = getResources().getIdentifier("win" + (i + 1), "id", getPackageName());

            //Obtener las referencias a los TextView correspondientes
            TextView playerTextView = findViewById(playerTextViewId);
            TextView winTextView = findViewById(winTextViewId);

            //Se establece el usuario en los TextView correspondientes
            if (playerTextView != null) {
                playerTextView.setText(user.getUsername());
            }

            //Se establecen las victorias en los TextView correspondientes
            if (winTextView != null) {
                winTextView.setText(getString(R.string.wins) + ": " + user.getWon());
            }
        }
        //Un bucle recorre el array de top de usuarios y cuando encuentra coincidencias entre el usuario y el usuario actual lo muestra por pantalla, junto con su número de victorias y posición en el ranking
        for (int i = 0; i < topUsers.size(); i++) {

            User user = topUsers.get(i);

            if(user.getUsername().equals(currentUsername)){
                TextView actualPlayer = findViewById(R.id.actualPlayer);
                actualPlayer.setText(getString(R.string.actual_user) + " '" + currentUsername + "' " + getString(R.string.position) + " " + (i+1));
            }
        }
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