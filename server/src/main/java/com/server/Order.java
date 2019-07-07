package com.server;

import java.util.List;

public class Order {
    private Long time;
    private List<Item> items;
    private String initiator;
    private String user_id;

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

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Order(Long time, List<Item> items, String initiator, String user_id) {
        this.time = time;
        this.items = items;
        this.user_id = user_id;
        this.initiator = initiator;
    }
}
