package com.cocoon.client.impl;

import com.cocoon.client.CurrencyClient;
import com.cocoon.dto.currency.CurrencyDto;
import com.cocoon.dto.currency.Rate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "mock.currency.enabled", havingValue = "true")
public class MockCurrencyClientImpl implements CurrencyClient {

    @Override
    public CurrencyDto retrieveCurrencyDetails() {
        CurrencyDto currencyDto = new CurrencyDto();
        currencyDto.rates = new Rate();
        currencyDto.rates.GBP = 0.82d;
        currencyDto.rates.JPY = 127.7d;
        currencyDto.rates.RUB = 118.02d;
        currencyDto.rates.USD = 1.10d;
        return currencyDto;
    }
}