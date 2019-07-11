package com.server.Watch;

import com.google.common.base.Strings;
import com.sun.org.apache.bcel.internal.generic.LCONST;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

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
        System.out.println("recheck path: " + "/"+path.split("/")[0]+"/"+path.split("/")[1]);
        printChild("/"+path.split("/")[0]+"/"+path.split("/")[1]);
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

    private void printChild(String path) throws KeeperException, InterruptedException {
        List<String> childs = getChildren(path);
        for(int i = 0; i < childs.size(); i++){
            System.out.println("child["+i+"]: " + childs.get(i));
        }
    }

    private void printList(List<String> strings){
        for(int i = 0; i < strings.size(); i++)
            System.out.println("child["+i+"]: " + strings.get(i));
    }

    private void ensureRootPath(String rootPath){
        try {
            if (zooKeeper.exists(rootPath,true)==null){
                zooKeeper.create(rootPath,"".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取锁
     * @return
     * @throws InterruptedException
     */
    public boolean acquire(String rootPath, int timeout, TimeUnit timeUnit){
        ensureRootPath(rootPath);
        try {
            String sequential_id = zooKeeper.create(rootPath+"/lock", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOCKPATH = sequential_id;
            List<String> minPath = zooKeeper.getChildren(rootPath,false);
            System.out.println(minPath);
            Collections.sort(minPath);
            printList(minPath);
            System.out.println(minPath.get(0)+" and path "+sequential_id);
            if (!Strings.nullToEmpty(sequential_id).trim().isEmpty()&&!Strings.nullToEmpty(minPath.get(0)).trim().isEmpty()&&sequential_id.equals(rootPath+"/"+minPath.get(0))) {
                System.out.println(sequential_id + "  get Lock...");
                return true;
            }
            String watchNode = null;
            for (int i=minPath.size()-1;i>=0;i--){
                if(minPath.get(i).compareTo(sequential_id.substring(sequential_id.lastIndexOf("/") + 1))<0){
                    watchNode = minPath.get(i);
                    break;
                }
            }

            if (watchNode!=null){
                final String watchNodeTmp = watchNode;
                final Thread thread = Thread.currentThread();
                Stat stat = zooKeeper.exists(rootPath + "/" + watchNodeTmp,new Watcher() {
                    @Override
                    public void process(WatchedEvent watchedEvent) {
                        if(watchedEvent.getType() == Event.EventType.NodeDeleted){
                            lockCountDownLatch.countDown();
                        }
                        try {
                            zooKeeper.exists(rootPath + "/" + watchNodeTmp,true);
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                if(stat != null){
                    System.out.println(sequential_id + " waiting for " + rootPath + "/" + watchNode);
                }
                lockCountDownLatch = new CountDownLatch(1);
            }
            lockCountDownLatch.await();
            System.out.println(sequential_id + "wait successfully get lock!!!!!!!!!!!!!!!!!!\n");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(LOCKPATH + "not get lock1!!!");
        return false;
    }

    /**
     * 释放锁
     */
    public void release(){
        try {
            System.out.println(LOCKPATH +  "release Lock...");
            zooKeeper.delete(LOCKPATH,-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}