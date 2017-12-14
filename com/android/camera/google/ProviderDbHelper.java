package com.android.camera.google;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

final class ProviderDbHelper extends SQLiteOpenHelper {
    private static volatile ProviderDbHelper helper;

    private ProviderDbHelper(Context context) {
        super(context, "provider_db_helper", null, 2);
    }

    static ProviderDbHelper get(Context context) {
        if (helper == null) {
            synchronized (ProviderDbHelper.class) {
                if (helper == null) {
                    helper = new ProviderDbHelper(context);
                }
            }
        }
        return helper;
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL(TypeIdTable.getCreateSql());
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        int i3 = i;
        while (i3 < i2) {
            switch (i3) {
                case 1:
                    sQLiteDatabase.delete("type_uri", null, null);
                    i3++;
                    break;
                default:
                    break;
            }
        }
    }
}
