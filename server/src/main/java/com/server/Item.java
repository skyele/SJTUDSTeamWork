package com.server;

import javax.persistence.Id;
import javax.persistence.Table;

public class Item implements Comparable<Item>{
    private Integer id;
    private Integer number;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Item(Integer id, Integer number) {
        this.id = id;
        this.number = number;
    }

    @Override
    public int compareTo(Item o) {
        return this.getId().compareTo(o.getId());
    }
}
