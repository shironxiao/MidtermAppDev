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
    private String userId;

    public FoundItem() {} // Firestore requires empty constructor

    public FoundItem(String itemName, String itemDescription, String itemLocation, String itemDate,
                     String itemTime, String category, String status, String imageUrl,
                     String handedStatus, String userId) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemLocation = itemLocation;
        this.itemDate = itemDate;
        this.itemTime = itemTime;
        this.category = category;
        this.status = status;
        this.imageUrl = imageUrl;
        this.handedStatus = handedStatus;
        this.userId = userId;
    }

    public String getItemName() { return itemName; }
    public String getItemDescription() { return itemDescription; }
    public String getItemLocation() { return itemLocation; }
    public String getItemDate() { return itemDate; }
    public String getItemTime() { return itemTime; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }

    public String getHandedStatus() { return handedStatus; }
    public String getUserId() { return userId; }
}
