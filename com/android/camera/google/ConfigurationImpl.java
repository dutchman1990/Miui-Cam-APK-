package com.android.camera.google;

enum ConfigurationImpl {
    BADGE("badge") {
        void validate(SpecialType specialType) {
            super.validate(specialType);
        }
    };
    
    private final String key;

    private ConfigurationImpl(String str) {
        this.key = str;
    }

    private static void checkArgument(boolean z, String str) {
        if (!z) {
            throw new IllegalArgumentException(str);
        }
    }

    private static void checkResourceId(int i, String str) {
        boolean z = false;
        if (i != 0) {
            z = true;
        }
        checkArgument(z, str + " must be a valid resource id");
    }

    String getKey() {
        return this.key;
    }

    void validate(SpecialType specialType) {
        checkResourceId(specialType.descriptionResourceId, "description");
        checkResourceId(specialType.iconBadgeResourceId, "icon");
        checkResourceId(specialType.iconDialogResourceId, "icon");
        checkResourceId(specialType.nameResourceId, "name");
    }
}
