package com.news.dto;

import lombok.*;
import java.time.Instant;


@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TrendingEvent {
private String userId;
private String articleId;
private String type; // view | click
private double latitude;
private double longitude;
private Instant timestamp;
}