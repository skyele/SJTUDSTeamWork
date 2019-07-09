package com.sender;

import com.google.gson.Gson;
import com.sender.Generator.InitiatorGenerate;
import com.sender.Generator.ItemGenerate;
import com.sender.Generator.OrderGenerate;
import com.sender.Generator.UserGenerate;
import com.sender.pojo.Item;
import com.sender.pojo.Order;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.*;

@SpringBootApplication
public class SenderApplication {
	//-uid -iid -time
	private static int userIdRange;

	private static int itemIdRange;

	private static int timeInterval;

	private static String urlPort;

	public static void main(String[] args) throws InterruptedException, IOException {
		SpringApplication.run(SenderApplication.class, args);
		urlPort = args[3];
		while(true){
			Thread.sleep(Integer.parseInt(args[2]));
			ItemGenerate itemGenerate = new ItemGenerate(Integer.parseInt(args[1]));
			UserGenerate userGenerate = new UserGenerate(Integer.parseInt(args[0]));
			Order order = new Order();
			order.setInitiator(new InitiatorGenerate().getCurrency());
			order.setTime(new Date().getTime());
			order.setUser_id(userGenerate.getUser_id());
			int loop = new Random().nextInt(4)+1;
			for(int j = 0; j < loop; j++){
				Item item = new Item(itemGenerate.getItem_id(), itemGenerate.getNumber());
				order.getItems().add(item);
			}
			requestByPostMethod(order);
		}
	}

	public static void requestByPostMethod(Order object) throws IOException {
		System.out.println("in requestByPostMethod");
		String url = "http://" + urlPort + "/request";
		System.out.println("the url: " + url);
//		String encoderJson = URLEncoder.encode(new Gson().toJson(object), String.valueOf(StandardCharsets.UTF_8));
		System.out.println("json object: " + new Gson().toJson(object));
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");//表示客户端发送给服务器端的数据格式
		httpPost.setHeader("Accept", "application/json");                    //表示服务端接口要返回给客户端的数据格式，

		StringEntity se = new StringEntity(new Gson().toJson(object));
//		se.setContentType("text/json");
//		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		httpPost.setEntity(se);
//		httpPost.setEntity(entity);
		client.execute(httpPost);
	}

	private static CloseableHttpClient getHttpClient(){
		return HttpClients.createDefault();
	}
	private static void closeHttpClient(CloseableHttpClient client) throws IOException{
		if (client != null){
			client.close();
		}
	}
}
