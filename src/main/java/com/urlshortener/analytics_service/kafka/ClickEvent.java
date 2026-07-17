package com.urlshortener.analytics_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClickEvent {
    private String shortCode;
    private Long userId;
    private LocalDateTime clickAt;
    private String ipAddress;
    private String userAgent;
}
