package com.example.englishapp.di;

import com.example.englishapp.core.network.dictionary.DictionaryApiClient;
import com.example.englishapp.core.network.dictionary.DictionaryService;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class FirebaseModule {

    @Provides
    @Singleton
    public DictionaryService provideDictionaryService() {
        return DictionaryApiClient.getDictionaryService();
    }
}