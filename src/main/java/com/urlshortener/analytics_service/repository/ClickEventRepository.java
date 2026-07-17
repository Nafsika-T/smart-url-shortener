package com.urlshortener.analytics_service.repository;

import com.urlshortener.analytics_service.dto.CountryClickCount;
import com.urlshortener.analytics_service.dto.DeviceClickCount;
import com.urlshortener.analytics_service.model.ClickEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEventEntity, Long> {

    List<ClickEventEntity> findByShortCode(String shortCode);

    long countByShortCode(String shortCode);

    @Query("SELECT new com.urlshortener.analytics_service.dto.CountryClickCount(c.country, COUNT(c)) " +
            "FROM ClickEventEntity c " +
            "WHERE c.shortCode = :shortCode " +
            "GROUP BY c.country")
    List<CountryClickCount> countClicksByCountry(@Param("shortCode") String shortCode);

    @Query("SELECT new com.urlshortener.analytics_service.dto.DeviceClickCount(c.deviceType, c.browser, COUNT(c)) " +
            "FROM ClickEventEntity c " +
            "WHERE c.shortCode = :shortCode " +
            "GROUP BY c.deviceType, c.browser")
    List<DeviceClickCount> countClicksByDevice(@Param("shortCode") String shortCode);
}