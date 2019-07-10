package com.spark;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SparkApplicationTests {
	@Autowired
	SparkService sparkService;
	@Test
	public void contextLoads() {
		sparkService.foreachRDD();
	}

}
