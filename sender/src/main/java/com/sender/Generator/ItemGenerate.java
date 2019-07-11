package com.sender.Generator;
import java.util.Random;

public class ItemGenerate {
    private int idRange;
    private int numberRange = 50;
    private Random r;
    public ItemGenerate(int idRange){
        this.idRange = idRange;
        r = new Random();
    }

    public int getItem_id(){
        return r.nextInt(idRange)+1;
    }

    public int getNumber(){
        return r.nextInt(numberRange)+1;
    }
}
