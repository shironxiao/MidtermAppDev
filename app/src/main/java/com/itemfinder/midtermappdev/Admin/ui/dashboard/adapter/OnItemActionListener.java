package com.itemfinder.midtermappdev.Admin.ui.dashboard.adapter;

import com.itemfinder.midtermappdev.Admin.data.model.Item_admin;

public interface OnItemActionListener {
    void onApproveItem(Item_admin itemAdmin);
    void onRejectItem(Item_admin itemAdmin);
}