package com.google.android.apps.photos.api;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import android.support.v7.recyclerview.C0049R;

public final class PhotosOemApi {
    public static String getAuthority(Context context) {
        return context.getString(C0049R.string.photos_special_types_authority);
    }

    private static Builder getBaseBuilder(Context context) {
        return new Builder().scheme("content").authority(getAuthority(context));
    }

    public static long getMediaStoreIdFromQueryTypeUri(Uri uri) {
        return Long.parseLong(Uri.decode(uri.getLastPathSegment()));
    }

    public static Uri getQueryProcessingUri(Context context, long j) {
        return getBaseBuilder(context).appendPath("processing").appendPath(String.valueOf(j)).build();
    }

    public static String getSpecialTypeIdFromQueryDataUri(Uri uri) {
        return Uri.decode(uri.getLastPathSegment());
    }
}
