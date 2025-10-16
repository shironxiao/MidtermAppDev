package com.itemfinder.midtermappdev.Admin.data.repository;

import android.util.Log;
import com.itemfinder.midtermappdev.Admin.data.model.Item_admin;
import com.itemfinder.midtermappdev.Admin.firebase.AdminFirebaseHelper;

import java.util.List;

public class ItemRepository {
    private static final String TAG = "ItemRepository";
    private final AdminFirebaseHelper firebaseHelper = new AdminFirebaseHelper();

    public interface RepositoryListener {
        void onDataLoaded(List<Item_admin> itemAdmins);
        void onError(String error);
    }

    public void getAllItems(RepositoryListener listener) {
        Log.d(TAG, "getAllItems called");
        firebaseHelper.fetchAllItems(new AdminFirebaseHelper.ItemFetchListener() {
            @Override
            public void onItemsFetched(List<Item_admin> itemAdmins) {
                Log.d(TAG, "Fetched " + itemAdmins.size() + " items from Firebase");
                listener.onDataLoaded(itemAdmins);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching items: " + error);
                listener.onError(error);
            }
        });
    }

    public void getPendingItems(RepositoryListener listener) {
        Log.d(TAG, "getPendingItems called");
        firebaseHelper.fetchPendingItems(new AdminFirebaseHelper.ItemFetchListener() {
            @Override
            public void onItemsFetched(List<Item_admin> itemAdmins) {
                Log.d(TAG, "Fetched " + itemAdmins.size() + " pending items");
                listener.onDataLoaded(itemAdmins);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching pending items: " + error);
                listener.onError(error);
            }
        });
    }

    public void getItemsByCategory(String category, RepositoryListener listener) {
        Log.d(TAG, "getItemsByCategory called for: " + category);
        firebaseHelper.fetchItemsByCategory(category, new AdminFirebaseHelper.ItemFetchListener() {
            @Override
            public void onItemsFetched(List<Item_admin> itemAdmins) {
                Log.d(TAG, "Fetched " + itemAdmins.size() + " items for category: " + category);
                listener.onDataLoaded(itemAdmins);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching items by category: " + error);
                listener.onError(error);
            }
        });
    }
}