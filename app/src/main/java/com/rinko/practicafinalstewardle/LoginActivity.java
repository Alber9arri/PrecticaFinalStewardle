package com.rinko.practicafinalstewardle;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;


import java.util.Locale;


public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin, buttonRegister;

    private UserRepository userRepository;
    private SharedPreferences preferences;
    private FrameLayout adContainerView;
    private InterstitialAd mInterstitialAd;
    Button btnInter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Obtiene las preferencias y la configuración de las preferencias para el idioma y modo oscuro
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration config = this.getResources().getConfiguration();
        if(preferences.getBoolean("dark_mode", false)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (!preferences.getString("language", "es").equals(config.getLocales().get(0).getLanguage())){
            Locale locale = new Locale(preferences.getString("language", "es"));
            Locale.setDefault(locale);
            config.locale = locale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
            recreate();
        }
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        adContainerView = findViewById(R.id.banner);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        //Abrimos el repositorio de usuarios
        userRepository = new UserRepository(this);
        userRepository.open();

        LoadInterticialAd();

        //Lógica botón login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        //Lógica botón registro
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadInterticialAd();
                showInterticial();
                registerUser();
            }
        });

        loadBanner();
        //LoadInterticialAd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userRepository.close();
    }

    //Este método se usa para loguear a los usuarios cuando ya están registrados
    private void loginUser(){
        showInterticial();
        String username = editTextUsername.getText().toString();
        //Aqui se hace uso de la clase HashPassword para hacer un uso seguro de las contraseñas
        String password = HashPassword.sha256(editTextPassword.getText().toString());

        if (userRepository.checkUser(username, password)) {
            // Inicio de sesión exitoso
            Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

            //Guardamos el usuario actual en el archivo de preferencias (se usará para obtener estadísticas, ranking...)
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("UsuarioActual", username);
            editor.apply();

            // Cambia a la pantalla de juego
            Intent intent = new Intent(LoginActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Nombre de usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }

    //Este método se usa para registrar usuarios en la base de datos
    private void registerUser() {
        String inputUsername = editTextUsername.getText().toString();
        //Aqui se hace uso de la clase HashPassword para cifrar las contraseñas de los usuarios que se registran
        String inputPassword = HashPassword.sha256(editTextPassword.getText().toString());

        // Verificar si el usuario ya existe en la base de datos
        if (!userRepository.checkUser(inputUsername, inputPassword)) {
            // Si no existe, agregarlo
            User user = new User(inputUsername, inputPassword);
            userRepository.addUser(user);

            // Mostrar un mensaje de registro exitoso
            Toast.makeText(LoginActivity.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
        } else {
            // Mostrar un mensaje indicando que el usuario ya está registrado
            Toast.makeText(LoginActivity.this, "El usuario ya está registrado", Toast.LENGTH_SHORT).show();
        }
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;
        adContainerView = findViewById(R.id.banner);
        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void loadBanner() {

        // Create a new ad view.
        AdView adView = new AdView(this);
        adView.setAdSize(getAdSize());
        adView.setAdUnitId("ca-app-pub-8457482165373954/5626725959");

        // Replace ad container with new ad view.
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        // Start loading the ad in the background.
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void showInterticial(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(LoginActivity.this);
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }

    private void LoadInterticialAd(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-8457482165373954/9943678241", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                //Log.d(TAG, "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                //Log.d(TAG, "Ad dismissed fullscreen content.");
                                mInterstitialAd = null;
                                LoadInterticialAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                //Log.e(TAG, "Ad failed to show fullscreen content.");
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                //Log.d(TAG, "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                //Log.d(TAG, "Ad showed fullscreen content.");
                            }
                        });
                        //Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        //Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }
}



