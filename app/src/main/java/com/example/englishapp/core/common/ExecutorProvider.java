package com.example.englishapp.core.common;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Interface providing Executors for threading.
 * Using an interface makes it easy to swap executors during Unit Testing.
 *
 * Interface cung cấp các Executor (Luồng) cho Java threading.
 * Sử dụng interface này giúp ta dễ dàng tráo đổi luồng khi viết Unit Test.
 */
public interface ExecutorProvider {

    /**
     * Executor that runs tasks on the Android main (UI) thread.
     */
    @NonNull
    Executor getMainExecutor();

    /**
     * Executor for I/O-bound tasks (database, network, file operations).
     */
    @NonNull
    ExecutorService getIoExecutor();

    /**
     * Executor for CPU-intensive / default tasks.
     */
    @NonNull
    ExecutorService getDefaultExecutor();

    /**
     * Posts a Runnable to the main thread. Convenience method.
     */
    void postToMainThread(@NonNull Runnable runnable);
}
