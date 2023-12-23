package com.example.practicafinalstewardle;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://stewardle.com/";
    private ApiService apiService;

    private JsonArray drivers = new JsonArray();
    ArrayList<String> autocomplete = new ArrayList<>();
    private AutoCompleteTextView driverInput;
    private JsonElement driver;
    private int intentos;
    private int idCount = R.id.driver1age;
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Configuración de idioma y modo oscuro en base a las preferencias
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
        driverInput = findViewById(R.id.DriverInput);
        //Añade un keylistener al recuadro de introducir el piloto para que se ejecute al pulsar enter
        driverInput.setOnKeyListener((v, keyCode, event) -> {
            // Verificar si la tecla presionada es la tecla "Enter"
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                try {
                    check(v);
                } catch (SVGParseException e) {
                    throw new RuntimeException(e);
                }
                return true; // Indica que el evento ha sido manejado
            }
            return false; // Indica que el evento no ha sido manejado
        });
        //Añade un listener al driver input para comprobar si se pulsa sobre un piloto
        driverInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    check(view);
                } catch (SVGParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Configura Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        // Crea una instancia de ApiService
        apiService = retrofit.create(ApiService.class);

        // Llama al método para obtener la respuesta de la api
        Call<String> call = apiService.getData();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                //Respuesta correcta de la api
                if (response.isSuccessful()) {
                    String responseData = response.body();
                    JsonParser jsonParser = new JsonParser();
                    JsonObject jsonObject = jsonParser.parse(responseData).getAsJsonObject();
                    Set<String> keys = jsonObject.keySet();

                    //Obtener los datos de los pilotos y los nombres para el desplegable de autocompletar
                    for (String key : keys) {
                        drivers.add(jsonObject.get(key));
                        autocomplete.add(jsonObject.get(key).getAsJsonObject().get("firstName").toString().replaceAll("\"", "")+" "+jsonObject.get(key).getAsJsonObject().get("lastName").toString().replaceAll("\"", ""));
                    }
                    JSONObject finalobject = new JSONObject();

                    //Guarda los datos de los pilotos en un archivo .json local para tener una copia por si falla la api
                    try {
                        finalobject.put("drivers", drivers);
                        JsonArray names = new JsonArray();

                        for (String name : autocomplete) {
                            names.add(name);
                        }
                        finalobject.put("names", names);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        // Obtener el directorio de archivos internos de la aplicación
                        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                        // Crear un archivo en el directorio interno
                        File file = new File(directory, "drivers.json");

                        // Convertir el objeto JSON a una cadena
                        String jsonString = finalobject.toString();

                        // Escribir la cadena en el archivo
                        FileWriter fileWriter = new FileWriter(file);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                        bufferedWriter.write(jsonString);
                        bufferedWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Ejecuta el código del juego
                    playGame();
                } else {
                    //En caso de fallo en la api se intentan recuperar los datos de los pilotos desde el archivo generado previamente
                    try {
                        // Obtener el directorio "Documents" en el almacenamiento interno de la aplicación
                        File documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

                        // Crear un objeto File apuntando al archivo JSON en el directorio "Documents"
                        File file = new File(documentsDirectory, "drivers.json");

                        // Verificar si el archivo existe
                        if (!file.exists()) {
                            Toast.makeText(GameActivity.this, "No existe el fichero drivers.json", Toast.LENGTH_SHORT).show();
                        }

                        // Leer el contenido del archivo JSON
                        FileInputStream fileInputStream = new FileInputStream(file);
                        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }

                        // Cerrar los recursos
                        bufferedReader.close();
                        inputStreamReader.close();
                        fileInputStream.close();
                        JsonParser parser = new JsonParser();
                        // Convertir la cadena de texto a un elemento JSON
                        JsonElement jsonElement = parser.parse(stringBuilder.toString());
                        Log.e(TAG, jsonElement.getAsJsonObject().get("names").toString().substring(1, jsonElement.getAsJsonObject().get("names").toString().length()-1));
                        drivers = parser.parse(jsonElement.getAsJsonObject().get("drivers").toString().substring(1, jsonElement.getAsJsonObject().get("drivers").toString().length()-1).replace("\\", "")).getAsJsonArray();
                        JsonArray names = parser.parse(jsonElement.getAsJsonObject().get("names").toString().substring(1, jsonElement.getAsJsonObject().get("names").toString().length()-1).replace("\\", "")).getAsJsonArray();
                        Log.e(TAG, names.get(0).toString());
                        for (JsonElement name : names) {
                            autocomplete.add(name.toString().replaceAll("\"", ""));
                        }

                        //Se ejecuta el juego
                        playGame();
                        Log.e(TAG, autocomplete.get(0));
                    } catch (IOException e) {
                        Toast.makeText(GameActivity.this, "No se han podido recuperar los datos de los pilotos", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                //En caso de que no se pueda realizar la petición se intentan recuperar los datos del archivo .json
                try {
                    // Obtener el directorio "Documents" en el almacenamiento interno de la aplicación
                    File documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

                    // Crear un objeto File apuntando al archivo JSON en el directorio "Documents"
                    File file = new File(documentsDirectory, "drivers.json");

                    // Verificar si el archivo existe
                    if (!file.exists()) {
                        Toast.makeText(GameActivity.this, "No existe el fichero drivers.json", Toast.LENGTH_SHORT).show();
                    }

                    // Leer el contenido del archivo JSON
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    // Cerrar los recursos
                    bufferedReader.close();
                    inputStreamReader.close();
                    fileInputStream.close();
                    JsonParser parser = new JsonParser();
                    // Convertir la cadena de texto a un elemento JSON
                    JsonElement jsonElement = parser.parse(stringBuilder.toString());
                    Log.e(TAG, jsonElement.getAsJsonObject().get("names").toString().substring(1, jsonElement.getAsJsonObject().get("names").toString().length()-1));
                    drivers = parser.parse(jsonElement.getAsJsonObject().get("drivers").toString().substring(1, jsonElement.getAsJsonObject().get("drivers").toString().length()-1).replace("\\", "")).getAsJsonArray();
                    JsonArray names = parser.parse(jsonElement.getAsJsonObject().get("names").toString().substring(1, jsonElement.getAsJsonObject().get("names").toString().length()-1).replace("\\", "")).getAsJsonArray();
                    Log.e(TAG, names.get(0).toString());
                    for (JsonElement name : names) {
                        autocomplete.add(name.toString().replaceAll("\"", ""));
                    }
                    //Se ejecuta el juego
                    playGame();
                    Log.e(TAG, autocomplete.get(0));
                } catch (IOException e) {
                    Toast.makeText(GameActivity.this, "No se han podido recuperar los datos de los pilotos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void playGame() {
        //Se ponen a 0 los intentos
        intentos = 0;
        idCount = R.id.driver1age; //Esta variable se usa para obtener el id de los elementos de la interfaz que se tienen que modificar.
        //Se crea la lista del desplegable de autocompletar
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autocomplete);
        driverInput.setAdapter(adapter);
        //Se elige al piloto que hay que adivinar de forma aleatoria
        Random random = new Random();
        int randomIndex = random.nextInt(drivers.size());
        driver = drivers.get(randomIndex);
    }

    private void clean() {
        idCount = R.id.driver1age;
        TextView textView;
        //Este bucle limpia los elementos de la pantalla para despejarla y poder volver a jugar
        for(int i=0; i<intentos; i++){
            textView = findViewById(idCount);
            textView.setText("");
            textView.setBackgroundColor(getResources().getColor(R.color.background));
            //Se incrementa el id para acceder el siguiente elemento de la interfaz
            idCount++;
            textView = findViewById(idCount);
            textView.setText("");
            textView.setBackgroundColor(getResources().getColor(R.color.background));
            idCount++;
            @SuppressLint("WrongViewCast") SVGImageView SVGimageView = findViewById(idCount);
            SVGimageView.setVisibility(View.INVISIBLE);
            SVGimageView.setBackgroundColor(getResources().getColor(R.color.background));
            idCount++;
            textView = findViewById(idCount);
            textView.setText("");
            textView.setBackgroundColor(getResources().getColor(R.color.background));
            idCount++;
            textView = findViewById(idCount);
            textView.setText("");
            textView.setBackgroundColor(getResources().getColor(R.color.background));
            idCount++;
            @SuppressLint("WrongViewCast") ImageView imageView = findViewById(idCount);
            imageView.setVisibility(View.INVISIBLE);
            imageView.setBackgroundColor(getResources().getColor(R.color.background));
            idCount++;
            textView = findViewById(idCount);
            textView.setText("");
            textView.setBackgroundColor(getResources().getColor(R.color.background));
            idCount++;
            idCount++;
        }
        //Se pone visible el boton de comprobar y invisibles el botón de reiniciar y el mensaje final
        textView = findViewById(R.id.message);
        textView.setVisibility(View.INVISIBLE);
        Button boton = findViewById(R.id.buttonPlayAgain);
        boton.setVisibility(View.INVISIBLE);
        boton = findViewById(R.id.buttonInput);
        boton.setVisibility(View.VISIBLE);
        driverInput.setVisibility(View.VISIBLE);
    }

    @SuppressLint("ResourceAsColor")
    public void check(View v) throws SVGParseException {
        //Este método se ejecuta al pulsar el botón de comprobar y comprueba si el piloto introducido coincide con el seleccionado aleatoriamente
        String input = driverInput.getText().toString().trim();
        String driverName;
        //Comprueba que llevas menos de 6 intentos
        if(intentos < 6){
            //Recorre el array de todos los pilotos hasta encontrar el piloto introducido
            for(int i = 0; i<drivers.size(); i++) {
                driverName = drivers.get(i).getAsJsonObject().get("firstName").toString().replaceAll("\"", "") + " " + drivers.get(i).getAsJsonObject().get("lastName").toString().replaceAll("\"", "");
                if (input.equals(driverName)) {
                    //Compara la edad del piloto introducido con el piloto secreto
                    TextView textView = findViewById(idCount);
                    textView.setText(String.valueOf(drivers.get(i).getAsJsonObject().get("age")));
                    if (drivers.get(i).getAsJsonObject().get("age").getAsInt() < driver.getAsJsonObject().get("age").getAsInt())
                        textView.setBackgroundColor(getResources().getColor(R.color.higher));
                    else if (drivers.get(i).getAsJsonObject().get("age").getAsInt() > driver.getAsJsonObject().get("age").getAsInt())
                        textView.setBackgroundColor(getResources().getColor(R.color.lower));
                    else textView.setBackgroundColor(Color.GREEN);
                    idCount++;
                    //Compara el año de debut
                    textView = findViewById(idCount);
                    textView.setText(String.valueOf(drivers.get(i).getAsJsonObject().get("firstYear")));
                    Log.e(TAG, String.valueOf(driver.getAsJsonObject().get("firstYear").getAsInt()));
                    Log.e(TAG, String.valueOf(drivers.get(i).getAsJsonObject().get("firstYear").getAsInt()));
                    if (drivers.get(i).getAsJsonObject().get("firstYear").getAsInt() < driver.getAsJsonObject().get("firstYear").getAsInt()) textView.setBackgroundColor(getResources().getColor(R.color.higher));
                    else if (drivers.get(i).getAsJsonObject().get("firstYear").getAsInt() > driver.getAsJsonObject().get("firstYear").getAsInt()) textView.setBackgroundColor(getResources().getColor(R.color.lower));
                    else textView.setBackgroundColor(Color.GREEN);
                    idCount++;
                    //Compara la nacionalidad de ambos pilotos
                    @SuppressLint("WrongViewCast") SVGImageView svgImageView = findViewById(idCount);
                    SVG svg = SVG.getFromInputStream(getResources().openRawResource(getResources().getIdentifier(drivers.get(i).getAsJsonObject().get("nationality").toString().replaceAll("\"", ""), "raw", getPackageName())));
                    svgImageView.setSVG(svg);
                    if (drivers.get(i).getAsJsonObject().get("nationality").toString().replaceAll("\"", "").equals(driver.getAsJsonObject().get("nationality").toString().replaceAll("\"", "")))
                        svgImageView.setBackgroundColor(Color.GREEN);
                    else svgImageView.setBackgroundColor(Color.RED);
                    svgImageView.setVisibility(View.VISIBLE);
                    idCount++;
                    //Muesta el nombre abreviado del piloto en su casilla
                    textView = findViewById(idCount);
                    textView.setText(drivers.get(i).getAsJsonObject().get("code").toString().replaceAll("\"", ""));
                    idCount++;
                    //Compara el número de ambos pilotos
                    textView = findViewById(idCount);
                    textView.setText(drivers.get(i).getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", ""));
                    if (Integer.parseInt(drivers.get(i).getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")) < Integer.parseInt(driver.getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")))
                        textView.setBackgroundColor(getResources().getColor(R.color.higher));
                    else if (Integer.parseInt(drivers.get(i).getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")) > Integer.parseInt(driver.getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")))
                        textView.setBackgroundColor(getResources().getColor(R.color.lower));
                    else textView.setBackgroundColor(Color.GREEN);
                    idCount++;
                    //Compara si el equipo del piloto introducido coincide con el último equipo del piloto secreto, o si ha corrido anteriormente para ese equipo
                    @SuppressLint("WrongViewCast") ImageView imageView = findViewById(idCount);
                    imageView.setImageResource(getResources().getIdentifier(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().get(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().size() - 1).toString().replaceAll("\"", ""), "drawable", getPackageName()));
                    if(driver.getAsJsonObject().get("constructors").getAsJsonArray().get(driver.getAsJsonObject().get("constructors").getAsJsonArray().size()-1).toString().replaceAll("\"", "").equals(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().get(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().size()-1).toString().replaceAll("\"", ""))){
                        imageView.setBackgroundColor(Color.GREEN);
                    }
                    else{
                        for(int j = 0; j<driver.getAsJsonObject().get("constructors").getAsJsonArray().size(); j++){
                            if(driver.getAsJsonObject().get("constructors").getAsJsonArray().get(j).toString().replaceAll("\"", "").equals(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().get(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().size()-1).toString().replaceAll("\"", ""))){
                                imageView.setBackgroundColor(getResources().getColor(R.color.higher));
                                break;
                            }else imageView.setBackgroundColor(Color.RED);
                        }
                    }
                    imageView.setVisibility(View.VISIBLE);
                    idCount++;
                    //Compara las victorias de ambos pilotos
                    textView = findViewById(idCount);
                    textView.setText(String.valueOf(drivers.get(i).getAsJsonObject().get("wins")));
                    if (drivers.get(i).getAsJsonObject().get("wins").getAsInt() < driver.getAsJsonObject().get("wins").getAsInt())
                        textView.setBackgroundColor(getResources().getColor(R.color.higher));
                    else if (drivers.get(i).getAsJsonObject().get("wins").getAsInt() > driver.getAsJsonObject().get("wins").getAsInt())
                        textView.setBackgroundColor(getResources().getColor(R.color.lower));
                    else textView.setBackgroundColor(Color.GREEN);
                    idCount++;
                    idCount++;
                    //Elimina el piloto introducido del desplegable de autocompletar
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) driverInput.getAdapter();
                    if (adapter != null) {
                        adapter.remove(drivers.get(i).getAsJsonObject().get("firstName").toString().replaceAll("\"", "")+" "+drivers.get(i).getAsJsonObject().get("lastName").toString().replaceAll("\"", ""));
                        adapter.notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado
                    }
                    //Incrementa el número de intentos
                    intentos++;
                }
            }
            driverInput.setText("");
            driverName = driver.getAsJsonObject().get("firstName").toString().replaceAll("\"", "")+" "+driver.getAsJsonObject().get("lastName").toString().replaceAll("\"", "");
            if(input.equals(driverName)){
                //Si se acierta el piloto se muestra el boton de volver a jugar y el mensaje de has ganado
                TextView textView = findViewById(R.id.message);
                textView.setText(getString(R.string.you_win)+" "+intentos+" "+getString(R.string.attempts));
                textView.setVisibility(View.VISIBLE);
                driverInput.setVisibility(View.INVISIBLE);
                Button boton = findViewById(R.id.buttonInput);
                boton.setVisibility(View.INVISIBLE);
                boton = findViewById(R.id.buttonPlayAgain);
                boton.setVisibility(View.VISIBLE);
            }
            else{
                //Si se llega a los 6 intentos se muestra el boton de volver a jugar, con el mensaje de has perdido y el nombre del piloto secreto
                if (intentos == 6){
                    driverInput.setVisibility(View.INVISIBLE);
                    Button boton = findViewById(R.id.buttonInput);
                    boton.setVisibility(View.INVISIBLE);
                    driverName = driver.getAsJsonObject().get("firstName").toString().replaceAll("\"", "")+" "+driver.getAsJsonObject().get("lastName").toString().replaceAll("\"", "");
                    TextView textView;
                    textView = findViewById(R.id.message);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.game_over)+" "+driverName);
                    boton = findViewById(R.id.buttonPlayAgain);
                    boton.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    //Metodo para reiniciar el juego asociado al boton de volver a jugar
    public void playAgain(View view) {
        clean();
        playGame();
    }
    @Override
    //Metodo para crear el menú
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    //Metodo para seleccionar las opciones del menú
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