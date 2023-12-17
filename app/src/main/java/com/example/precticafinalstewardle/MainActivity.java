// MainActivity.java
package com.example.precticafinalstewardle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        userRepository = new UserRepository(this);
        userRepository.open();

        // Agregar un usuario de ejemplo al iniciar la aplicación
        addUserExample();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                if (userRepository.checkUser(username, password)) {
                    // Inicio de sesión exitoso
                    Toast.makeText(MainActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                    // Cambiar a la actividad SuccessfulLoginActivity
                    Intent intent = new Intent(MainActivity.this, SuccessfulLoginActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Nombre de usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userRepository.close();
    }

    private void addUserExample() {
        // Verificar si el usuario "user" ya existe en la base de datos
        if (!userRepository.checkUser("user", "user")) {
            // Si no existe, agregarlo
            User user = new User("user", "user");
            userRepository.addUser(user);
        }
    }
}



