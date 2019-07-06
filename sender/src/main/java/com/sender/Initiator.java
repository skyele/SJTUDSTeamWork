package com.sender;

import java.util.Random;

public class Initiator {
    public String getCurrency(){
        Random r = new Random();
        int i = r.nextInt(4);
        switch (i){
            case 0:
                return "USD";
            case 1:
                return "RMB";
            case 2:
                return "JPY";
            case 3:
                return "EUR";
            default:
                System.out.println("error");
                return "?";
        }
    }
}
