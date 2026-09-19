package com.da_grupo9.ronda.util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.da_grupo9.ronda.data.model.UploadImageResponse;
import com.da_grupo9.ronda.data.remote.ImageUploadApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Prepara y sube las imágenes seleccionadas con una única política de formato y tamaño. */
@Singleton
public class ImageUploadManager {
    public static final long MAX_IMAGE_BYTES = 20L * 1024L * 1024L;

    public interface Result<T> {
        void onSuccess(T value);
        void onError(String message);
    }

    private final Context context;
    private final ImageUploadApi api;
    private final ExecutorService fileExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public ImageUploadManager(@ApplicationContext Context context, ImageUploadApi api) {
        this.context = context;
        this.api = api;
    }

    /** Copia una selección a almacenamiento persistente para que pueda formar parte del borrador. */
    public void copyToDraft(Uri uri, File directory, String prefix, Result<File> result) {
        fileExecutor.execute(() -> {
            try {
                String mime = requireSupportedMime(context.getContentResolver().getType(uri));
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IOException("No se pudo crear el directorio del borrador");
                }
                File destination = new File(directory, prefix + System.nanoTime() + extensionFor(mime));
                copyUri(uri, destination);
                deliverSuccess(result, destination);
            } catch (ImageValidationException error) {
                deliverError(result, error.getMessage());
            } catch (Exception error) {
                deliverError(result, "No se pudo guardar la imagen");
            }
        });
    }

    /** Copia una selección a un temporal, la sube y siempre elimina el temporal. */
    public void upload(Uri uri, String prefix, Result<String> result) {
        fileExecutor.execute(() -> {
            File temporary = null;
            try {
                String mime = requireSupportedMime(context.getContentResolver().getType(uri));
                File directory = new File(context.getCacheDir(), "image_uploads");
                if (!directory.exists() && !directory.mkdirs()) throw new IOException();
                temporary = new File(directory, prefix + System.nanoTime() + extensionFor(mime));
                copyUri(uri, temporary);
                uploadFile(temporary, mime, true, result);
            } catch (ImageValidationException error) {
                delete(temporary);
                deliverError(result, error.getMessage());
            } catch (Exception error) {
                delete(temporary);
                deliverError(result, "No se pudo leer la imagen seleccionada");
            }
        });
    }

    public void uploadFiles(List<String> paths, Result<List<String>> result) {
        fileExecutor.execute(() -> {
            List<File> files = new ArrayList<>();
            List<String> mimes = new ArrayList<>();
            try {
                for (String path : paths) {
                    File file = new File(path);
                    if (!file.isFile() || file.length() == 0) throw new IOException();
                    if (file.length() > MAX_IMAGE_BYTES) throw tooLarge();
                    files.add(file);
                    mimes.add(requireSupportedMime(mimeForFile(file)));
                }
                uploadNext(files, mimes, 0, new ArrayList<>(), result);
            } catch (ImageValidationException error) {
                deliverError(result, error.getMessage());
            } catch (Exception error) {
                deliverError(result, "No se pudo leer una imagen seleccionada");
            }
        });
    }

    private void uploadNext(List<File> files, List<String> mimes, int index,
                            List<String> urls, Result<List<String>> result) {
        if (index >= files.size()) {
            deliverSuccess(result, urls);
            return;
        }
        uploadFile(files.get(index), mimes.get(index), false, new Result<String>() {
            @Override public void onSuccess(String url) {
                urls.add(url);
                uploadNext(files, mimes, index + 1, urls, result);
            }

            @Override public void onError(String message) {
                deliverError(result, message);
            }
        });
    }

    private void uploadFile(File file, String mime, boolean deleteAfter, Result<String> result) {
        RequestBody body = RequestBody.create(file, MediaType.parse(mime));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), body);
        api.uploadImage(part).enqueue(new Callback<UploadImageResponse>() {
            @Override public void onResponse(Call<UploadImageResponse> call, Response<UploadImageResponse> response) {
                if (deleteAfter) delete(file);
                UploadImageResponse upload = response.body();
                if (!response.isSuccessful() || upload == null || upload.getUrl() == null) {
                    deliverError(result, ApiError.from(response,
                            "No se pudo subir una imagen (código " + response.code() + ")").getMessage());
                    return;
                }
                deliverSuccess(result, upload.getUrl());
            }

            @Override public void onFailure(Call<UploadImageResponse> call, Throwable error) {
                if (deleteAfter) delete(file);
                deliverError(result, "No se pudo subir una imagen");
            }
        });
    }

    private void copyUri(Uri uri, File destination) throws IOException, ImageValidationException {
        try (InputStream input = context.getContentResolver().openInputStream(uri);
             FileOutputStream output = new FileOutputStream(destination)) {
            if (input == null) throw new IOException();
            byte[] buffer = new byte[8192];
            long total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_IMAGE_BYTES) throw tooLarge();
                output.write(buffer, 0, read);
            }
            if (total == 0) throw new IOException();
        } catch (IOException | ImageValidationException error) {
            delete(destination);
            throw error;
        }
    }

    private String requireSupportedMime(String mime) throws ImageValidationException {
        if ("image/jpeg".equals(mime) || "image/png".equals(mime) || "image/webp".equals(mime)) {
            return mime;
        }
        throw new ImageValidationException("Elegí una imagen JPG, PNG o WebP");
    }

    private String mimeForFile(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        return null;
    }

    private String extensionFor(String mime) {
        if ("image/png".equals(mime)) return ".png";
        if ("image/webp".equals(mime)) return ".webp";
        return ".jpg";
    }

    private ImageValidationException tooLarge() {
        return new ImageValidationException("La imagen no puede superar los 20 MB");
    }

    private <T> void deliverSuccess(Result<T> result, T value) {
        mainHandler.post(() -> result.onSuccess(value));
    }

    private void deliverError(Result<?> result, String message) {
        mainHandler.post(() -> result.onError(message));
    }

    private void delete(File file) {
        if (file != null && file.exists()) file.delete();
    }

    private static final class ImageValidationException extends Exception {
        ImageValidationException(String message) { super(message); }
    }
}
