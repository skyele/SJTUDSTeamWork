package com.server;

import com.server.mysql.pojo.ExchangeRate;
import com.server.mysql.repo.ExchangeRateRepository;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CountDownLatch;


@SpringBootApplication
public class ExchangeRateApplication {
    private static Integer NUMBER = 4;
    private static Integer index = 0;
    @Autowired
    static ExchangeRateRepository exchangeRateRepository;
    static String PATH = "/ExchangeRate/";

    @Bean
    public static final Zk zkHandler(){
        Zk zk = new Zk();
        try{
            zk.connectZookeeper("http://zookeeper:2181");
        }catch (Exception e) {

        }
        return zk;
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(ExchangeRateApplication.class, args);
        changeRate();
        //注册watch
    }

    public static void changeRate() throws InterruptedException {
        while(true){
            CountDownLatch countDownLatch = new CountDownLatch(NUMBER);
            //多线程
            for(int i = 0; i < NUMBER; i++){
                new Thread() {
                    public void run() {
                        String currency = getCurrency(index++);
                        ExchangeRate exchangeRate = exchangeRateRepository.findByCurrency(currency);
                        exchangeRate.setRate(exchangeRate.getRate() + (double)(Math.random() * 2) - 1);// +- 0.x
                        exchangeRateRepository.save(exchangeRate);
                        try {
                            //setData - Watch
                            zkHandler().setData(PATH+exchangeRate.getCurrency(), exchangeRate.getRate().toString());
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
