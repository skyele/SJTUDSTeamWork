package com.sender;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
import org.omg.CORBA.NameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
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
			String orderString = order.getJSONOrder();
			requestByPostMethod(orderString);
		}
	}

	public static void requestByPostMethod(String orderString) throws IOException {
		System.out.println("in requestByPostMethod");
		String url = "http://" + urlPort + "/request";
//		String url = urlPort + "/request";
		System.out.println("the url: " + url);
		String encoderJson = URLEncoder.encode(orderString, String.valueOf(StandardCharsets.UTF_8));

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");

		StringEntity se = new StringEntity(encoderJson);
		se.setContentType("text/json");
		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
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
}
