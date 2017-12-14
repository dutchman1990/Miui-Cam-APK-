package com.google.android.apps.photos.api.signature;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public final class TrustedPartners {
    private final PackageManager packageManager;
    private final Set<String> trustedPartnerCertificateHashes;

    public TrustedPartners(Context context, Set<String> set) {
        this.packageManager = context.getPackageManager();
        this.trustedPartnerCertificateHashes = set;
    }

    public boolean isTrustedApplication(String str) {
        if (TextUtils.isEmpty(str)) {
            if (Log.isLoggable("TrustedPartners", 5)) {
                Log.w("TrustedPartners", "null or empty package name; do not trust");
            }
            return false;
        }
        try {
            PackageInfo packageInfo = this.packageManager.getPackageInfo(str, 64);
            if (packageInfo.signatures == null || packageInfo.signatures.length != 1) {
                if (Log.isLoggable("TrustedPartners", 5)) {
                    Log.w("TrustedPartners", packageInfo.signatures.length + " signatures found for package (" + str + "); do not trust");
                }
                return false;
            }
            try {
                MessageDigest instance = MessageDigest.getInstance("SHA1");
                instance.update(packageInfo.signatures[0].toByteArray());
                return this.trustedPartnerCertificateHashes.contains(HexConvert.bytesToHex(instance.digest()));
            } catch (NoSuchAlgorithmException e) {
                if (Log.isLoggable("TrustedPartners", 6)) {
                    Log.e("TrustedPartners", "unable to compute hash using SHA1; do not trust");
                }
                return false;
            }
        } catch (NameNotFoundException e2) {
            if (Log.isLoggable("TrustedPartners", 5)) {
                Log.w("TrustedPartners", "package not found (" + str + "); do not trust");
            }
            return false;
        }
    }
}
