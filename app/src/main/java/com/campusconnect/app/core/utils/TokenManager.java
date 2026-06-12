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
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            prefs = EncryptedSharedPreferences.create(
                    context,
                    Constants.PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTokens(String accessToken, String refreshToken) {
        prefs.edit()
                .putString(Constants.KEY_ACCESS, accessToken)
                .putString(Constants.KEY_REFRESH, refreshToken)
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(Constants.KEY_ACCESS, null);
    }

    public String getRefreshToken() {
        return prefs.getString(Constants.KEY_REFRESH, null);
    }

    public boolean hasToken() {
        return getAccessToken() != null;
    }

    public void clearTokens() {
        prefs.edit().clear().apply();
    }
}