package com.urlshortener.analytics_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "click_events")
public class ClickEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shortCode;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    @Column(nullable = false)
    private String ipAddress;

    @Column(length = 512)
    private String userAgent;

    private String country;

    private String deviceType;

    private String browser;
}