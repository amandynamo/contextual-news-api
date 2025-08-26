package com.news.service;

import com.news.dto.TrendingEvent;
import com.news.model.NewsArticle;
import com.news.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TrendingService {

    @Autowired
    private final NewsArticleRepository repo;
    @Autowired
    private final DistanceService distanceService;

    // In-memory event store for demo;
    private final List<TrendingEvent> events = new CopyOnWriteArrayList<>();

    public void ingest(TrendingEvent e) {
        events.add(e);
    }


    @Cacheable(value = "trending", key = "#lat + ':' + #lon + ':' + #limit")
    public List<NewsArticle> topTrending(double lat, double lon, int limit) {
        Instant now = Instant.now();
        Map<String, Double> scores = new HashMap<>();
        for (TrendingEvent e : events) {
            double base = switch (e.getType()) {
                case "click" -> 2.0;
                case "view" -> 1.0;
                default -> 0.5;
            };
            double hoursAgo = Duration.between(e.getTimestamp(), now).toHours();
            double timeDecay = Math.exp(-hoursAgo / 24.0); // 1 day half-life approx
            double distance = distanceService.haversineKm(lat, lon, e.getLatitude(), e.getLongitude());
            double proximity = Math.exp(-distance / 50.0); // 50km decay
            double score = base * timeDecay * proximity;
            scores.merge(e.getArticleId(), score, Double::sum);
        }
        List<String> topIds = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
        Map<String, NewsArticle> byId = repo.findAllById(topIds).stream().collect(Collectors.toMap(NewsArticle::getId, a -> a));
        return topIds.stream().map(byId::get).filter(Objects::nonNull).toList();
    }
}