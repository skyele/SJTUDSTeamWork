package com.server;

import com.server.pojo.Commodity;
import com.server.repo.CommodityRepository;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@SpringBootApplication
public class ExchangeRateApplication {
    private static Integer NUMBER = 4;
    private static Integer MAXITEMID;
    private static Integer index = 0;
    private static Integer TEST = 1000*3;
    private static Integer REAL = 1000*60;
    static String PATH = "/ExchangeRate";

    private static Zk zk = zkHandler();

    private static CommodityRepository commodityRepository;

    @Autowired
    public void setCommodityRepository(CommodityRepository commodityRepository) {
        ExchangeRateApplication.commodityRepository = commodityRepository;
    }

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
        MAXITEMID = Integer.parseInt(args[0]);
        initZkNode();
        initCommodity();
        initRate();
        System.out.println("thread1 run!");
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
        System.out.println("thread2 run!");
        //add inventory
        new Thread(){
            @Override
            public void run() {
                try {
                    addInventory();
                } catch (Exception e) {
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

    public static void initCommodity() throws Exception{
        String[] currencyList = {"USD","RMB","JPY","EUR"};
        Double[] priceList = {66.0,89.0,99.0,45.5};
        Integer[] inventoryList = {123,789,666,333};

        for(int i = 1; i <= MAXITEMID; i++){
            Commodity commodity = new Commodity(i, "Commodity"+i, priceList[i%4]*(0.5+Math.random()), currencyList[i%4], (int) (inventoryList[i%4]*(0.5+Math.random())));
            commodityRepository.save(commodity);
        }
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
                            System.out.println("currency: " + currency + " new rate: " + rate);
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
            index = 0;
            Thread.sleep(TEST);
        }
    }

    public static void addInventory() throws Exception {
        String connectString = "zookeeper:2181";
        while (true){
            List<Commodity> commodities = commodityRepository.findAll();
            for(Commodity commodity : commodities){
                boolean lockSucc = false;
                while(!lockSucc) {
                    //加锁
                    String lockPath = "/distributed-lock/" + commodity.getId();
                    RetryPolicy retry = new ExponentialBackoffRetry(1000, 3);
                    System.out.println("create client");
                    CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, 60000, 15000, retry);
                    System.out.println("client start");
                    client.start();
                    System.out.println("get mutex");
                    InterProcessMutex mutex = new InterProcessMutex(client, lockPath);
                    System.out.println("attempt to acquire mutex");
                    if (mutex.acquire(5, TimeUnit.SECONDS)) {
                        System.out.println("acquire mutex successfully!");
                        commodity.setInventory(commodity.getInventory() + (int) (Math.random()) * 100);
                        System.out.println("new inventory: " + commodity.getInventory());
                        lockSucc = true;
                    }
                    //放锁
                    if(lockSucc){
                        System.out.println("release lock");
                        mutex.release();
                        System.out.println("close client");
                        client.close();
                    }
                }
            }
            Thread.sleep(TEST);
        }
    }

    public static void initZkNode() throws Exception {
        zk.createNode("/distributed-lock", "new");
        for(int i = 1; i <= MAXITEMID; i++)
            zk.createNode("/distributed-lock/"+i, "new");
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
