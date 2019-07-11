package com.server.mysql.pojo;

import com.server.Item;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

@Entity
public class KafkaMessage {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;
    private String initiator;
    private String items;
    private Double exchangeRate;
    private Boolean success;

    public KafkaMessage(Integer id, String initiator, String items, Double exchangeRate, Boolean success) {
        this.id = id;
        this.initiator = initiator;
        this.items = items;
        this.exchangeRate = exchangeRate;
        this.success = success;
    }

    public KafkaMessage(String initiator, String items, Double exchangeRate, Boolean success) {
        this.initiator = initiator;
        this.items = items;
        this.exchangeRate = exchangeRate;
        this.success = success;
    }

    public KafkaMessage(){

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

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
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
