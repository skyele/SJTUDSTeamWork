package com.sender;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.omg.CORBA.NameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.methods.PostMethod;

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
		String url = "http://" + urlPort + "/request";
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		try {
			StringEntity s = new StringEntity(orderString);
			s.setContentEncoding("UTF-8");
			s.setContentType("application/json");//发送json数据需要设置contentType
			post.setEntity(s);
			HttpResponse res = client.execute(post);
			if(res.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				String result = EntityUtils.toString(res.getEntity());// 返回json格式：
				System.out.println("return: " + result);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
