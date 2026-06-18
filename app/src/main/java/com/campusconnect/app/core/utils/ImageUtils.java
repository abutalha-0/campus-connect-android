package com.campusconnect.app.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ImageUtils
 * ───────────
 * Small helper for compressing a picked gallery image before uploading it.
 * Profile photos don't need to be full resolution — keeping uploads under
 * ~1MB makes the experience noticeably faster on mobile data.
 *
 * Usage:
 *   File compressed = ImageUtils.compressImageFromUri(context, pickedUri);
 *   // then wrap `compressed` in a MultipartBody.Part for upload
 */
public class ImageUtils {

    private static final int MAX_DIMENSION = 1024;   // px, longest side
    private static final int JPEG_QUALITY  = 80;      // 0-100

    /**
     * Reads the image at the given content URI, downsizes it if needed,
     * compresses it to JPEG, and writes the result to a temp file in the
     * app's cache directory. Returns that file, ready to upload.
     */
    public static File compressImageFromUri(Context context, Uri imageUri) throws IOException {
        Bitmap original = loadBitmap(context, imageUri);
        Bitmap resized = resizeIfNeeded(original);

        File outputFile = new File(
                context.getCacheDir(),
                "profile_photo_" + System.currentTimeMillis() + ".jpg"
        );

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
            fos.write(baos.toByteArray());
        }

        if (resized != original) {
            original.recycle();
        }
        resized.recycle();

        return outputFile;
    }

    private static Bitmap loadBitmap(Context context, Uri uri) throws IOException {
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(input);
        }
    }

    private static Bitmap resizeIfNeeded(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= MAX_DIMENSION && height <= MAX_DIMENSION) {
            return original; // already small enough
        }

        float scale = Math.min(
                (float) MAX_DIMENSION / width,
                (float) MAX_DIMENSION / height
        );

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
}