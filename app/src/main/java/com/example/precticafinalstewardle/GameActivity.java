package com.example.precticafinalstewardle;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import java.util.ArrayList;
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
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://stewardle.com/";
    private ApiService apiService;
    private JsonArray drivers = new JsonArray();
    ArrayList<String> autocomplete = new ArrayList<>();
    private MultiAutoCompleteTextView driverInput;
    private JsonElement driver;
    private int intentos;
    private int idCount = R.id.driver1age;
    private int idSVGImageCount = R.id.driver1flag;
    private int idImageCount = R.id.driver1team;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
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
                    playGame();
                } else {
                    //Manejar error de respuesta
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                //Manejar error de peticion
            }
        });
    }
    private void playGame() {
        intentos = 0;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autocomplete);
        driverInput.setAdapter(adapter);
        driverInput.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        Random random = new Random();
        int randomIndex = random.nextInt(drivers.size());
        driver = drivers.get(randomIndex);
        Log.e(TAG, driver.getAsJsonObject().get("firstName").toString());
    }

    public void check(View v) throws SVGParseException {
        String input = driverInput.getText().toString();
        Log.e(TAG, input);
        String driverName = driver.getAsJsonObject().get("firstName").toString().replaceAll("\"", "")+" "+driver.getAsJsonObject().get("lastName").toString().replaceAll("\"", "");
        if(intentos < 6){
            if(input.equals(driverName)){
                Log.e(TAG, driverName);
            }
            else{
                for(int i = 0; i<drivers.size(); i++){
                    driverName = drivers.get(i).getAsJsonObject().get("firstName").toString().replaceAll("\"", "")+" "+drivers.get(i).getAsJsonObject().get("lastName").toString().replaceAll("\"", "");
                    if(input.equals(driverName)){
                        Log.e(TAG, drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().get(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().size()-1).toString());

                        TextView textView = findViewById(idCount);
                        textView.setText(String.valueOf(drivers.get(i).getAsJsonObject().get("age")));
                        idCount++;
                        textView = findViewById(idCount);
                        textView.setText(String.valueOf(drivers.get(i).getAsJsonObject().get("firstYear")));
                        idCount++;
                        @SuppressLint("WrongViewCast") SVGImageView svgImageView = findViewById(idCount);
                        SVG svg = SVG.getFromInputStream(getResources().openRawResource(getResources().getIdentifier(drivers.get(i).getAsJsonObject().get("nationality").toString().replaceAll("\"", ""), "raw", getPackageName())));
                        svgImageView.setSVG(svg);
                        idCount++;
                        textView = findViewById(idCount);
                        textView.setText(drivers.get(i).getAsJsonObject().get("code").toString().replaceAll("\"", ""));
                        idCount++;
                        textView = findViewById(idCount);
                        textView.setText(drivers.get(i).getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", ""));
                        idCount++;
                        @SuppressLint("WrongViewCast") ImageView imageView = findViewById(idCount);
                        imageView.setImageResource(getResources().getIdentifier(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().get(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().size()-1).toString().replaceAll("\"", ""), "drawable", getPackageName()));
                        idCount++;
                        textView = findViewById(idCount);
                        textView.setText(String.valueOf(drivers.get(i).getAsJsonObject().get("wins")));
                        idCount++;
                        idCount++;
                        Log.e(TAG, String.valueOf(idCount));
                        Log.e(TAG, String.valueOf(R.id.driver3age));
                        intentos++;
                    }
                }

            }
        }

    }
}