package com.news.service;

import com.news.dto.LlmExtraction;
import com.news.dto.NewsResponse;
import com.news.exception.InvalidRequestException;
import com.news.exception.ResourceNotFoundException;
import com.news.model.NewsArticle;
import com.news.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {
    @Autowired
    private NewsArticleRepository repo;

    @Autowired
    private LlmClient llm;

    @Autowired
    private DistanceService distanceService;


    public LlmExtraction analyzeQuery(String query, Double lat, Double lon) {
        if (query == null || query.trim().isEmpty()) {
            throw new InvalidRequestException("Query parameter cannot be empty");
        }
        return llm.extract(query, lat, lon);
    }

    public NewsResponse byCategory(String category, int page, int size) {
        if (category == null || category.isBlank()) {
            throw new InvalidRequestException("Category name cannot be empty");
        }
        Page<NewsArticle> newsArticlePage = repo.findByCategoryIgnoreCase(category, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publicationDate")));
        if (newsArticlePage.isEmpty()) {
            throw new ResourceNotFoundException("No articles found for category: " + category);
        }
        List<NewsArticle> enriched = enrich(newsArticlePage.getContent());
        return new NewsResponse(newsArticlePage.getTotalElements(), page, topN(enriched, 5));
    }

    public NewsResponse bySource(String source, int page, int size) {
        if (source == null || source.isBlank()) {
            throw new InvalidRequestException("Source name cannot be empty");
        }
        Page<NewsArticle> newsArticlePage = repo.findBySourceNameIgnoreCase(source, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publicationDate")));
        if (newsArticlePage.isEmpty()) {
            throw new ResourceNotFoundException("No articles found for source: " + source);
        }
        List<NewsArticle> enriched = enrich(newsArticlePage.getContent());
        return new NewsResponse(newsArticlePage.getTotalElements(), page, topN(enriched, 5));
    }

    public NewsResponse byScore(double threshold, int page, int size) {
        if (threshold < 0 || threshold > 1) {
            throw new InvalidRequestException("Relevance score threshold must be between 0 and 1");
        }
        Page<NewsArticle> newsArticlePage = repo.findByRelevanceScoreGreaterThanEqual(threshold, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "relevanceScore")));
        if (newsArticlePage.isEmpty()) {
            throw new ResourceNotFoundException("No articles found for score: " + threshold);
        }
        List<NewsArticle> enriched = enrich(newsArticlePage.getContent());
        List<NewsArticle> sorted = enriched.stream()
                .sorted(Comparator.comparingDouble(NewsArticle::getRelevanceScore).reversed())
                .collect(Collectors.toList());
        return new NewsResponse(newsArticlePage.getTotalElements(), page, topN(sorted, 5));
    }

    public NewsResponse search(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            throw new InvalidRequestException("Search query cannot be empty");
        }
        Page<NewsArticle> p = repo.searchByText(query, PageRequest.of(page, size));
        if (p.isEmpty()) {
            throw new ResourceNotFoundException("No articles found matching query: " + query);
        }
        List<NewsArticle> enriched = enrich(p.getContent());
        List<NewsArticle> ranked = enriched.stream()
                .sorted(Comparator.comparingDouble(NewsArticle::getRelevanceScore).reversed())
                .collect(Collectors.toList());
        return new NewsResponse(p.getTotalElements(), page, topN(ranked, 5));
    }

    public NewsResponse nearby(double lat, double lon, double radiusKm, int page, int size) {
// For simplicity, fetch a page then filter by distance (could use Mongo geo-index in prod)
        if (radiusKm <= 0) {
            throw new InvalidRequestException("Radius must be greater than 0");
        }
        Page<NewsArticle> p = repo.findAll(PageRequest.of(page, size));
        List<NewsArticle> filtered = p.getContent().stream()
                .filter(a -> a.getLatitude() != null && a.getLongitude() != null)
                .peek(a -> a.setLlmSummary(llm.summarize(a.getTitle(), a.getDescription())))
                .filter(a -> distanceService.haversineKm(lat, lon, a.getLatitude(), a.getLongitude()) <= radiusKm)
                .sorted(Comparator.comparingDouble(a -> distanceService.haversineKm(lat, lon, a.getLatitude(), a.getLongitude())))
                .collect(Collectors.toList());
        if (filtered.isEmpty()) {
            throw new ResourceNotFoundException("No nearby articles found within " + radiusKm + " km");
        }
        return new NewsResponse(filtered.size(), page, topN(filtered, 5));
    }

    public void loadArticles(List<NewsArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            throw new InvalidRequestException("No articles provided to load");
        }
        for (NewsArticle a : articles) {
            if (a.getPublicationDate() == null) a.setPublicationDate(LocalDateTime.now());
            repo.save(a);
        }
    }


    private List<NewsArticle> enrich(List<NewsArticle> list) {
        return list.stream().peek(a -> a.setLlmSummary(llm.summarize(a.getTitle(), a.getDescription()))).toList();
    }

    private List<NewsArticle> topN(List<NewsArticle> list, int n) {
        return list.stream().limit(n).toList();
    }
}