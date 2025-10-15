package com.itemfinder.midtermappdev.Find;

public class Item {
    private String name;
    private String category;
    private String location;
    private String status;
    private String date;

    public Item(String name, String category, String location, String status, String date) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.status = status;
        this.date = date;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
}
