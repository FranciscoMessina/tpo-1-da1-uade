package com.da_grupo9.ronda.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class NetworkMonitor {

    public interface NetworkStatusListener {
        void onNetworkChanged(boolean isOnline);
    }

    private final ConnectivityManager connectivityManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final CopyOnWriteArrayList<NetworkStatusListener> listeners = new CopyOnWriteArrayList<>();
    private boolean isRegistered = false;
    private boolean lastKnownStatus = false;

    @Inject
    public NetworkMonitor(@ApplicationContext Context context) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.lastKnownStatus = isOnline();
        registerCallback();
    }

    public boolean isOnline() {
        if (connectivityManager == null) return false;
        try {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) return false;
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (Exception e) {
            return false;
        }
    }

    public void addListener(NetworkStatusListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            listener.onNetworkChanged(isOnline());
        }
    }

    public void removeListener(NetworkStatusListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    private void registerCallback() {
        if (connectivityManager == null || isRegistered) return;
        try {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    notifyStatus(true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    notifyStatus(false);
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities capabilities) {
                    boolean online = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                    notifyStatus(online);
                }
            });
            isRegistered = true;
        } catch (Exception ignored) {
        }
    }

    private void notifyStatus(boolean isOnline) {
        lastKnownStatus = isOnline;
        mainHandler.post(() -> {
            for (NetworkStatusListener listener : listeners) {
                listener.onNetworkChanged(isOnline);
            }
        });
    }
}
