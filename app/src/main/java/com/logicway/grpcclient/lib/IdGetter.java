package com.logicway.grpcclient.lib;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class IdGetter {

    private final static String ID_KEY = "android_id";

    private final static String URI_STRING = "content://com.google.android.gsf.gservices";

    private final static String HASHING_ALGORITHM = "MD5";

    private final static String CHARSET_NAME = "UTF-8";

    public String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public String getGsfId(Context context) {
        Uri URI = Uri.parse(URI_STRING);
        String params[] = {ID_KEY};
        Cursor c = context.getContentResolver().query(URI, null, null, params, null);
        if (!c.moveToFirst() || c.getColumnCount() < 2)
            return null;
        try {
            return Long.toHexString(Long.parseLong(c.getString(1)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getFingerprint() {
        return Build.FINGERPRINT;
    }

    public String getBase64FromIdsHashes(Context context) {
        String[] stringsToHash = {getAndroidId(context), getGsfId(context), getFingerprint()};
        byte[] digest = {};
        try {
            MessageDigest md5 = MessageDigest.getInstance(HASHING_ALGORITHM);
            for (String toHash : stringsToHash) {
                try {
                    md5.update(toHash.getBytes(CHARSET_NAME));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            digest = md5.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(digest);
    }
}
