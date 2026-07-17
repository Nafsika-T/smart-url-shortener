package com.urlshortener.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CountryClickCount {
    private String country;
    private Long total;
}
