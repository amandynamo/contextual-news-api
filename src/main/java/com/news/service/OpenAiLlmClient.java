package com.news.service;

import com.news.dto.LlmExtraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.util.List;
import java.util.Map;


@Component
@Profile("openai")
public class OpenAiLlmClient implements LlmClient {

    @Autowired
    private WebClient webClient;
    private final String model;


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
        String system = "Extract entities (people, orgs, locations) and infer intent among: category, source, score, search, nearby. ";
        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", userQuery)
                )
        );


        Map<?,?> response = webClient.post()
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> Mono.just(Map.of()))
                .block();

        if (response == null || response.isEmpty()) {
            return new SimpleLlmClient().extract(userQuery, lat, lon);
        }
        return new SimpleLlmClient().extract(userQuery, lat, lon);
    }


    @Override
    public String summarize(String title, String description) {
        String prompt = "Summarize this news in one sentence: Title: " + title + ". Description: " + description;
// For brevity, call simple impl; wire real call similarly to extract
        return new SimpleLlmClient().summarize(title, description);
    }
}