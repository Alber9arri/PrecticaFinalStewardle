package com.example.precticafinalstewardle;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

import org.w3c.dom.Text;

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
            idCount++;
            textView = findViewById(idCount);
            textView.setText("");
            idCount++;
            @SuppressLint("WrongViewCast") SVGImageView SVGimageView = findViewById(idCount);
            SVGimageView.setVisibility(View.INVISIBLE);
            idCount++;
            textView = findViewById(idCount);
            textView.setText("");
            idCount++;
            textView = findViewById(idCount);
            textView.setText("");
            idCount++;
            @SuppressLint("WrongViewCast") ImageView imageView = findViewById(idCount);
            imageView.setVisibility(View.INVISIBLE);
            idCount++;
            textView = findViewById(idCount);
            textView.setText("");
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
                        textView.setTextColor(Color.RED);
                    else if (drivers.get(i).getAsJsonObject().get("age").getAsInt() > driver.getAsJsonObject().get("age").getAsInt())
                        textView.setTextColor(Color.BLACK);
                    else textView.setTextColor(Color.GREEN);
                    idCount++;
                    textView = findViewById(idCount);
                    textView.setText(String.valueOf(drivers.get(i).getAsJsonObject().get("firstYear")));
                    if (drivers.get(i).getAsJsonObject().get("firstYear").getAsInt() < driver.getAsJsonObject().get("firstYear").getAsInt())
                        textView.setTextColor(Color.RED);
                    else if (drivers.get(i).getAsJsonObject().get("firstYear").getAsInt() > driver.getAsJsonObject().get("firstYear").getAsInt())
                        textView.setTextColor(Color.BLACK);
                    else textView.setTextColor(Color.GREEN);
                    idCount++;
                    @SuppressLint("WrongViewCast") SVGImageView svgImageView = findViewById(idCount);
                    SVG svg = SVG.getFromInputStream(getResources().openRawResource(getResources().getIdentifier(drivers.get(i).getAsJsonObject().get("nationality").toString().replaceAll("\"", ""), "raw", getPackageName())));
                    svgImageView.setSVG(svg);
                    svgImageView.setVisibility(View.VISIBLE);
                    idCount++;
                    textView = findViewById(idCount);
                    textView.setText(drivers.get(i).getAsJsonObject().get("code").toString().replaceAll("\"", ""));
                    idCount++;
                    textView = findViewById(idCount);
                    textView.setText(drivers.get(i).getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", ""));
                    if (Integer.parseInt(drivers.get(i).getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")) < Integer.parseInt(driver.getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")))
                        textView.setTextColor(Color.RED);
                    else if (Integer.parseInt(drivers.get(i).getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")) > Integer.parseInt(driver.getAsJsonObject().get("permanentNumber").toString().replaceAll("\"", "")))
                        textView.setTextColor(Color.BLACK);
                    else textView.setTextColor(Color.GREEN);
                    idCount++;
                    @SuppressLint("WrongViewCast") ImageView imageView = findViewById(idCount);
                    imageView.setImageResource(getResources().getIdentifier(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().get(drivers.get(i).getAsJsonObject().get("constructors").getAsJsonArray().size() - 1).toString().replaceAll("\"", ""), "drawable", getPackageName()));
                    imageView.setVisibility(View.VISIBLE);
                    idCount++;
                    textView = findViewById(idCount);
                    textView.setText(String.valueOf(drivers.get(i).getAsJsonObject().get("wins")));
                    if (drivers.get(i).getAsJsonObject().get("wins").getAsInt() < driver.getAsJsonObject().get("wins").getAsInt())
                        textView.setTextColor(Color.RED);
                    else if (drivers.get(i).getAsJsonObject().get("wins").getAsInt() > driver.getAsJsonObject().get("wins").getAsInt())
                        textView.setTextColor(Color.BLACK);
                    else textView.setTextColor(Color.GREEN);
                    idCount++;
                    idCount++;
                    driverInput.setText("");
                    intentos++;
                }
            }
                driverName = driver.getAsJsonObject().get("firstName").toString().replaceAll("\"", "")+" "+driver.getAsJsonObject().get("lastName").toString().replaceAll("\"", "");
            if(input.equals(driverName)){
                TextView textView = findViewById(R.id.message);
                textView.setText(getString(R.string.you_win)+" En "+intentos+" intentos");
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
}