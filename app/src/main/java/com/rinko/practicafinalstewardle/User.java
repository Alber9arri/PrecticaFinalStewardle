package com.rinko.practicafinalstewardle;

public class User {
    private int id;
    private String username;
    private String password;
    private int played;
    private int won;
    private int lost;

    // Constructor
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.played = 0;
        this.won = 0;
        this.lost = 0;
    }

    //Getters y Setters de id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    //Getter de username
    public String getUsername() {
        return username;
    }

    //Getter de password
    public String getPassword() {
        return password;
    }

    //Getters y Setters de won
    public int getWon() {
        return won;
    }

    public void setWon(int won) {
        this.won = won;
    }
}