package com.server;

import com.server.pojo.Commodity;
import com.server.repo.CommodityRepository;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;


@SpringBootApplication
public class ExchangeRateApplication {
    private static Integer NUMBER = 4;
    private static Integer index = 0;
    static String PATH = "/ExchangeRate";

    private static Zk zk = zkHandler();

    @Autowired
    private static CommodityRepository commodityRepository;

    @Bean
    public static final Zk zkHandler(){
        Zk zk = new Zk();
        try{
            zk.connectZookeeper("zookeeper:2181");
        }catch (Exception e) {

        }
        return zk;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ExchangeRateApplication.class, args);
        initRate();
        //exchange rate
        new Thread(){
            public void run() {
                try {
                    changeRate();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        //add inventory
        new Thread(){
            @Override
            public void run() {
                try {
                    addInventory();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void initRate() throws Exception {
        zk.createNode(PATH, String.valueOf(0.0));
        zk.createNode(PATH+"/RMB", String.valueOf(3.0));
        zk.createNode(PATH+"/USD", String.valueOf(12.0));
        zk.createNode(PATH+"/JPY", String.valueOf(0.3));
        zk.createNode(PATH+"/EUR", String.valueOf(9.0));
    }

    public static void changeRate() throws InterruptedException {
        while (true){
            CountDownLatch countDownLatch = new CountDownLatch(NUMBER);
            //多线程
            for(int i = 0; i < NUMBER; i++){
                new Thread() {
                    public void run() {
                        String currency = getCurrency(index++);
                        Double rate = 0.0;
                        try {
                            rate = Double.parseDouble(new String(zk.getData(PATH + "/" + currency, false)));
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        rate += (Double) (Math.random() * 2) - 1;// +- 0.x
                        while(rate <= 0){
                            rate += (Double)(Math.random());
                        }
                        try {
                            //setData - Watch 注册watch
                            zk.setData(PATH + "/" + currency, rate.toString());
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        countDownLatch.countDown();
                    }
                }.start();
            }
            //countDownLatch wait
            countDownLatch.await();
            Thread.sleep(1000*60);
        }
    }

    public static void addInventory() throws InterruptedException {
        while (true){
            List<Commodity> commodities = commodityRepository.findAll();
            for(Commodity commodity : commodities){
                commodity.setInventory(commodity.getInventory() +  (int)(Math.random()) * 100);
            }
            Thread.sleep(1000*30);
        }
    }

    public static String getCurrency(int i){
        switch (i){
            case 0:
                return "RMB";
            case 1:
                return "USD";
            case 2:
                return "JPY";
            case 3:
                return "EUR";
            default:
                return "NULL";
        }
    }
}
