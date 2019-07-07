package com.server;

import com.server.mysql.pojo.Commodity;
import com.server.mysql.repo.CommodityRepository;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Time;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RestController
public class OrderController {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private Zk zk;

    @Autowired
    private CommodityRepository commodityRepository;
    private DistributedLock distributedLock;

    private final String lockPath = "/distributed-lock";

    @PostMapping(value = "/request")
    public String receiveOrder(Order order) throws Exception {
        // acquire lock
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
                Commodity tmp = commodityRepository.findByID(items.get(i).getId());
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
        // send msg to kafka
        kafkaTemplate.send("orders", order.getUser_id(), "developing");

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