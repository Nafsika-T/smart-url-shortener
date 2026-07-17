package com.urlshortener.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeviceClickCount {
    private String deviceType;
    private String browser;
    private Long total;
}
