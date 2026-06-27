package com.example.englishapp.di;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.englishapp.core.common.DefaultExecutorProvider;
import com.example.englishapp.core.common.ExecutorProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IoExecutor {}

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultExecutor {}

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MainExecutor {}

    @Provides
    @Singleton
    public static ExecutorProvider provideExecutorProvider() {
        return new DefaultExecutorProvider();
    }

    @Provides
    @Singleton
    public static SharedPreferences provideSharedPreferences(@ApplicationContext Context context) {
        return context.getSharedPreferences(
                com.example.englishapp.core.datastore.UserPreferences.PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    @Provides
    @Singleton
    public static FirebaseFirestore provideFirestore() {
        return FirebaseFirestore.getInstance();
    }

}