package com.news.service;


import com.news.dto.LlmExtraction;

public interface LlmClient {
    LlmExtraction extract(String userQuery, Double lat, Double lon);

    String summarize(String title, String description);
}