package com.sender;

import com.google.gson.Gson;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.http.protocol.HTTP.USER_AGENT;

@SpringBootApplication
public class SenderApplication {
	//-uid -iid -time
	private static int userIdRange;

	private static int itemIdRange;

	private static int timeInterval;

	private static String urlPort;

	public static void main(String[] args) throws InterruptedException, IOException {
		SpringApplication.run(SenderApplication.class, args);
		Item item = new Item(Integer.parseInt(args[1]));
		User user = new User(Integer.parseInt(args[0]));
		Order order = new Order(user, item);
		urlPort = args[3];
		while(true){
			Thread.sleep(Integer.parseInt(args[2]));
//			String orderString = order.getJSONOrder();
			Map<String, Order> object = new HashMap<>();
			object.put("order", order);
			requestByPostMethod(object);
		}
	}

	public static void requestByPostMethod(Map<String, Order> object) throws IOException {
		System.out.println("in requestByPostMethod");
		String url = "http://" + urlPort + "/request";
		System.out.println("the url: " + url);
//		String encoderJson = URLEncoder.encode(new Gson().toJson(object), String.valueOf(StandardCharsets.UTF_8));

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		for (Map.Entry<String, Order> entry : object.entrySet()) {
			formparams.add(new BasicNameValuePair(entry.getKey(), new Gson().toJson(entry.getValue())));
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, StandardCharsets.UTF_8);
//		HttpPost httppost = new HttpPost(url);
//		httppost.setEntity(entity);



//		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, StandardCharsets.UTF_8);

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");

//		StringEntity se = new StringEntity(encoderJson);
//		se.setContentType("text/json");
//		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//		httpPost.setEntity(se);
		httpPost.setEntity(entity);
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
