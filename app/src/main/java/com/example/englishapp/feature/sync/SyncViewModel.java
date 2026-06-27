package com.example.englishapp.feature.sync;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.core.common.ExecutorProvider;
import com.example.englishapp.core.firebase.service.FirestoreService;
import com.example.englishapp.core.sync.SyncManager;
import com.google.android.gms.tasks.Tasks;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SyncViewModel extends ViewModel {

    private final FirestoreService firestoreService;
    private final ExecutorProvider executorProvider;

    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private final MutableLiveData<String> syncStatus = new MutableLiveData<>("Sẵn sàng đồng bộ");
    private final MutableLiveData<Boolean> hasRemoteData = new MutableLiveData<>(false);

    @Inject
    public SyncViewModel(FirestoreService firestoreService, ExecutorProvider executorProvider) {
        this.firestoreService = firestoreService;
        this.executorProvider = executorProvider;
        checkRemoteData();
    }

    public LiveData<Boolean> isSyncing() { return isSyncing; }
    public LiveData<String> getSyncStatus() { return syncStatus; }
    public LiveData<Boolean> hasRemoteData() { return hasRemoteData; }

    public void syncData(Context context) {
        isSyncing.setValue(true);
        syncStatus.setValue("Đang đồng bộ...");

        executorProvider.getIoExecutor().execute(() -> {
            try {
                SyncManager.syncNow(context);
                syncStatus.postValue("Đồng bộ thành công!");
                hasRemoteData.postValue(true);
            } catch (Exception e) {
                syncStatus.postValue("Lỗi đồng bộ: " + e.getMessage());
            } finally {
                isSyncing.postValue(false);
            }
        });
    }

    public void checkRemoteData() {
        executorProvider.getIoExecutor().execute(() -> {
            try {
                boolean hasData = Tasks.await(firestoreService.hasUserData());
                hasRemoteData.postValue(hasData);
            } catch (Exception e) {
                hasRemoteData.postValue(false);
            }
        });
    }
}