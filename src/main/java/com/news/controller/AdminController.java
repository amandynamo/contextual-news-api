package com.news.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.model.NewsArticle;
import com.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final NewsService newsService;
    private final ObjectMapper objectMapper;  // inject Spring Boot mapper

    @PostMapping(value = "/load", consumes = {"multipart/form-data"})
    public ResponseEntity<String> load(@RequestPart("file") MultipartFile file) throws IOException {
        List<NewsArticle> articles = objectMapper.readValue(
                file.getInputStream(),
                new TypeReference<List<NewsArticle>>() {
                }
        );
        newsService.loadArticles(articles);
        return ResponseEntity.ok("Loaded " + articles.size() + " articles");
    }
}