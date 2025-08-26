package com.news.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "news_articles")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewsArticle {
    @Id
    private String id;
    private String title;
    private String description;
    private String url;

    @Indexed
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publicationDate;
    private String sourceName;
    private List<String> category;
    private double relevanceScore;
    private Double latitude;
    private Double longitude;
    private String llmSummary;
}