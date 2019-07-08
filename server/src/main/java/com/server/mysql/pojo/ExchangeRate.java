package com.server.mysql.pojo;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ExchangeRate {
    @Id
    private Integer id;
    private String currency;
    private Double rate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}
