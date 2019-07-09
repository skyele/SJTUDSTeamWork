package com.sender;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.*;

public class Order implements Serializable {
    private Initiator initiator;
    private Item item;
    private User user;
    private int itemNumberRange = 4;
    Random r;

    public Order(User user, Item item){
        initiator = new Initiator();
        this.item = item;
        this.user = user;
        r = new Random();
    }

    public String getJSONOrder(){
        Date date = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", user.getUser_id());
        map.put("initiator", initiator.getCurrency());
        map.put("time", date.getTime());

        LinkedList<Map<String, Object>>items = new LinkedList<>();
        int loop = r.nextInt(itemNumberRange)+1;
        for(int j = 0; j < loop; j++){
            Map<String, Object>itemInfo = new HashMap<>();
            itemInfo.put("id", item.getItem_id());
            itemInfo.put("number", item.getNumber());
            items.add(itemInfo);
        }
        map.put("items", new Gson().toJson(items));
        return new Gson().toJson(map);
    }
}
