package com.server.Controller;

import com.server.mysql.pojo.ExchangeRate;
import com.server.mysql.repo.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class ExchangeRateController {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @PostMapping(value = "/request")
    public Double queryRate(String currency) throws Exception {
        ExchangeRate exchangeRate = exchangeRateRepository.findByCurrency(currency);
        return exchangeRate.getRate();
    }
}
