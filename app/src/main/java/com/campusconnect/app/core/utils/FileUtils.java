package com.campusconnect.app.core.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/** Helpers for turning a picked content Uri into an uploadable cache file. */
public final class FileUtils {

    private FileUtils() {}

    public static File copyToCache(Context context, Uri uri) throws Exception {
        String name = displayName(context, uri);
        File out = new File(context.getCacheDir(), name);
        try (InputStream in = context.getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(out)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) > 0) os.write(buffer, 0, read);
        }
        return out;
    }

    public static String displayName(Context context, Uri uri) {
        String name = null;
        try (Cursor c = context.getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = c.getString(idx);
            }
        } catch (Exception ignored) {}
        return name != null ? name : ("upload_" + System.currentTimeMillis());
    }
}
