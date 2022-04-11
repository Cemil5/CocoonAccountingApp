package com.cocoon.client.impl;

import com.cocoon.client.CurrencyClient;
import com.cocoon.dto.currency.CurrencyDto;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CurrencyClientImpl implements CurrencyClient {

    private final WebClient webClient = WebClient.create("http://data.fixer.io");

    @Override
    public CurrencyDto retrieveCurrencyDetails() {
        return webClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/api/latest")
                                .queryParam("access_key","0efa2bc3ea0bb4f378496f560590a648")
                                .queryParam("symbols","USD,GBP,RUB,JPY")
                                .queryParam("format","1")
                                .build()
                )
                .retrieve()
                .bodyToMono(CurrencyDto.class).block();
    }
}