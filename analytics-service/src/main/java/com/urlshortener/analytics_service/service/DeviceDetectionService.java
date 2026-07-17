package com.urlshortener.analytics_service.service;

import com.urlshortener.analytics_service.dto.DeviceInfo;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.stereotype.Service;

@Service
public class DeviceDetectionService {

    public DeviceInfo parse(String userAgentString) {
        if (userAgentString == null) {
            return new DeviceInfo(null, null);
        }
        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
        return new DeviceInfo(
                userAgent.getOperatingSystem().getDeviceType().getName(),
                userAgent.getBrowser().getName());
    }
}
