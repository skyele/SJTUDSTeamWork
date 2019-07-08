package com.server;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServerApplication {
	private static Integer NUMBER = 4;
	private static String PATH = "/ExchangeRate/";

	@Bean
	public static final Zk zkHandler(){
		Zk zk = new Zk();
		try{
			zk.connectZookeeper("zookeeper:2181");
			//注册 watch
			for(int i = 0; i < NUMBER; i++){
				zk.getData(PATH + getCurrency(i), true);
			}
		}catch (Exception e) {

		}
		return zk;
	}

	public static void main(String[] args) throws KeeperException, InterruptedException {
		SpringApplication.run(ServerApplication.class, args);
		//注册watch
		for(int i = 0; i < NUMBER; i++){
			String currency = getCurrency(i);
			zkHandler().getData(PATH+currency, true);
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
