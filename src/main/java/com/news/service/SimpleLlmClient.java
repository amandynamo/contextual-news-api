package com.news.service;

import com.news.dto.LlmExtraction;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
@Profile("simple")
public class SimpleLlmClient implements LlmClient {
    private static final Pattern ENTITY_PATTERN = Pattern.compile("[A-Z][a-z]+(?: [A-Z][a-z]+)*");

    @Override
    public LlmExtraction extract(String userQuery, Double lat, Double lon) {
        List<String> entities = new ArrayList<>();
        Matcher m = ENTITY_PATTERN.matcher(userQuery);
        while (m.find()) entities.add(m.group());


        String intent = inferIntent(userQuery);
        return LlmExtraction.builder()
                .entities(entities)
                .intent(intent)
                .refinedQuery(userQuery)
                .build();
    }


    private String inferIntent(String q) {
        String s = q.toLowerCase(Locale.ROOT);
        if (s.contains("near") || s.contains("nearby") || s.contains("around")) return "nearby";
        if (s.contains("category") || s.contains("technology") || s.contains("business") || s.contains("sports"))
            return "category";
        if (s.contains("source") || s.contains("from")) return "source";
        if (s.contains("top") || s.contains("latest")) return "score";
        if (s.contains("search") || s.contains("about") || s.contains("on ")) return "search";
        return "search";
    }


    @Override
    public String summarize(String title, String description) {
        String desc = description == null ? "" : description;
        return (title + ": " + (desc.length() > 140 ? desc.substring(0, 140) + "..." : desc)).trim();
    }
}