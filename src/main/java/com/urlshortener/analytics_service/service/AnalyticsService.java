package com.urlshortener.analytics_service.service;

import com.urlshortener.analytics_service.dto.CountryClickCount;
import com.urlshortener.analytics_service.dto.DeviceClickCount;
import com.urlshortener.analytics_service.dto.DeviceInfo;
import com.urlshortener.analytics_service.kafka.ClickEvent;
import com.urlshortener.analytics_service.model.ClickEventEntity;
import com.urlshortener.analytics_service.repository.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;
    private final GeoLocationService geoLocationService;
    private final DeviceDetectionService deviceDetectionService;

    public void processClickEvent(ClickEvent event) {
        String country= geoLocationService.getCountry(event.getIpAddress());
        DeviceInfo deviceInfo = deviceDetectionService.parse(event.getUserAgent());

        ClickEventEntity clickEventEntity= new ClickEventEntity();
        clickEventEntity.setShortCode(event.getShortCode());
        clickEventEntity.setUserId(event.getUserId());
        clickEventEntity.setClickedAt(event.getClickAt());
        clickEventEntity.setIpAddress(event.getIpAddress());
        clickEventEntity.setUserAgent(event.getUserAgent());
        clickEventEntity.setCountry(country);
        clickEventEntity.setDeviceType(deviceInfo.getDeviceType());
        clickEventEntity.setBrowser(deviceInfo.getBrowser());

        clickEventRepository.save(clickEventEntity);
    }

    public long getTotalClicks(String shortCode) {
        return clickEventRepository.countByShortCode(shortCode);
    }

    public List<CountryClickCount> getClicksByCountry(String shortCode) {
        return clickEventRepository.countClicksByCountry(shortCode);
    }

    public List<DeviceClickCount> getClicksByDevice(String shortCode) {
        return clickEventRepository.countClicksByDevice(shortCode);
    }

    public List<ClickEventEntity> getClickHistory(String shortCode) {
        return clickEventRepository.findByShortCode(shortCode);
    }
}