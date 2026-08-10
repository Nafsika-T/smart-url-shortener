package com.urlshortener.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ClickHistoryEntry {
    private String shortCode;
    private LocalDateTime clickedAt;
    private String country;
    private String deviceType;
    private String browser;
}
