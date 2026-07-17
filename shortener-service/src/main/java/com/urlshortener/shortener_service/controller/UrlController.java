package com.urlshortener.shortener_service.controller;

import com.urlshortener.shortener_service.dto.CreateUrlRequest;
import com.urlshortener.shortener_service.dto.UrlResponse;
import com.urlshortener.shortener_service.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponse> createShortUrl(
            @Valid @RequestBody CreateUrlRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.createShortUrl(request, userId));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {

        String ipAddress = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        String originalUrl = urlService.redirect(shortCode, ipAddress, userAgent);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("/api/urls")
    public ResponseEntity<List<UrlResponse>> getUserUrls(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(urlService.getUserUrls(userId));
    }

    @DeleteMapping("/api/urls/{id}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        urlService.deleteUrl(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/urls/{id}/deactivate")
    public ResponseEntity<UrlResponse> deactivateUrl(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(urlService.deactivateUrl(id, userId));
    }


    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}