package com.example.balaghi.models;

import java.io.Serializable;

public class Report implements Serializable {

    private int id;
    private String title;
    private String description;
    private String category;
    private String status;
    private String location;
    private String image;
    private String date;
    private boolean isAnonymous;
    private int userId;

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    private String extractedText;

    public Report() {
    }

    public Report(int id, String title, String description,
                  String category, String status,
                  String location, String image,
                  String date, boolean isAnonymous,
                  int userId) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.location = location;
        this.image = image;
        this.date = date;
        this.isAnonymous = isAnonymous;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}