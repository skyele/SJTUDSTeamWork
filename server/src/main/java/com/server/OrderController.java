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
        public String receiveOrder(HttpServletRequest request, @RequestBody String data) throws Exception {
//        @RequestParam(value = "order", required = false) String orderString,
            System.out.println("the order is " + null);
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {//传递json数据的时候request是没有表单参数的
                String parameterName = parameterNames.nextElement();
                System.out.println(parameterName + "===" + request.getParameter(parameterName));
                //System.out.println(parameterNames.nextElement()+"=="+request.getParameter(parameterNames.nextElement()));
            }
            JsonElement jsonObject = new Gson().toJsonTree(data);
            System.out.println("body: " + data);//以{}包含的字符
            System.out.println("json object: " + jsonObject.toString());
            Order order = null;
            BufferedReader reader = request.getReader();
            char[] buf = new char[512];
            int len = 0;
            StringBuffer contentBuffer = new StringBuffer();
            while ((len = reader.read(buf)) != -1) {
                contentBuffer.append(buf, 0, len);
            }

            String content = contentBuffer.toString();

            if(content == null){
                System.out.println("is null!!!");
                content = "";
            }else {
                System.out.println("the content: "  + content);
            }

        System.out.println("the orderstring "+order);
        // acquire lock
//        Order order = null;
        System.out.println("the server get order userid: " + order.getUser_id()+ " initiator: "+order.getInitiator());
        List<Item> items = order.getItems();
        List<CuratorFramework> clients = new LinkedList<>();
        List<InterProcessMutex> mutexes = new LinkedList<>();
        List<Commodity> commodities = new LinkedList<>();
        int i;
        Collections.sort(items);
        for(i = 0; i < items.size(); i++){
            //https://stackoverflow.com/questions/30134447/what-does-an-apache-curator-connection-string-look-like
            //上面是connectString的意义
            String connectString = "http://zookeeper:2181";
            String lockPath = "/distributed-lock/" + items.get(i).getId();
            RetryPolicy retry = new ExponentialBackoffRetry(1000, 3);
            CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, 60000, 15000, retry);
            client.start();
            InterProcessMutex mutex= new InterProcessMutex(client, lockPath);
            if(mutex.acquire(10, TimeUnit.SECONDS)){
                ((LinkedList<CuratorFramework>) clients).push(client);
                ((LinkedList<InterProcessMutex>) mutexes).push(mutex);
                Commodity tmp = commodityRepository.findById(items.get(i).getId().intValue());
                ((LinkedList<Commodity>) commodities).push(tmp);
                if(tmp.getInventory() < items.get(i).getNumber())//库存不足
                    break;
            }else//可能死锁，放弃这次订单
                break;
        }

        if (i != items.size()){
            cleanAllStates(i, clients, mutexes);
            return "Invalid";
        }

        // modify mysql
        for(i = 0; i < items.size(); i++){
            Commodity tmp = commodities.get(i);
            tmp.setInventory(commodities.get(i).getInventory()-items.get(i).getNumber());
            commodityRepository.save(tmp);
        }


        KafkaMessage kafkaMessage = new KafkaMessage(1, order.getInitiator(), items, 3.8, true);
        Gson gson = new Gson();

        // send msg to kafka
        kafkaTemplate.send("orders", order.getUser_id(), gson.toJson(kafkaMessage));

        // release lock
        cleanAllStates(items.size(), clients, mutexes);
        return "Hi";
    }

    void cleanAllStates(int number, List<CuratorFramework> clients, List<InterProcessMutex> mutexes) throws Exception {
        for(int i = 0; i < number; i++){
            mutexes.get(i).release();
            distributedLock.close(clients.get(i));
        }
    }
}