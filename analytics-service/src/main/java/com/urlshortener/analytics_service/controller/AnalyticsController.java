package com.urlshortener.analytics_service.controller;

import com.urlshortener.analytics_service.dto.ClickHistoryEntry;
import com.urlshortener.analytics_service.dto.CountryClickCount;
import com.urlshortener.analytics_service.dto.DeviceClickCount;
import com.urlshortener.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}/total")
    public long getTotalClicks(@PathVariable String shortCode, @RequestHeader("X-User-Id") Long userId) {
        return analyticsService.getTotalClicks(shortCode, userId);
    }

    @GetMapping("/{shortCode}/by-country")
    public List<CountryClickCount> getClicksByCountry(@PathVariable String shortCode, @RequestHeader("X-User-Id") Long userId) {
        return analyticsService.getClicksByCountry(shortCode, userId);
    }

    @GetMapping("/{shortCode}/by-device")
    public List<DeviceClickCount> getClicksByDevice(@PathVariable String shortCode, @RequestHeader("X-User-Id") Long userId) {
        return analyticsService.getClicksByDevice(shortCode, userId);
    }

    @GetMapping("/{shortCode}/history")
    public List<ClickHistoryEntry> getClickHistory(@PathVariable String shortCode, @RequestHeader("X-User-Id") Long userId) {
        return analyticsService.getClickHistory(shortCode, userId);
    }
}