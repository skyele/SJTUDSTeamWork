package com.server;

import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServerApplication {

	@Bean
	public static final Zk zkHandler(){
		Zk zk = new Zk();
		try{
			zk.connectZookeeper("zookeeper:2181");
		}catch (Exception e) {

		}
		return zk;
	}

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}
