package com.sender;

import com.google.gson.Gson;
import com.sender.Generator.InitiatorGenerate;
import com.sender.Generator.ItemGenerate;
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
		ItemGenerate itemGenerate = new ItemGenerate(Integer.parseInt(args[1]));
		UserGenerate userGenerate = new UserGenerate(Integer.parseInt(args[0]));
		for(int i = 0; i < 3; i++){
			new Thread(){
				public void run(){
					while(true){
						try {
							Thread.sleep(Integer.parseInt(args[2]));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Order order = new Order();
						order.setInitiator(new InitiatorGenerate().getCurrency());
						order.setTime(new Date().getTime());
						order.setUser_id(userGenerate.getUser_id());
						int loop = new Random().nextInt(4)+1;
						Map<Integer, Integer> map = new LinkedHashMap<>();
						while(loop != 0){
							Item item = new Item(itemGenerate.getItem_id(), itemGenerate.getNumber());
							System.out.println("the item id in new! is : " + item.getId());
							if(!map.containsKey(item.getId())||map.get(item.getId())==0){
								map.put(item.getId(), 1);
								order.getItems().add(item);
								loop--;
							}
						}
						printList(order.getItems());
						try {
							requestByPostMethod(order);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
	}

	public static void requestByPostMethod(Order object) throws IOException {
		String url = "http://" + urlPort + "/request";
		System.out.println("the url: " + url);
		System.out.println("json object: " + new Gson().toJson(object));
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");//表示客户端发送给服务器端的数据格式
		httpPost.setHeader("Accept", "application/json");                    //表示服务端接口要返回给客户端的数据格式，

		StringEntity se = new StringEntity(new Gson().toJson(object));
		httpPost.setEntity(se);
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
	private static void printList(List<Item> items){
		for(int i = 0; i < items.size();i++){
			System.out.println("the item id: " + items.get(i).getId());
		}
	}
}
