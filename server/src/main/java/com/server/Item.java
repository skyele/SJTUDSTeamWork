package com.server;

public class Item {
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
}
