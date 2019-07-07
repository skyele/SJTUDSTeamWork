package com.server;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class Watch implements Watcher {
    private static String PATH = "/ExchangeRate/";

    @Override
    public void process(WatchedEvent event) {

        System.out.println("==========DataWatcher start==============");

        System.out.println("DataWatcher state: " + event.getState().name());

        System.out.println("DataWatcher type: " + event.getType().name());

        System.out.println("DataWatcher path: " + event.getPath());

        String[] paths = event.getPath().split("/");
        String currency = paths[paths.length-1];
        //发消息 更新最新的currency rate
        //TODO
    }
}
