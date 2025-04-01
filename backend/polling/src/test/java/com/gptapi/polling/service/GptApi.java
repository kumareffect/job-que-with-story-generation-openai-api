package com.gptapi.polling.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

@Service
public class GptApi {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";
    private static final int MAX_RETRIES = 3;
    private static final int BASE_RETRY_DELAY_MS = 5000;

    public String generateStory(String prompt, String apiKey) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return callOpenAI(apiKey, prompt);
            } catch (Exception e) {
                if (attempt < MAX_RETRIES) {
                    System.out.println("Retrying... Attempt " + attempt + "/" + MAX_RETRIES);
                    sleep(attempt * BASE_RETRY_DELAY_MS);
                } else {
                    System.err.println("Failed after " + MAX_RETRIES + " attempts:");
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    private String callOpenAI(String apiKey, String prompt) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL);

            JsonArray messages = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);
            messages.add(message);
            requestBody.add("messages", messages);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.toString().getBytes("utf-8"));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorMsg = readErrorStream(connection);
                throw new Exception("API call failed with status code: " + responseCode + " - " + errorMsg);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseJson = br.lines().collect(Collectors.joining());
                JsonObject jsonResponse = JsonParser.parseString(responseJson).getAsJsonObject();
                return jsonResponse.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readErrorStream(HttpURLConnection connection) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
            return br.lines().collect(Collectors.joining());
        } catch (Exception e) {
            return "Unknown error";
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}