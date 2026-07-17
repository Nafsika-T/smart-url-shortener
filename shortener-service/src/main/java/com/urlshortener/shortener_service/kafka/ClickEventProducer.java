package com.urlshortener.shortener_service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClickEventProducer {

    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;

    public void sendClickEvent(ClickEvent event) {
        kafkaTemplate.send("url-clicks", event);
    }
}
