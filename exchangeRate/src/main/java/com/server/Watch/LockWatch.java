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
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private String LOCKPATH;

    public LockWatch(){
        //new zookeeper
        try {
            zooKeeper = new ZooKeeper("zookeeper:2181", SESSION_TIME_OUT, this);
            connectedSemaphore.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println("the event is: " + watchedEvent.getType()+ " state: " + watchedEvent.getState());

        System.out.println("\nthe path is " + watchedEvent.getPath()+"\n");
        if ( Event.KeeperState.SyncConnected == watchedEvent.getState() ) {
            connectedSemaphore.countDown();
        }
        if(watchedEvent.getType() == Event.EventType.NodeDeleted){
            System.out.println("the node deleted!");
            lockCountDownLatch.countDown();
        }
    }

    public boolean acquire(String lockPath, int timeout, TimeUnit timeUnit) throws Exception {
        System.out.println("want to acquire " + lockPath);
        String sequential_id = createNode(lockPath + "/lock", "lock", CreateMode.EPHEMERAL_SEQUENTIAL);
        LOCKPATH = sequential_id;
        System.out.println("the sqe_id: " + sequential_id);
        while (true){
            System.out.println("in while in acquire!");
            List<String> childs = getChildren(lockPath);
            System.out.println("we get childs! the size: " + childs.size());

            for(int i = 0; i < childs.size();i++){
                System.out.println("child["+i+"]: "+childs.get(i));
            }

            for(int i = 0; i < childs.size(); i++){
                System.out.println("the childs["+i+"]: " + childs.get(i));
                if(i == 0){
                    if(sequential_id.contains(childs.get(0)))
                        System.out.println("acquire! 0");
                    return true;
                }else{
                    if(this.zooKeeper.exists(childs.get(i), true) != null){
                        lockCountDownLatch = new CountDownLatch(1);
                        //超时放锁
                        if(!lockCountDownLatch.await(timeout, timeUnit)){
                            System.out.println("sry timeout!");
                            deleteNode(LOCKPATH);
                            return false;
                        }
                        break;
                    }
                    else if(sequential_id.contains(childs.get(i))){
                        System.out.println("acquire! "+i);
                        return true;
                    }
                }
            }
            Thread.sleep(2000);
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
        System.out.println("\ndelete Node: " + path+"!!\n");
        this.zooKeeper.delete(path, -1);
    }

    public void closeConnection() throws InterruptedException{
        if (zooKeeper != null) {
            System.out.println("closeConnection!");
            zooKeeper.close();
            zooKeeper = null;
        }
    }

    protected void finalize() throws InterruptedException {
        if (zooKeeper != null){
            System.out.println("in finalize close Connection!");
            zooKeeper.close();
            zooKeeper = null;
        }
    }
}