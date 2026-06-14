package com.example.englishapp.core.network.dictionary;

import com.example.englishapp.core.network.dictionary.dto.DictionaryResponseDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DictionaryService {
    @GET("{word}")
    Call<List<DictionaryResponseDto>> lookupWord(@Path("word") String word);
}
