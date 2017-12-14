package com.android.camera.google;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.C0049R;

public enum SpecialType {
    UNKNOWN,
    NONE,
    PORTRAIT_TYPE(1, C0049R.string.photos_portrait_type_name, C0049R.string.photos_portrait_type_description, C0049R.drawable.ic_photos_portrait_badge, C0049R.drawable.ic_photos_portrait_dialog, null, null, null, ConfigurationImpl.BADGE);
    
    @Nullable
    private final ConfigurationImpl configuration;
    final int descriptionResourceId;
    @Nullable
    private final Class<? extends Activity> editActivityClass;
    final int iconBadgeResourceId;
    final int iconDialogResourceId;
    @Nullable
    private final Class<? extends Activity> interactActivityClass;
    @Nullable
    private Class<? extends Activity> launchActivityClass;
    final int nameResourceId;
    final int typeId;

    private SpecialType(int i, int i2, int i3, int i4, int i5, Class<? extends Activity> cls, Class<? extends Activity> cls2, @Nullable Class<? extends Activity> cls3, @Nullable ConfigurationImpl configurationImpl) {
        this.typeId = i;
        this.nameResourceId = i2;
        this.descriptionResourceId = i3;
        this.iconBadgeResourceId = i4;
        this.iconDialogResourceId = i5;
        this.editActivityClass = cls;
        this.interactActivityClass = cls2;
        this.launchActivityClass = cls3;
        this.configuration = configurationImpl;
        if (configurationImpl != null) {
            configurationImpl.validate(this);
        }
    }

    static SpecialType fromTypeId(int i) {
        switch (i) {
            case 1:
                return PORTRAIT_TYPE;
            default:
                return UNKNOWN;
        }
    }

    ConfigurationImpl getConfiguration() {
        if (this.configuration != null) {
            return this.configuration;
        }
        throw new UnsupportedOperationException();
    }
}
