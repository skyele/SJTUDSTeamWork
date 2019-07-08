package com.server.mysql.pojo;

import com.server.Item;

import java.util.List;

public class KafkaMessage {
    private Integer id;
    private String initiator;
    private List<Item> items;
    private Double exchangeRate;
    private Boolean success;

    public KafkaMessage(Integer id, String initiator, List<Item> items, Double exchangeRate, Boolean success) {
        this.id = id;
        this.initiator = initiator;
        this.items = items;
        this.exchangeRate = exchangeRate;
        this.success = success;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(Double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
