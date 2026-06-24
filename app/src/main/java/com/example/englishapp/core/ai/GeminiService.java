package com.example.englishapp.core.ai;

import com.example.englishapp.BuildConfig;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.feature.story.domain.StoryGameData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.POST;

public class GeminiService {
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static final String[] STORY_MODELS = {
            "gemini-2.5-flash",
            "gemini-flash-latest",
            "gemini-2.5-flash-lite"
    };

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
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("responseMimeType", "application/json");
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("maxOutputTokens", 1200);
        body.add("generationConfig", generationConfig);

        IOException lastError = null;
        for (String model : STORY_MODELS) {
            for (int attempt = 0; attempt < 2; attempt++) {
                try {
                    StoryGameData data = requestStory(model, body, words);
                    data.setOffline(false);
                    return data;
                } catch (IOException exception) {
                    lastError = exception;
                    if (!isTemporaryGeminiError(exception)) {
                        break;
                    }
                    sleepBeforeRetry(attempt);
                }
            }
        }
        throw lastError == null ? new IOException("Gemini generation failed") : lastError;
    }

    private StoryGameData requestStory(String model, JsonObject body, List<VocabularyEntity> words) throws IOException {
        Response<JsonObject> response = apiService.generateContent(model, BuildConfig.GEMINI_API_KEY, body).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Gemini returned HTTP " + response.code());
        }
        String jsonText = extractText(response.body());
        StoryGameData data = validator.parseAndValidate(stripCodeFence(jsonText));
        if (data == null) {
            throw new IOException("Invalid story JSON");
        }
        if (!usesTargetWords(data, words)) {
            throw new IOException("Story blanks do not match selected vocabulary");
        }
        attachVocabularyIds(data, words);
        return data;
    }

    private boolean isTemporaryGeminiError(IOException exception) {
        String message = exception.getMessage();
        return message != null && (message.contains("HTTP 429") || message.contains("HTTP 500")
                || message.contains("HTTP 502") || message.contains("HTTP 503")
                || message.contains("HTTP 504"));
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(attempt == 0 ? 800L : 1500L);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private void attachVocabularyIds(StoryGameData data, List<VocabularyEntity> words) {
        Map<String, Long> idsByWord = new HashMap<>();
        for (VocabularyEntity word : words) {
            idsByWord.put(word.getWord().toLowerCase(), word.getVocabularyId());
        }
        for (int i = 0; i < data.getBlanks().size(); i++) {
            String blankWord = data.getBlanks().get(i).getWord();
            if (blankWord != null && idsByWord.containsKey(blankWord.toLowerCase())) {
                data.getBlanks().get(i).setVocabularyId(idsByWord.get(blankWord.toLowerCase()));
            } else if (i < words.size()) {
                data.getBlanks().get(i).setVocabularyId(words.get(i).getVocabularyId());
            }
        }
    }

    private boolean usesTargetWords(StoryGameData data, List<VocabularyEntity> words) {
        if (data.getBlanks().size() != words.size()) {
            return false;
        }
        for (int i = 0; i < words.size(); i++) {
            String expected = words.get(i).getWord();
            String actual = data.getBlanks().get(i).getWord();
            if (expected == null || actual == null
                    || !expected.trim().equalsIgnoreCase(actual.trim())) {
                return false;
            }
        }
        return true;
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
        @POST("v1beta/models/{model}:generateContent")
        Call<JsonObject> generateContent(@Path("model") String model,
                @Header("x-goog-api-key") String apiKey,
                @Body JsonObject body);
    }
}

