package com.example.precticafinalstewardle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin, buttonRegister;

    private UserRepository userRepository;
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getBoolean("dark_mode", false)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        userRepository = new UserRepository(this);
        userRepository.open();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userRepository.close();
    }

    private void loginUser() {
        String username = editTextUsername.getText().toString();
        String password = HashPassword.sha256(editTextPassword.getText().toString());

        if (userRepository.checkUser(username, password)) {
            // Inicio de sesión exitoso
            Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

            // Cambia a la pantalla de juego
            Intent intent = new Intent(LoginActivity.this, GameActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(LoginActivity.this, "Nombre de usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        String inputUsername = editTextUsername.getText().toString();
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
}



