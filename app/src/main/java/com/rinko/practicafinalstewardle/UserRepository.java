package com.rinko.practicafinalstewardle;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private SQLiteDatabase database;
    private DBHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = new DBHelper(context);
    }

    //Abre la base de datos, para permitir el introducir nuevos datos
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    //Cierra la base de datos
    public void close() {
        dbHelper.close();
    }

    //Méotodo que añade un usuario a la base de datos
    public long addUser(User user) {
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("played", 0);
        values.put("won", 0);
        values.put("lost", 0);

        return database.insert("users", null, values);
    }

    //Método para comprobar que exista el usuario en la base de datos
    public boolean checkUser(String username, String password) {
        String[] columns = {"id"};
        String selection = "username=? and password=?";
        String[] selectionArgs = {username, password};

        try (Cursor cursor = database.query("users", columns, selection, selectionArgs, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    //Método para obtener las partidas jugadas
    @SuppressLint("Range")
    public int getPlayed(User user) {
        // Obtemos los valores de played desde la base de datos
        String[] columns = {"played"};
        String selection = "username=?";
        String[] selectionArgs = {user.getUsername()};

        Cursor cursor = database.query("users", columns, selection, selectionArgs, null, null, null);

        int played = 0;

        if (cursor != null && cursor.moveToFirst()) {
            played = cursor.getInt(cursor.getColumnIndex("played"));
            cursor.close();
        }

        return played;
    }

    //Método para obtener las partidas ganadas
    @SuppressLint("Range")
    public int getWon(User user) {
        // Obtemos los valores de won desde la base de datos
        String[] columns = {"won"};
        String selection = "username=?";
        String[] selectionArgs = {user.getUsername()};

        Cursor cursor = database.query("users", columns, selection, selectionArgs, null, null, null);
        Log.e(TAG, "String i");


        int won = 0;

        if (cursor != null && cursor.moveToFirst()) {
            won = cursor.getInt(cursor.getColumnIndex("won"));
            cursor.close();
        }

        return won;
    }

    //Método para obtener las partidas perdidas
    @SuppressLint("Range")
    public int getLost(User user) {
        //Obtemos los valores de lost desde la base de datos
        String[] columns = {"lost"};
        String selection = "username=?";
        String[] selectionArgs = {user.getUsername()};

        Cursor cursor = database.query("users", columns, selection, selectionArgs, null, null, null);

        int lost = 0;

        if (cursor != null && cursor.moveToFirst()) {
            lost = cursor.getInt(cursor.getColumnIndex("lost"));
            cursor.close();
        }

        return lost;
    }

    //Método para obtener el Top5 de los usuarios con más partidas ganadas
    public List<User> getTopFiveUsers() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        List<User> topUsers = new ArrayList<>();

        //Seleccionamos las columnas que queremos obtener de la DB y las ordenamos por victorias descendentes (en RankingActivity se limitarán a 5 los usuarios)
        String[] columns = {"username", "won"};
        String orderBy = "won DESC";

        Cursor cursor = database.query("users", columns, null, null, null, null, orderBy);

        //Extraemos la información de la DB, creamos objetos tipo User con estos datos y los agregamos al array topUsers
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex("username"));
                @SuppressLint("Range") int won = cursor.getInt(cursor.getColumnIndex("won"));

                User user = new User(username, "");
                user.setWon(won);

                topUsers.add(user);
            } while (cursor.moveToNext());

            cursor.close();
        }

        //Se cierra la DB
        database.close();

        return topUsers;
    }

    //Método para actualizar las partidas jugadas
    public void updatePlayed(User user) {

        int played = getPlayed(user);

        // Incrementa los valores
        ContentValues values = new ContentValues();
        values.put("played", played + 1);

        String selection = "username=?";
        String[] selectionArgs = {user.getUsername()};

        // Actualiza la base de datos con los nuevos valores
        database.update("users", values, selection, selectionArgs);
    }

    //Método para actualizar las partidas ganadas
    public void updateWon(User user) {

        int won = getWon(user);

        // Incrementa los valores
        ContentValues values = new ContentValues();
        values.put("won", won + 1);

        String selection = "username=?";
        String[] selectionArgs = {user.getUsername()};

        // Actualiza la base de datos con los nuevos valores
        database.update("users", values, selection, selectionArgs);
    }

    //Método para actualizar las partidas perdidas
    public void updateLost(User user) {

        int lost = getLost(user);

        // Incrementa los valores
        ContentValues values = new ContentValues();
        values.put("lost", lost + 1);

        String selection = "username=?";
        String[] selectionArgs = {user.getUsername()};

        // Actualiza la base de datos con los nuevos valores
        database.update("users", values, selection, selectionArgs);
    }
}
