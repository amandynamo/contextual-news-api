package com.news.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Error response returned by the API")
public class ApiError {
    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Short error reason", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "No articles found for category: Sports")
    private String message;

    @Schema(description = "Request path", example = "/news/category")
    private String path;

    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp;

    public ApiError(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}
