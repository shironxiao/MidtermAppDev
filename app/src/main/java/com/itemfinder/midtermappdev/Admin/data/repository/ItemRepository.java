package com.itemfinder.midtermappdev.Admin.data.repository;

import com.itemFinder.realfinalappdev.data.model.Item_admin;
import com.itemFinder.realfinalappdev.firebase.FirebaseHelper;

import java.util.List;

public class ItemRepository {
    private final FirebaseHelper firebaseHelper = new FirebaseHelper();

    public interface RepositoryListener {
        void onDataLoaded(List<Item_admin> itemAdmins);
        void onError(String error);
    }

    public void getAllItems(RepositoryListener listener) {
        firebaseHelper.fetchAllItems(new FirebaseHelper.ItemFetchListener() {
            @Override
            public void onItemsFetched(List<Item_admin> itemAdmins) {
                listener.onDataLoaded(itemAdmins);
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }
}
