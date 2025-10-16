package com.itemfinder.midtermappdev.Admin.data.model;

public class Claim {
    private String id;
    private String itemId;
    private String claimantName;
    private String claimantEmail;
    private String claimantPhone;
    private String description;
    private String status; // PENDING, APPROVED, REJECTED
    private long claimDate;
    private String itemName;

    public Claim() {} // Required for Firebase

    public Claim(String id, String itemId, String claimantName, String claimantEmail,
                 String claimantPhone, String description, String status, long claimDate, String itemName) {
        this.id = id;
        this.itemId = itemId;
        this.claimantName = claimantName;
        this.claimantEmail = claimantEmail;
        this.claimantPhone = claimantPhone;
        this.description = description;
        this.status = status;
        this.claimDate = claimDate;
        this.itemName = itemName;
    }

    // Getters
    public String getId() { return id; }
    public String getItemId() { return itemId; }
    public String getClaimantName() { return claimantName; }
    public String getClaimantEmail() { return claimantEmail; }
    public String getClaimantPhone() { return claimantPhone; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public long getClaimDate() { return claimDate; }
    public String getItemName() { return itemName; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setClaimantName(String claimantName) { this.claimantName = claimantName; }
    public void setClaimantEmail(String claimantEmail) { this.claimantEmail = claimantEmail; }
    public void setClaimantPhone(String claimantPhone) { this.claimantPhone = claimantPhone; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setClaimDate(long claimDate) { this.claimDate = claimDate; }
    public void setItemName(String itemName) { this.itemName = itemName; }
}