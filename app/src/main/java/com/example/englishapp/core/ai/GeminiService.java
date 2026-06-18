package com.example.englishapp.core.ai;

import com.example.englishapp.BuildConfig;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.feature.story.domain.StoryGameData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class GeminiService {
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";

    private final GeminiApiService apiService;
    private final StoryPromptBuilder promptBuilder;
    private final StoryValidator validator;

    public GeminiService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.apiService = retrofit.create(GeminiApiService.class);
        this.promptBuilder = new StoryPromptBuilder();
        this.validator = new StoryValidator();
    }

    public StoryGameData generateStory(List<VocabularyEntity> words) throws IOException {
        if (BuildConfig.GEMINI_API_KEY == null || BuildConfig.GEMINI_API_KEY.trim().isEmpty()) {
            throw new IOException("Missing Gemini API key");
        }
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", promptBuilder.buildPrompt(words));

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject body = new JsonObject();
        body.add("contents", contents);

        Response<JsonObject> response = apiService.generateContent(BuildConfig.GEMINI_API_KEY, body).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Gemini returned HTTP " + response.code());
        }
        String jsonText = extractText(response.body());
        StoryGameData data = validator.parseAndValidate(stripCodeFence(jsonText));
        if (data == null) {
            throw new IOException("Invalid story JSON");
        }
        data.setOffline(false);
        attachVocabularyIds(data, words);
        return data;
    }

    private void attachVocabularyIds(StoryGameData data, List<VocabularyEntity> words) {
        for (int i = 0; i < data.getBlanks().size() && i < words.size(); i++) {
            data.getBlanks().get(i).setVocabularyId(words.get(i).getVocabularyId());
        }
    }

    private String extractText(JsonObject response) throws IOException {
        try {
            return response.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (RuntimeException exception) {
            throw new IOException("Gemini response missing text", exception);
        }
    }

    private String stripCodeFence(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```json\\s*", "").replaceFirst("^```\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed;
    }

    public interface GeminiApiService {
        @POST("v1beta/models/gemini-1.5-flash:generateContent")
        Call<JsonObject> generateContent(@Query("key") String apiKey, @Body JsonObject body);
    }
}

