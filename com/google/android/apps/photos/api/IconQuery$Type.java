package com.google.android.apps.photos.api;

import android.support.v7.recyclerview.C0049R;

public enum IconQuery$Type {
    BADGE("badge", C0049R.dimen.badge_icon_size),
    INTERACT("interact", C0049R.dimen.interact_icon_size),
    DIALOG("dialog", C0049R.dimen.interact_icon_size);
    
    private final int dimensionResourceId;
    private final String path;

    private IconQuery$Type(String str, int i) {
        this.path = str;
        this.dimensionResourceId = i;
    }

    public int getDimensionResourceId() {
        return this.dimensionResourceId;
    }
}
