package com.news.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.dto.LlmExtraction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Profile("openai")
public class OpenAiLlmClient implements LlmClient {

    private final WebClient webClient;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();

    public OpenAiLlmClient(@Value("${app.llm.openai.apiKey:}") String apiKey,
                           @Value("${app.llm.openai.model:gpt-4o-mini}") String model) {
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public LlmExtraction extract(String userQuery, Double lat, Double lon) {
        String system = """
            You are an intent and entity extractor for a news API.
            Extract relevant entities (people, orgs, locations, sources) and determine intent.
            Possible intents: category, source, nearby, trending, keyword.
            
            Return JSON only in format:
            {
              "entities": ["..."],
              "intent": "..."
            }
            """;

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", userQuery)
                )
        );

        try {
            Map<?, ?> response = webClient.post()
                    .body(BodyInserters.fromValue(payload))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> Mono.just(Collections.emptyMap()))
                    .block();

            if (response == null || response.isEmpty()) {
                return new SimpleLlmClient().extract(userQuery, lat, lon);
            }

            // Extract content string from OpenAI JSON
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return new SimpleLlmClient().extract(userQuery, lat, lon);
            }

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");

            return mapper.readValue(content, LlmExtraction.class);

        } catch (Exception e) {
            e.printStackTrace();
            return new SimpleLlmClient().extract(userQuery, lat, lon);
        }
    }

    @Override
    public String summarize(String title, String description) {
        String system = "Summarize the following news article in one sentence.";
        String prompt = "Title: " + title + "\nDescription: " + description;

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", prompt)
                )
        );

        try {
            Map<?, ?> response = webClient.post()
                    .body(BodyInserters.fromValue(payload))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.isEmpty()) {
                return new SimpleLlmClient().summarize(title, description);
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return new SimpleLlmClient().summarize(title, description);
            }

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            e.printStackTrace();
            return new SimpleLlmClient().summarize(title, description);
        }
    }
}
