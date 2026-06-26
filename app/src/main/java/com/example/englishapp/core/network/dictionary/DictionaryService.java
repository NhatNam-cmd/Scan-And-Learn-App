package com.example.englishapp.core.network.dictionary;

import com.example.englishapp.core.network.dictionary.dto.DictionaryWordDto;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DictionaryService {
    @GET("api/v2/entries/en/{word}")
    Call<List<DictionaryWordDto>> getWordData(@Path("word") String word);
}