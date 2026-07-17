package com.urlshortener.analytics_service.controller;

import com.urlshortener.analytics_service.dto.CountryClickCount;
import com.urlshortener.analytics_service.dto.DeviceClickCount;
import com.urlshortener.analytics_service.model.ClickEventEntity;
import com.urlshortener.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}/total")
    public long getTotalClicks(@PathVariable String shortCode) {
        return analyticsService.getTotalClicks(shortCode);
    }

    @GetMapping("/{shortCode}/by-country")
    public List<CountryClickCount> getClicksByCountry(@PathVariable String shortCode) {
        return analyticsService.getClicksByCountry(shortCode);
    }

    @GetMapping("/{shortCode}/by-device")
    public List<DeviceClickCount> getClicksByDevice(@PathVariable String shortCode) {
        return analyticsService.getClicksByDevice(shortCode);
    }

    @GetMapping("/{shortCode}/history")
    public List<ClickEventEntity> getClickHistory(@PathVariable String shortCode) {
        return analyticsService.getClickHistory(shortCode);
    }
}