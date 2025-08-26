package com.news.dto;

import com.news.model.NewsArticle;
import lombok.*;
import java.util.List;


@Getter @Setter @Builder
@NoArgsConstructor
public class NewsResponse {
    private long total;
    private int page;
    private List<NewsArticle> articles;

    public NewsResponse(long total, int page, List<NewsArticle> articles) {
        this.total = total;
        this.page = page;
        this.articles = articles;
    }
}
