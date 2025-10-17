package com.itemfinder.midtermappdev.LoginAndProfile;

public class FoundItem {
    private String itemName;
    private String itemDescription;
    private String itemLocation;
    private String itemDate;
    private String itemTime;
    private String category;
    private String status;
    private String imageUrl;
    private String handedStatus;
    private String documentId; // This will store the Firestore document ID

    public FoundItem() {} // Firestore requires empty constructor

    public FoundItem(String itemName, String itemDescription, String itemLocation, String itemDate,
                     String itemTime, String category, String status, String imageUrl,
                     String handedStatus, String documentId) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemLocation = itemLocation;
        this.itemDate = itemDate;
        this.itemTime = itemTime;
        this.category = category;
        this.status = status;
        this.imageUrl = imageUrl;
        this.handedStatus = handedStatus;
        this.documentId = documentId;
    }

    // Getters
    public String getItemName() { return itemName; }
    public String getItemDescription() { return itemDescription; }
    public String getItemLocation() { return itemLocation; }
    public String getItemDate() { return itemDate; }
    public String getItemTime() { return itemTime; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public String getHandedStatus() { return handedStatus; }
    public String getUserId() { return documentId; } // Keeping this for compatibility with adapter
    public String getDocumentId() { return documentId; }

    // Setters
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }
    public void setItemLocation(String itemLocation) { this.itemLocation = itemLocation; }
    public void setItemDate(String itemDate) { this.itemDate = itemDate; }
    public void setItemTime(String itemTime) { this.itemTime = itemTime; }
    public void setCategory(String category) { this.category = category; }
    public void setStatus(String status) { this.status = status; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setHandedStatus(String handedStatus) { this.handedStatus = handedStatus; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}