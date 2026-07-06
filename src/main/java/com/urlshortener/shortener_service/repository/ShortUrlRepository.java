package com.urlshortener.shortener_service.repository;

import com.urlshortener.shortener_service.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    List<ShortUrl> findByUserId(Long UserId);

    Optional<ShortUrl> findByShortCode(String shortCode);
}