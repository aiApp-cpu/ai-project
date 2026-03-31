package com.example.aiassistant.app.service;

import com.example.aiassistant.app.model.NotesRequest;
import com.example.aiassistant.app.model.NotesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.aiassistant.app.model.HfChatResponse;

@Service
public class AiService {

    private final RestTemplate restTemplate;

    public AiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${hf.api.key}")
    private String apiKey;

    @Value("${hf.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotesResponse generateNotes(NotesRequest request) {

        if (request == null || request.getInput() == null || request.getInput().trim().isEmpty()) {
            return new NotesResponse("Please provide some input.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String model = "Qwen/Qwen2.5-7B-Instruct";

            String jsonBody = """
            {
              "model": "%s",
              "messages": [
                {
                  "role": "system",
                  "content": "You are a helpful AI assistant. Always answer in clear and natural English language only. Do not use Chinese, Hindi, or any other language."
                },
                {
                  "role": "user",
                  "content": "%s"
                }
              ],
              "temperature": 0.7,
              "max_tokens": 800,
              "stream": false
            }
            """.formatted(model, request.getInput().replace("\"", "\\\""));

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String rawJson = responseEntity.getBody();

            // Parse with HfChatResponse (the class you already created)
            HfChatResponse hfResponse = objectMapper.readValue(rawJson, HfChatResponse.class);

            String content = "No content received.";
            if (hfResponse.getChoices() != null && !hfResponse.getChoices().isEmpty()) {
                var msg = hfResponse.getChoices().get(0).getMessage();
                if (msg != null && msg.getContent() != null) {
                    content = msg.getContent().trim();
                }
            }

            return new NotesResponse(content);

        } catch (Exception e) {
            return new NotesResponse("Error: " + e.getMessage());
        }
    }
}