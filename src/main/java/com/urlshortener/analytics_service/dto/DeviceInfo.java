package com.urlshortener.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeviceInfo {
    private String deviceType;
    private String browser;
}
