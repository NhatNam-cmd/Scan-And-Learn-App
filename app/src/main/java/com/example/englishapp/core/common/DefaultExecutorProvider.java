package com.example.englishapp.core.common;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default implementation of {@link ExecutorProvider}.
 * Uses fixed thread pools for IO and default work, and Android's main Looper for the main thread.
 */
public class DefaultExecutorProvider implements ExecutorProvider {

    private final Executor mainExecutor;
    private final ExecutorService ioExecutor;
    private final ExecutorService defaultExecutor;
    private final Handler mainHandler;

    public DefaultExecutorProvider() {
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.mainExecutor = mainHandler::post;
        // IO executor – suitable for database / network / file I/O
        this.ioExecutor = Executors.newFixedThreadPool(4);
        // Default executor – suitable for CPU-intensive work
        this.defaultExecutor = Executors.newFixedThreadPool(2);
    }

    @NonNull
    @Override
    public Executor getMainExecutor() {
        return mainExecutor;
    }

    @NonNull
    @Override
    public ExecutorService getIoExecutor() {
        return ioExecutor;
    }

    @NonNull
    @Override
    public ExecutorService getDefaultExecutor() {
        return defaultExecutor;
    }

    @Override
    public void postToMainThread(@NonNull Runnable runnable) {
        mainHandler.post(runnable);
    }
}
