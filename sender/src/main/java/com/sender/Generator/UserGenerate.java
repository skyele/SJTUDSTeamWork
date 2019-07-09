package com.sender.Generator;

import java.util.Random;

public class UserGenerate {
    private int idRange;
    public UserGenerate(int range){
        idRange = range;
    }

    public int getUser_id(){
        Random r = new Random();
        return r.nextInt(idRange)+1;
    }
}
