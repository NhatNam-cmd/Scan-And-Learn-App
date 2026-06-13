package com.project.englishapp.di
import com.example.englishapp.feature.dictionary.data.repository.VocabularyRepositoryImpl
import com.project.englishapp.feature.dictionary.domain.repository.VocabularyRepository
import dagger.Binds
import javax.inject.Singleton
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVocabularyRepository(
        impl: VocabularyRepositoryImpl
    ): VocabularyRepository
}