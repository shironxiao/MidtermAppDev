package com.itemfinder.midtermappdev.Admin.data.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.List;

@IgnoreExtraProperties
public class Claim {

    private String id;
    private String itemId;
    private String claimantName;
    private String claimantId;
    private String claimantEmail;
    private String claimantPhone;
    private String description;
    private String status; // PENDING, APPROVED, REJECTED, CLAIMED
    private long claimDate;
    private String itemName;
    private String claimLocation;
    private List<String> proofImages;

    // Item details
    private String itemCategory;
    private String itemLocation;
    private String itemDate;
    private String itemImageUrl;

    // 🔹 Added fields
    private String userId;
    private long approvedAt; // ✅ FIXED: should be long, not String

    // Required no-argument constructor for Firestore
    public Claim() {}

    public Claim(String id, String itemId, String claimantName, String claimantId,
                 String claimantEmail, String claimantPhone, String description,
                 String status, long claimDate, String itemName) {
        this.id = id;
        this.itemId = itemId;
        this.claimantName = claimantName;
        this.claimantId = claimantId;
        this.claimantEmail = claimantEmail;
        this.claimantPhone = claimantPhone;
        this.description = description;
        this.status = status;
        this.claimDate = claimDate;
        this.itemName = itemName;
    }

    // 🔹 Getters
    public String getId() { return id; }
    public String getItemId() { return itemId; }
    public String getClaimantName() { return claimantName; }
    public String getClaimantId() { return claimantId; }
    public String getClaimantEmail() { return claimantEmail; }
    public String getClaimantPhone() { return claimantPhone; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public long getClaimDate() { return claimDate; }
    public String getItemName() { return itemName; }
    public String getClaimLocation() { return claimLocation; }
    public List<String> getProofImages() { return proofImages; }
    public String getItemCategory() { return itemCategory; }
    public String getItemLocation() { return itemLocation; }
    public String getItemDate() { return itemDate; }
    public String getItemImageUrl() { return itemImageUrl; }
    public String getUserId() { return userId; }
    public long getApprovedAt() { return approvedAt; } // ✅ FIXED

    // 🔹 Setters
    public void setId(String id) { this.id = id; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setClaimantName(String claimantName) { this.claimantName = claimantName; }
    public void setClaimantId(String claimantId) { this.claimantId = claimantId; }
    public void setClaimantEmail(String claimantEmail) { this.claimantEmail = claimantEmail; }
    public void setClaimantPhone(String claimantPhone) { this.claimantPhone = claimantPhone; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setClaimDate(long claimDate) { this.claimDate = claimDate; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setClaimLocation(String claimLocation) { this.claimLocation = claimLocation; }
    public void setProofImages(List<String> proofImages) { this.proofImages = proofImages; }
    public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }
    public void setItemLocation(String itemLocation) { this.itemLocation = itemLocation; }
    public void setItemDate(String itemDate) { this.itemDate = itemDate; }
    public void setItemImageUrl(String itemImageUrl) { this.itemImageUrl = itemImageUrl; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setApprovedAt(long approvedAt) { this.approvedAt = approvedAt; } // ✅ FIXED
}
