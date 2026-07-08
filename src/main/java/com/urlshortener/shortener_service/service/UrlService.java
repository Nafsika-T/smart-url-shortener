package com.urlshortener.shortener_service.service;

import com.urlshortener.shortener_service.dto.CreateUrlRequest;
import com.urlshortener.shortener_service.dto.UrlResponse;
import com.urlshortener.shortener_service.exception.ResourceNotFoundException;
import com.urlshortener.shortener_service.exception.UnauthorizedException;
import com.urlshortener.shortener_service.kafka.ClickEvent;
import com.urlshortener.shortener_service.kafka.ClickEventProducer;
import com.urlshortener.shortener_service.model.ShortUrl;
import com.urlshortener.shortener_service.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ClickEventProducer clickEventProducer;

    private static final String BASE_URL = "http://localhost:8082/";
    private static final long REDIS_TTL_HOURS = 24;

    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request, Long userId) {
        String shortCode = UUID.randomUUID().toString().substring(0, 8);

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(request.getOriginalUrl());
        shortUrl.setShortCode(shortCode);
        shortUrl.setUserId(userId);

        ShortUrl saved = shortUrlRepository.save(shortUrl);

        redisTemplate.opsForValue().set(shortCode, request.getOriginalUrl(), REDIS_TTL_HOURS, TimeUnit.HOURS);

        return toResponse(saved);
    }

    @Transactional
    public String redirect(String shortCode, String ipAddress, String userAgent) {
        String cachedUrl = (String) redisTemplate.opsForValue().get(shortCode);

        if (cachedUrl != null) {
            publishClickEvent(shortCode, ipAddress, userAgent);
            return cachedUrl;
        }

        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode));

        if (!shortUrl.isActive()) {
            throw new ResourceNotFoundException("Short URL is deactivated: " + shortCode);
        }

        redisTemplate.opsForValue().set(shortCode, shortUrl.getOriginalUrl(), REDIS_TTL_HOURS, TimeUnit.HOURS);

        publishClickEvent(shortCode, ipAddress, userAgent);

        return shortUrl.getOriginalUrl();
    }

    public List<UrlResponse> getUserUrls(Long userId) {
        return shortUrlRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteUrl(Long id, Long userId) {
        ShortUrl shortUrl = shortUrlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + id));

        if (!shortUrl.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not allowed to delete this URL");
        }

        redisTemplate.delete(shortUrl.getShortCode());
        shortUrlRepository.delete(shortUrl);
    }

    @Transactional
    public UrlResponse deactivateUrl(Long id, Long userId) {
        ShortUrl shortUrl = shortUrlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + id));

        if (!shortUrl.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not allowed to deactivate this URL");
        }

        shortUrl.setActive(false);
        redisTemplate.delete(shortUrl.getShortCode());

        return toResponse(shortUrlRepository.save(shortUrl));
    }

    private void publishClickEvent(String shortCode, String ipAddress, String userAgent) {
        shortUrlRepository.findByShortCode(shortCode).ifPresent(shortUrl -> {
            shortUrl.setClickCount(shortUrl.getClickCount() + 1);
            shortUrlRepository.save(shortUrl);
            clickEventProducer.sendClickEvent(new ClickEvent(
                    shortCode,
                    shortUrl.getUserId(),
                    LocalDateTime.now(),
                    ipAddress,
                    userAgent
            ));
        });
    }

    private UrlResponse toResponse(ShortUrl shortUrl) {
        return new UrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                BASE_URL + shortUrl.getShortCode(),
                shortUrl.isActive(),
                shortUrl.getCreatedAt(),
                shortUrl.getClickCount()
        );
    }
}