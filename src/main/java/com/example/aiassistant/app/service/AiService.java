package com.example.aiassistant.app.service;

import com.example.aiassistant.app.model.NotesRequest;
import com.example.aiassistant.app.model.NotesResponse;
import com.example.aiassistant.app.model.HfChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

@Value("${hf.api.key}")
private String hfApiKey;

    @Value("${hf.api.url}")
    private String apiUrl;

    public NotesResponse generateNotes(NotesRequest request) {

        System.out.println("API KEY: " + hfApiKey);

        if (request == null || request.getInput() == null || request.getInput().trim().isEmpty()) {
            return new NotesResponse("Please provide some input.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(hfApiKey); 
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 1. Build Payload using Map (Very Safe)
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "Qwen/Qwen2.5-Coder-7B-Instruct");
            
            payload.put("messages", List.of(
                Map.of("role", "system", "content", "You are a helpful AI assistant. Answer in clear English."),
                Map.of("role", "user", "content", request.getInput())
            ));
            
            payload.put("temperature", 0.7);
            payload.put("max_tokens", 800);
            payload.put("stream", false);

            // 2. Convert Map to JSON String automatically
            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 3. Parse Response
            HfChatResponse hfResponse = objectMapper.readValue(responseEntity.getBody(), HfChatResponse.class);

            if (hfResponse.getChoices() != null && !hfResponse.getChoices().isEmpty()) {
                String content = hfResponse.getChoices().get(0).getMessage().getContent();
                return new NotesResponse(content != null ? content.trim() : "No content.");
            }

            return new NotesResponse("No content received from AI.");

        } catch (Exception e) {
            // Logs mein error print karein taaki Railway logs mein dikhe
            System.err.println("AI Service Error: " + e.getMessage());
            return new NotesResponse("Error: " + e.getMessage());
        }
    }
}
