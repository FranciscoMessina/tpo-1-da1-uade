package com.da_grupo9.ronda.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class ImageStorageManager {

    private static final String TAG = "ImageStorageManager";
    private static final String IMAGES_DIR_NAME = "cached_images";

    private final Context context;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public interface ImageSavedCallback {
        void onSaved(String localPath);
        void onError(Exception e);
    }

    @Inject
    public ImageStorageManager(@ApplicationContext Context context) {
        this.context = context;
        getImagesDir();
    }

    private File getImagesDir() {
        File dir = new File(context.getFilesDir(), IMAGES_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public String generateFileName(String url) {
        if (TextUtils.isEmpty(url)) return "unknown.jpg";
        return "img_" + Math.abs(url.hashCode()) + ".jpg";
    }

    public String getLocalImagePathIfExists(String url) {
        if (TextUtils.isEmpty(url)) return null;
        File file = new File(getImagesDir(), generateFileName(url));
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }
        return null;
    }

    public void downloadAndSaveImageAsync(String imageUrl, ImageSavedCallback callback) {
        if (TextUtils.isEmpty(imageUrl) || !imageUrl.startsWith("http")) {
            if (callback != null) callback.onError(new IllegalArgumentException("URL inválida"));
            return;
        }

        String existing = getLocalImagePathIfExists(imageUrl);
        if (existing != null) {
            if (callback != null) callback.onSaved(existing);
            return;
        }

        executorService.execute(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(12000);
                connection.setInstanceFollowRedirects(true);
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    if (callback != null) callback.onError(new Exception("HTTP " + connection.getResponseCode()));
                    return;
                }

                File targetFile = new File(getImagesDir(), generateFileName(imageUrl));
                File tempFile = new File(getImagesDir(), generateFileName(imageUrl) + ".tmp");

                try (InputStream inputStream = connection.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }

                if (tempFile.renameTo(targetFile)) {
                    if (callback != null) callback.onSaved(targetFile.getAbsolutePath());
                } else {
                    if (callback != null) callback.onError(new Exception("No se pudo renombrar archivo temporal"));
                }
            } catch (Exception e) {
                Log.w(TAG, "Error descargando imagen: " + imageUrl, e);
                if (callback != null) callback.onError(e);
            }
        });
    }
}
