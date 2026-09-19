package com.da_grupo9.ronda.data.local;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionExpirationNotifier {
    public interface Listener {
        void onSessionExpired();
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<>();
    private final AtomicBoolean pendingExpiration = new AtomicBoolean(false);

    @Inject
    public SessionExpirationNotifier() {
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void notifySessionExpired() {
        if (!pendingExpiration.compareAndSet(false, true)) return;

        mainHandler.post(() -> {
            for (Listener listener : listeners) {
                listener.onSessionExpired();
            }
        });
    }

    public boolean consumeExpiration() {
        return pendingExpiration.compareAndSet(true, false);
    }
}
