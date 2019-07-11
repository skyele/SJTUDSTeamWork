package com.server.Watcher;

import com.server.OrderController;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class RateWatch implements Watcher {

    private ZooKeeper zooKeeper;
    private static final int SESSION_TIME_OUT = 2000;
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public RateWatch(String path){
        //new zookeeper
        try {
            zooKeeper = new ZooKeeper("zookeeper:2181", SESSION_TIME_OUT, this);
            connectedSemaphore.await();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //注册 watch
        try {
            zooKeeper.getData(path, true, null);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if ( Event.KeeperState.SyncConnected == watchedEvent.getState() ) {
            connectedSemaphore.countDown();
        }
        if(watchedEvent.getType() == Event.EventType.NodeCreated  || watchedEvent.getType() == Event.EventType.NodeDataChanged){
            //到zookeeper getdata拿数据
//            System.out.println("data has changed!!!");
            Double rate = null;
            try {
                rate = Double.parseDouble(new String(zooKeeper.getData(watchedEvent.getPath(), false, null)));
                zooKeeper.getData(watchedEvent.getPath(), true, null);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("the path is " + watchedEvent.getPath() +" new rate: " + rate);
            if(watchedEvent.getPath().contains("RMB"))
                OrderController.RMB = rate;
            else if(watchedEvent.getPath().contains("USD"))
                OrderController.USD = rate;
            else if(watchedEvent.getPath().contains("JPY"))
                OrderController.JPY = rate;
            else if(watchedEvent.getPath().contains("EUR"))
                OrderController.EUR = rate;
        }

    }
}
