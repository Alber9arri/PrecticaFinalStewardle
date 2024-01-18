package com.rinko.practicafinalstewardle;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    //Crea una base de datos llamada app_db
    private static final String DATABASE_NAME = "app_db";
    private static final int DATABASE_VERSION = 1;

    //Crea la tabla users dentro de la base de datos app_db
    private static final String CREATE_TABLE_USERS = "CREATE TABLE users " +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, played INTEGER, won INTEGER, lost INTEGER)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Inicializa la base de datos cuando se crea por primera vez
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
    }

    //Inicializa la base de datos cuando se crea por primera vez
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}

