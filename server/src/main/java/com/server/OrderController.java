package com.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.server.mysql.pojo.Commodity;
import com.server.mysql.pojo.KafkaMessage;
import com.server.mysql.repo.CommodityRepository;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.CountDownLatch;
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
    private Zk zk;

    @Autowired
    private CommodityRepository commodityRepository;
    private DistributedLock distributedLock;

    private final String lockPath = "/distributed-lock";

    @PostMapping(value = "/request")
//    public String receiveOrder(String orderString) throws Exception {
//        public String receiveOrder(HttpServletRequest request, @RequestBody String data) throws Exception {
    //        @RequestParam(value = "order", required = false) String orderString,
    public String receiveOrder(@RequestParam(value = "order", required = false) Order orderString, @RequestBody String data) throws Exception {
        Order order = new Gson().fromJson(data, Order.class);
        System.out.println("the orderstring "+order.toString());
        // acquire lock
//        Order order = null;
        System.out.println("the server get order userid: " + order.getUser_id()+ " initiator: "+order.getInitiator());
        List<Item> items = order.getItems();
        List<CuratorFramework> clients = new LinkedList<>();
        List<InterProcessMutex> mutexes = new LinkedList<>();
        List<Commodity> commodities = new LinkedList<>();
        System.out.println("the items size: " + items.size());
        int i;
        Collections.sort(items);
        //uncomment!!!!!!!!!!!!!!!!!!!
//        for(i = 0; i < items.size(); i++){
//            //https://stackoverflow.com/questions/30134447/what-does-an-apache-curator-connection-string-look-like
//            //上面是connectString的意义
//            String connectString = "zookeeper:2181";
//            String lockPath = "/distributed-lock/" + items.get(i).getId();
//            RetryPolicy retry = new ExponentialBackoffRetry(1000, 3);
//            CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, 60000, 15000, retry);
//            client.start();
//            InterProcessMutex mutex= new InterProcessMutex(client, lockPath);
//            if(mutex.acquire(10, TimeUnit.SECONDS)){
//                ((LinkedList<CuratorFramework>) clients).push(client);
//                ((LinkedList<InterProcessMutex>) mutexes).push(mutex);
//                Commodity tmp = commodityRepository.findById(items.get(i).getId().intValue());
//                System.out.println("the tmp is " + tmp);
//                ((LinkedList<Commodity>) commodities).push(tmp);
//                if(tmp.getInventory() < items.get(i).getNumber())//库存不足
//                    break;
//            }else//可能死锁，放弃这次订单
//                break;
//        }
//
//        if (i != items.size()){
//            cleanAllStates(i, clients, mutexes);
//            return "Invalid";
//        }
//
//        // modify mysql
//        for(i = 0; i < items.size(); i++){
//            Commodity tmp = commodities.get(i);
//            tmp.setInventory(commodities.get(i).getInventory()-items.get(i).getNumber());
//            commodityRepository.save(tmp);
//        }

        //get exchange rate
//        KafkaMessage kafkaMessage = new KafkaMessage(order.getInitiator(), items, getRate(order.getInitiator()), true);

        KafkaMessage kafkaMessage = new KafkaMessage(1, order.getInitiator(), new Gson().toJson(items), 3.8, true);
        Gson gson = new Gson();

        // send msg to kafka
        kafkaTemplate.send("orders", order.getUser_id().toString(), gson.toJson(kafkaMessage));

        // release lock
        //uncomment!!!!!!!!!!!!!!!!!!!1
//        cleanAllStates(items.size(), clients, mutexes);
        return "Hi";
    }

    void cleanAllStates(int number, List<CuratorFramework> clients, List<InterProcessMutex> mutexes) throws Exception {
        for(int i = 0; i < number; i++){
            mutexes.get(i).release();
            distributedLock.close(clients.get(i));
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