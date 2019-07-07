package com.server.mysql.repo;

import com.server.mysql.pojo.ExchangeRate;
import org.springframework.data.repository.CrudRepository;

public interface ExchangeRateRepository extends CrudRepository<ExchangeRate, Integer> {
    ExchangeRate findByCurrency(String currency);
}
