package com.example.hospitaluniversitario.models;

import java.util.Locale;

public class User {
    private String uid;
    private String email;
    private String name;
    private String role;


    public User() {

    }


    public User(String uid, String email, String name, String role) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = role;
    }


    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }


    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role != null ? role.toLowerCase(Locale.ROOT) : null;
    }
}
