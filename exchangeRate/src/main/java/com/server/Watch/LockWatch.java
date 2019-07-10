package com.server.Watch;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LockWatch implements Watcher {
    private ZooKeeper zooKeeper;
    private static final int SESSION_TIME_OUT = 2000;
    private CountDownLatch lockCountDownLatch;
    private String LOCKPATH;

    public LockWatch(){
        //new zookeeper
        try {
            zooKeeper = new ZooKeeper("zookeeper:2181", SESSION_TIME_OUT, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getType() == Event.EventType.NodeDeleted){
            System.out.println("the node deleted!");
            lockCountDownLatch.countDown();
        }
    }

    public boolean acquire(String lockPath, int timeout, TimeUnit timeUnit) throws Exception {
        LOCKPATH = lockPath;
        while (true){
            String sequential_id = createNode(lockPath, "lock", CreateMode.EPHEMERAL_SEQUENTIAL);
            List<String> childs = getChildren(lockPath);
            for(int i = 0; i < childs.size(); i++){
                if(i == 0){
                    if(childs.get(0).equals(sequential_id))
                        return true;
                }else{
                    if(this.zooKeeper.exists(childs.get(i), true) != null){
                        lockCountDownLatch = new CountDownLatch(1);
                        //超时放锁
                        if(!lockCountDownLatch.await(timeout, timeUnit))
                            return false;
                        break;
                    }
                    else if(childs.get(i).equals(sequential_id))
                        return true;
                }
            }
        }
    }

    public void release() throws KeeperException, InterruptedException {
        deleteNode(LOCKPATH);
    }

    public String createNode(String path,String data, CreateMode createMode) throws Exception{
        return this.zooKeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
    }

    public List<String> getChildren(String path) throws KeeperException, InterruptedException{
        List<String> children = zooKeeper.getChildren(path, false);
        return children;
    }

    public void deleteNode(String path) throws InterruptedException, KeeperException{
        this.zooKeeper.delete(path, -1);
    }
}