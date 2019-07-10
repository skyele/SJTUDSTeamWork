package com.server;

import com.google.gson.Gson;
import com.server.Watcher.LockWatch;
import com.server.mysql.pojo.Commodity;
import com.server.mysql.pojo.KafkaMessage;
import com.server.mysql.repo.CommodityRepository;
import com.server.mysql.repo.KafkaMessageRepository;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class OrderController {

    public static Double RMB;
    public static Double USD;
    public static Double JPY;
    public static Double EUR;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private CommodityRepository commodityRepository;

    @Autowired
    private KafkaMessageRepository kafkaMessageRepository;

    private final String LOCKPATH = "/distributed-lock";

    @PostMapping(value = "/request")
    public String receiveOrder(@RequestParam(value = "order", required = false) Order orderString, @RequestBody String data) throws Exception {
        Order order = new Gson().fromJson(data, Order.class);
        System.out.println("the orderstring "+order.toString());
        // acquire lock
        System.out.println("the server get order userid: " + order.getUser_id()+ " initiator: "+order.getInitiator());
        List<Item> items = order.getItems();
        LinkedList<Commodity> commodities = new LinkedList<>();
        LinkedList<LockWatch> lockWatches = new LinkedList<>();
        System.out.println("the items size: " + items.size());
        int i;
        Collections.sort(items);
        for(i = 0; i < items.size(); i++){
            String lockPath = LOCKPATH + "/" + items.get(i).getId();
            LockWatch lockWatch = new LockWatch();
            System.out.println("the lockPath in controller: " + lockPath);
            if(lockWatch.acquire(lockPath, 5000, TimeUnit.MILLISECONDS)){
                System.out.println("the id is : " + items.get(i).getId().intValue());
                Commodity tmp = commodityRepository.findById(items.get(i).getId().intValue());
                System.out.println("the tmp is " + tmp);
                commodities.push(tmp);
                lockWatches.push(lockWatch);
                if(tmp.getInventory() < items.get(i).getNumber())//库存不足
                    break;
            }else//可能死锁，放弃这次订单
                break;
        }
        // release lock
        if (i != items.size()){
            cleanAllStates(i, lockWatches);
            return "Invalid";
        }
//         modify mysql
        for(i = 0; i < items.size(); i++){
            Commodity tmp = commodities.get(i);
            tmp.setInventory(commodities.get(i).getInventory()-items.get(i).getNumber());
            items.get(i).setPrice(tmp.getPrice());
            commodityRepository.save(tmp);
        }
        cleanAllStates(items.size(), lockWatches);
        //get exchange rate
        KafkaMessage kafkaMessage = new KafkaMessage(order.getInitiator(), new Gson().toJson(items), getRate(order.getInitiator()), true);
        kafkaMessage = kafkaMessageRepository.save(kafkaMessage);

        System.out.println("kafka msg id: " + kafkaMessage.getId());

        // send msg to kafka
        kafkaTemplate.send("orders", order.getUser_id().toString(), new Gson().toJson(kafkaMessage));
        return "Hi";
    }

    void cleanAllStates(int number, LinkedList<LockWatch> lockWatches) throws Exception {
        for(int i = 0; i < number; i++){
            lockWatches.get(i).release();
            lockWatches.get(i).closeConnection();
        }
    }

    Double getRate(String currency){
        if(currency.equals("RMB"))
            return this.RMB;
        else if(currency.equals("USD"))
            return this.USD;
        else if(currency.equals("JPY"))
            return this.JPY;
        else if(currency.equals("EUR"))
            return this.EUR;
        else
            return 0.0;
    }
}