package com.urlshortener.shortener_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CachedUrlData {
    private String originalUrl;
    private Long userId;
    private boolean active;
}
