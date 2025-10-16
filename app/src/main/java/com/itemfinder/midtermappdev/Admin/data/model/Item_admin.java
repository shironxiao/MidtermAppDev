package com.itemfinder.midtermappdev.Admin.data.model;

public class Item_admin {
    private String id;
    private String name;
    private String description;
    private String status;
    private String imageUrl;
    private long timestamp;

    // ✅ NEW FIELDS from Report section
    private String category;           // "Electronics", "Books", etc.
    private String foundLocation;      // "Library", "Cafeteria", etc.
    private String dateFound;          // "2025-10-10"
    private String photoUrl;           // URL to photo
    private String contactInfo;        // "09123456789"
    private boolean anonymous;         // true/false

    public Item_admin() {} // Required for Firestore

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public long getTimestamp() { return timestamp; }
    public String getCategory() { return category; }
    public String getFoundLocation() { return foundLocation; }
    public String getDateFound() { return dateFound; }
    public String getPhotoUrl() { return photoUrl; }
    public String getContactInfo() { return contactInfo; }
    public boolean isAnonymous() { return anonymous; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setCategory(String category) { this.category = category; }
    public void setFoundLocation(String foundLocation) { this.foundLocation = foundLocation; }
    public void setDateFound(String dateFound) { this.dateFound = dateFound; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
}