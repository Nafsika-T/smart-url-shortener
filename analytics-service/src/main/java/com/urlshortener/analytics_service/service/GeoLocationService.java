package com.urlshortener.analytics_service.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoLocationService {

    private final DatabaseReader databaseReader;

    public String getCountry(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return databaseReader.city(inetAddress).getCountry().getName();
        } catch (IOException | GeoIp2Exception e) {
            log.warn("Could not resolve country for IP {}: {}", ipAddress, e.getMessage());
            return null;
        }
    }
}