package com.server;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private Long time;
    private List<Item> items;
    private String initiator;
    private Integer user_id;

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Order(Long time, List<Item> items, String initiator, Integer user_id) {
        this.time = time;
        this.items = items;
        this.user_id = user_id;
        this.initiator = initiator;
    }

    public Order() {
    }
}
