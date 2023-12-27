package com.example.practicafinalstewardle;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class UserRepository {

    private SQLiteDatabase database;
    private DBHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addUser(User user) {
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("played", 0);
        values.put("won", 0);
        values.put("lost", 0);

        return database.insert("users", null, values);
    }

    public boolean checkUser(String username, String password) {
        String[] columns = {"id"};
        String selection = "username=? and password=?";
        String[] selectionArgs = {username, password};

        try (Cursor cursor = database.query("users", columns, selection, selectionArgs, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    public int getPlayed(User user){
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

    public int getWon(User user){
        // Obtemos los valores de won desde la base de datos
        String[] columns = {"won"};
        String selection = "username=?";
        String[] selectionArgs = {user.getUsername()};

        Cursor cursor = database.query("users", columns, selection, selectionArgs, null, null, null);

        int won = 0;

        if (cursor != null && cursor.moveToFirst()) {
            won = cursor.getInt(cursor.getColumnIndex("won"));
            cursor.close();
        }

        return won;
    }

    public int getLost(User user){
        // Obtemos los valores de lost desde la base de datos
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

    public void updatePlayed(User user) {
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

        // Incrementa los valores
        ContentValues values = new ContentValues();
        values.put("played", played + 1);

        // Actualiza la base de datos con los nuevos valores
        database.update("users", values, selection, selectionArgs);
    }

    public void updateWon(User user) {
        // Obtemos los valores de won desde la base de datos
        String[] columns = {"won"};
        String selection = "username=?";
        String[] selectionArgs = {user.getUsername()};

        Cursor cursor = database.query("users", columns, selection, selectionArgs, null, null, null);

        int won = 0;

        if (cursor != null && cursor.moveToFirst()) {
            won = cursor.getInt(cursor.getColumnIndex("won"));
            cursor.close();
        }

        // Incrementa los valores
        ContentValues values = new ContentValues();
        values.put("won", won + 1);

        // Actualiza la base de datos con los nuevos valores
        database.update("users", values, selection, selectionArgs);
    }

    public void updateLost(User user) {
        // Obtemos los valores de lost desde la base de datos
        String[] columns = {"lost"};
        String selection = "username=?";
        String[] selectionArgs = {user.getUsername()};

        Cursor cursor = database.query("users", columns, selection, selectionArgs, null, null, null);

        int lost = 0;

        if (cursor != null && cursor.moveToFirst()) {
            lost = cursor.getInt(cursor.getColumnIndex("lost"));
            cursor.close();
        }

        // Incrementa los valores
        ContentValues values = new ContentValues();
        values.put("lost", lost + 1);

        // Actualiza la base de datos con los nuevos valores
        database.update("users", values, selection, selectionArgs);
    }
}
