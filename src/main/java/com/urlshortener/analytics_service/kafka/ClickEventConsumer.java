package com.urlshortener.analytics_service.kafka;

import com.urlshortener.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickEventConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "url-clicks", groupId = "analytics-service")
    public void consume(ClickEvent event) {
        log.info("Received click event for shortCode: {}", event.getShortCode());
        analyticsService.processClickEvent(event);
    }
}