package com.example.arranger;

public class User{
    public String email;
    public String name;
    public String profilePicUrl;
    public String placeOfWork;
    public String department;
    public String post;

    public User(){}

    public User(String email,String name){
        this.email = email;
        this.name = name;
    }

    public User(String email,String name, String profilePicUrl){
        this.email = email;
        this.name = name;
        this.profilePicUrl = profilePicUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                '}';
    }
}
