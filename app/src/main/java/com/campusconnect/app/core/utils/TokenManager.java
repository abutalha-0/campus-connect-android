package com.campusconnect.app.core.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {

    private SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = createEncryptedPrefs(context);

        // if prefs is still null after first attempt
        // the file is corrupted — delete it and try again
        if (prefs == null) {
            context.deleteSharedPreferences(Constants.PREF_NAME);
            prefs = createEncryptedPrefs(context);
        }
    }

    private SharedPreferences createEncryptedPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    Constants.PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            return null;
        }
    }

    public void saveTokens(String accessToken, String refreshToken) {
        if (prefs == null) return;
        prefs.edit()
                .putString(Constants.KEY_ACCESS, accessToken)
                .putString(Constants.KEY_REFRESH, refreshToken)
                .apply();
    }

    public String getAccessToken() {
        if (prefs == null) return null;
        return prefs.getString(Constants.KEY_ACCESS, null);
    }

    public String getRefreshToken() {
        if (prefs == null) return null;
        return prefs.getString(Constants.KEY_REFRESH, null);
    }

    public boolean hasToken() {
        return getAccessToken() != null;
    }

    public void clearTokens() {
        if (prefs == null) return;
        prefs.edit().clear().apply();
    }
}