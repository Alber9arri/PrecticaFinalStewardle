package com.example.practicafinalstewardle;

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

    // Getter and Setter methods for id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and Setter methods for username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getter and Setter methods for password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getter and Setter methods for played
    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    // Getter and Setter methods for won
    public int getWon() {
        return won;
    }

    public void setWon(int won) {
        this.won = won;
    }

    // Getter and Setter methods for lost
    public int getLost() {
        return lost;
    }

    public void setLost(int lost) {
        this.lost = lost;
    }
}