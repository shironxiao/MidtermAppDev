package com.itemfinder.midtermappdev.Find;

public class Item {
    private String name;
    private String category;
    private String location;
    private String status;
    private String date;
    private String imageUrl;

    public Item(String name, String category, String location, String status, String date) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.status = status;
        this.date = date;
        this.imageUrl = null;
    }

    public Item(String name, String category, String location, String status, String date, String imageUrl) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.status = status;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}