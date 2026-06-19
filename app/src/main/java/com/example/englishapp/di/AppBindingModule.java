package com.example.englishapp.di;

import com.example.englishapp.feature.scan.data.repository.ScanRepositoryImpl;
import com.example.englishapp.feature.scan.domain.repository.ScanRepository;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public abstract class AppBindingModule {

    @Binds
    @Singleton
    public abstract ScanRepository bindScanRepository(ScanRepositoryImpl impl);
}