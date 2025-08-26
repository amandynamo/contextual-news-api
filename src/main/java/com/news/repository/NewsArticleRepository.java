package com.news.repository;

import com.news.model.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsArticleRepository extends MongoRepository<NewsArticle, String> {
    Page<NewsArticle> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<NewsArticle> findBySourceNameIgnoreCase(String sourceName, Pageable pageable);

    Page<NewsArticle> findByRelevanceScoreGreaterThanEqual(double relevanceScore, Pageable pageable);


    @Query("{ $or: [ { title: { $regex: ?0, $options: 'i' } }, { description: { $regex: ?0, $options: 'i' } } ] }")
    Page<NewsArticle> searchByText(String regex, Pageable pageable);
}