package com.example.englishapp.feature.quiz.di;

import com.example.englishapp.feature.quiz.data.repository.QuizRepositoryImpl;
import com.example.englishapp.feature.quiz.domain.repository.repository.QuizRepository;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class QuizModule {

    @Binds
    public abstract QuizRepository bindQuizRepository(QuizRepositoryImpl impl);
}
