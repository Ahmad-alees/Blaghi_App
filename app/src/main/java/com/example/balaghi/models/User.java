package com.example.balaghi.models;

import java.io.Serializable;

public class User implements Serializable {

    private int id;
    private String fullName;
    private String userName;
    private String email;
    private String phone;
    private String password;
    private String birthDate;
    private String profileImage;

    public User(){
    }
    public User(int id , String fullName , String userName
            , String email , String phone , String password, String birthDate, String profileImage){
        this.id=id;
         this.fullName=fullName;
        this.userName=userName;
        this.email=email;
        this.phone=phone;
        this.password=password;
        this.birthDate=birthDate;
        this.profileImage=profileImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return userName;
    }

    public void setUsername(String username) {
        this.userName = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}