package com.example.videomeet.models;

import java.io.Serializable;

public class User implements Serializable {
    public String Name;
    public String Email;
    public String Password;
    public String Token;

    public User(){

    }

    public User(String name, String email, String password){
        this.Name = name;
        this.Email = email;
        this.Password = password;
    }

    public User(String email, String password){
        this.Email = email;
        this.Password = password;
    }
}
