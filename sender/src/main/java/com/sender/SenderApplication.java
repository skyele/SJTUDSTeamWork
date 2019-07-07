package com.sender;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.omg.CORBA.NameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class SenderApplication {
	//-uid -iid -time
	private static int userIdRange;

	private static int itemIdRange;

	private static int timeInterval;

	private static String urlPort;

	public static void main(String[] args) throws InterruptedException {
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

	public static void requestByPostMethod(String orderString){
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpPost post = new HttpPost("http://" + urlPort + "/request");          //这里用上本机的某个工程做测试
			//创建参数列表
			List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
			list.add(new BasicNameValuePair("order", orderString));
			//url格式编码
			UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(list,"UTF-8");
			post.setEntity(uefEntity);
			System.out.println("POST 请求...." + post.getURI());
			//执行请求
			CloseableHttpResponse httpResponse = httpClient.execute(post);
			try{
				HttpEntity entity = httpResponse.getEntity();
				if (null != entity){
					System.out.println("-------------------------------------------------------");
					System.out.println(EntityUtils.toString(uefEntity));
					System.out.println("-------------------------------------------------------");
				}
			} finally{
				httpResponse.close();
			}

		} catch( UnsupportedEncodingException e){
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try{
				closeHttpClient(httpClient);
			} catch(Exception e){
				e.printStackTrace();
			}
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
