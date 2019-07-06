package com.sender;

import java.util.Random;

public class User {
    private int idRange;
    public User(int range){
        idRange = range;
    }

    public int getUser_id(){
        System.out.println(idRange);
        Random r = new Random();
        return r.nextInt(idRange)+1;
    }
}
