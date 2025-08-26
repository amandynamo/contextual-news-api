package com.news.dto;

import lombok.*;

import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmExtraction {
    private List<String> entities;
    private String intent; // category | source | score | search | nearby
    private String refinedQuery;
}