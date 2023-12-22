package com.example.practicafinalstewardle;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.io.Writer;
import java.lang.reflect.Type;
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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

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
    private int idSVGImageCount = R.id.driver1flag;
    private int idImageCount = R.id.driver1team;
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
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
        // Configura Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        // Crea una instancia de ApiService
        apiService = retrofit.create(ApiService.class);

        // Llama al método para obtener la respuesta como una cadena
        Call<String> call = apiService.getData();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String responseData = response.body();
                    JsonParser jsonParser = new JsonParser();
                    JsonObject jsonObject = jsonParser.parse(responseData).getAsJsonObject();
                    Set<String> keys = jsonObject.keySet();
                    for (String key : keys) {
                        drivers.add(jsonObject.get(key));
                        autocomplete.add(jsonObject.get(key).getAsJsonObject().get("firstName").toString().replaceAll("\"", "")+" "+jsonObject.get(key).getAsJsonObject().get("lastName").toString().replaceAll("\"", ""));
                    }
                    JSONObject finalobject = new JSONObject();
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
                    playGame();
                } else {
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
                        playGame();
                        Log.e(TAG, autocomplete.get(0));
                    } catch (IOException e) {
                        Toast.makeText(GameActivity.this, "No se han podido recuperar los datos de los pilotos", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
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
                    playGame();
                    Log.e(TAG, autocomplete.get(0));
                } catch (IOException e) {
                    Toast.makeText(GameActivity.this, "No se han podido recuperar los datos de los pilotos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void playGame() {
        intentos = 0;
        idCount = R.id.driver1age;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autocomplete);
        driverInput.setAdapter(adapter);
        //driverInput.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        Random random = new Random();
        int randomIndex = random.nextInt(drivers.size());
        driver = drivers.get(randomIndex);
        Log.e(TAG, driver.getAsJsonObject().get("firstName").toString());
    }

    private void clean() {
        idCount = R.id.driver1age;
        TextView textView;
        for(int i=0; i<intentos; i++){
            textView = findViewById(idCount);
            textView.setText("");
            textView.setBackgroundColor(getResources().getColor(R.color.background));
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
        String input = driverInput.getText().toString().trim();
        Log.e(TAG, input);
        String driverName;
        if(intentos < 6){
            for(int i = 0; i<drivers.size(); i++) {
                driverName = drivers.get(i).getAsJsonObject().get("firstName").toString().replaceAll("\"", "") + " " + drivers.get(i).getAsJsonObject().get("lastName").toString().replaceAll("\"", "");
                if (input.equals(driverName)) {
                    Log.e(TAG, drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().get(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().size() - 1).toString());

                    TextView textView = findViewById(idCount);
                    textView.setText(String.valueOf(drivers.get(i).getAsJsonObject().get("age")));
                    if (drivers.get(i).getAsJsonObject().get("age").getAsInt() < driver.getAsJsonObject().get("age").getAsInt())
                        textView.setBackgroundColor(getResources().getColor(R.color.higher));
                    else if (drivers.get(i).getAsJsonObject().get("age").getAsInt() > driver.getAsJsonObject().get("age").getAsInt())
                        textView.setBackgroundColor(getResources().getColor(R.color.lower));
                    else textView.setBackgroundColor(Color.GREEN);
                    idCount++;
                    textView = findViewById(idCount);
                    textView.setText(String.valueOf(drivers.get(i).getAsJsonObject().get("firstYear")));
                    Log.e(TAG, String.valueOf(driver.getAsJsonObject().get("firstYear").getAsInt()));
                    Log.e(TAG, String.valueOf(drivers.get(i).getAsJsonObject().get("firstYear").getAsInt()));
                    if (drivers.get(i).getAsJsonObject().get("firstYear").getAsInt() < driver.getAsJsonObject().get("firstYear").getAsInt()) textView.setBackgroundColor(getResources().getColor(R.color.higher));
                    else if (drivers.get(i).getAsJsonObject().get("firstYear").getAsInt() > driver.getAsJsonObject().get("firstYear").getAsInt()) textView.setBackgroundColor(getResources().getColor(R.color.lower));
                    else textView.setBackgroundColor(Color.GREEN);
                    idCount++;
                    @SuppressLint("WrongViewCast") SVGImageView svgImageView = findViewById(idCount);
                    SVG svg = SVG.getFromInputStream(getResources().openRawResource(getResources().getIdentifier(drivers.get(i).getAsJsonObject().get("nationality").toString().replaceAll("\"", ""), "raw", getPackageName())));
                    svgImageView.setSVG(svg);
                    if (drivers.get(i).getAsJsonObject().get("nationality").toString().replaceAll("\"", "").equals(driver.getAsJsonObject().get("nationality").toString().replaceAll("\"", "")))
                        svgImageView.setBackgroundColor(Color.GREEN);
                    else svgImageView.setBackgroundColor(Color.RED);
                    svgImageView.setVisibility(View.VISIBLE);
                    idCount++;
                    textView = findViewById(idCount);
                    textView.setText(drivers.get(i).getAsJsonObject().get("code").toString().replaceAll("\"", ""));
                    idCount++;
                    textView = findViewById(idCount);
                    textView.setText(drivers.get(i).getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", ""));
                    if (Integer.parseInt(drivers.get(i).getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")) < Integer.parseInt(driver.getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")))
                        textView.setBackgroundColor(getResources().getColor(R.color.higher));
                    else if (Integer.parseInt(drivers.get(i).getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")) > Integer.parseInt(driver.getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")))
                        textView.setBackgroundColor(getResources().getColor(R.color.lower));
                    else textView.setBackgroundColor(Color.GREEN);
                    idCount++;
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
                    textView = findViewById(idCount);
                    textView.setText(String.valueOf(drivers.get(i).getAsJsonObject().get("wins")));
                    if (drivers.get(i).getAsJsonObject().get("wins").getAsInt() < driver.getAsJsonObject().get("wins").getAsInt())
                        textView.setBackgroundColor(getResources().getColor(R.color.higher));
                    else if (drivers.get(i).getAsJsonObject().get("wins").getAsInt() > driver.getAsJsonObject().get("wins").getAsInt())
                        textView.setBackgroundColor(getResources().getColor(R.color.lower));
                    else textView.setBackgroundColor(Color.GREEN);
                    idCount++;
                    idCount++;
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) driverInput.getAdapter();
                    if (adapter != null) {
                        adapter.remove(drivers.get(i).getAsJsonObject().get("firstName").toString().replaceAll("\"", "")+" "+drivers.get(i).getAsJsonObject().get("lastName").toString().replaceAll("\"", ""));
                        adapter.notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado
                    }
                    intentos++;
                }
            }
            driverInput.setText("");
            driverName = driver.getAsJsonObject().get("firstName").toString().replaceAll("\"", "")+" "+driver.getAsJsonObject().get("lastName").toString().replaceAll("\"", "");
            if(input.equals(driverName)){
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

    public void playAgain(View view) {
        clean();
        playGame();
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