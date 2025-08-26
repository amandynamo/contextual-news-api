package com.news.controller;

import com.news.dto.LlmExtraction;
import com.news.dto.NewsResponse;
import com.news.dto.TrendingEvent;
import com.news.exception.ApiError;
import com.news.model.NewsArticle;
import com.news.service.NewsService;
import com.news.service.TrendingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;


@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Validated
public class NewsController {

    @Autowired
    private NewsService newsService;
    @Autowired
    private TrendingService trendingService;


    // --------- LLM route to decide intent (optional helper) ---------
    @GetMapping("/analyze")
    @Operation(summary = "Analyze query intent", description = "Uses LLM to extract entities, intent, and optional location from query")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful analysis"),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<LlmExtraction> analyze(@RequestParam String query,
                                                 @RequestParam(required = false) Double lat,
                                                 @RequestParam(required = false) Double lon) {
        return ResponseEntity.ok(newsService.analyzeQuery(query, lat, lon));
    }


    // --------- Core endpoints ---------
    @GetMapping("/category")
    @Operation(summary = "Fetch news by category", description = "Returns paginated news articles for a given category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "No articles found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<NewsResponse> byCategory(@RequestParam String name,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(newsService.byCategory(name, page, size));
    }


    @GetMapping("/source")
    @Operation(summary = "Fetch news by source", description = "Returns paginated news articles from a specific source")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "404", description = "No articles found for given source", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<NewsResponse> bySource(@RequestParam String name,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(newsService.bySource(name, page, size));
    }


    @GetMapping("/score")
    @Operation(summary = "Fetch news by relevance score", description = "Fetches news above a certain relevance score threshold")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "400", description = "Invalid threshold or parameters", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<NewsResponse> byScore(@RequestParam(defaultValue = "0.7") double threshold,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(newsService.byScore(threshold, page, size));
    }


    @GetMapping("/search")
    @Operation(summary = "Search news", description = "Performs full-text search on news articles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "404", description = "No articles matched the query", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<NewsResponse> search(@RequestParam String query,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(newsService.search(query, page, size));
    }


    @GetMapping("/nearby")
    @Operation(summary = "Fetch nearby news", description = "Fetches geolocated news within a radius of given coordinates")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<NewsResponse> nearby(@RequestParam double lat,
                                               @RequestParam double lon,
                                               @RequestParam(defaultValue = "10") double radiusKm,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "200") int size) {
        return ResponseEntity.ok(newsService.nearby(lat, lon, radiusKm, page, size));
    }


    // --------- Trending (Bonus) ---------
    @PostMapping("/events")
    @Operation(summary = "Ingest a trending event", description = "Accepts event data to feed trending service")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Event accepted"),
            @ApiResponse(responseCode = "400", description = "Invalid event payload", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> ingestEvent(@RequestBody TrendingEvent e) {
        if (e.getTimestamp() == null) e.setTimestamp(Instant.now());
        trendingService.ingest(e);
        return ResponseEntity.accepted().build();
    }


    @GetMapping("/trending")
    @Operation(summary = "Fetch trending news", description = "Returns top trending news near given coordinates")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "404", description = "No trending articles found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<NewsArticle>> trending(@RequestParam double lat,
                                                      @RequestParam double lon,
                                                      @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(trendingService.topTrending(lat, lon, limit));
    }
}