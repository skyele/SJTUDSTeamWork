package com.spark.mysql.repo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.Item;
import com.spark.ResultController;
import com.spark.SparkApplication;
import com.spark.mysql.pojo.Result;
import consumer.kafka.MessageAndMetadata;
import consumer.kafka.ProcessedOffsetManager;
import consumer.kafka.ReceiverLauncher;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

@Service
public class SparkService {

    @Autowired
    private ResultController resultController;

    public void foreachRDD(){
        Properties props = new Properties();
        props.put("zookeeper.hosts", "zookeeper");
        props.put("zookeeper.port", "2181");
        props.put("kafka.topic", "orders");
        props.put("kafka.consumer.id", "spark");
        props.put("bootstrap.servers", "kafka:9092");
// Optional Properties
        props.put("max.poll.records", "250");
        props.put("consumer.fillfreqms", "1000");

        SparkConf _sparkConf = new SparkConf().setMaster("local[2]").setAppName("ds");

        JavaStreamingContext jsc = new JavaStreamingContext(_sparkConf, Durations.seconds(30));
// Specify number of Receivers you need.
        int numberOfReceivers = 3;

        JavaDStream<MessageAndMetadata<byte[]>> unionStreams = ReceiverLauncher.launch(
                jsc, props, numberOfReceivers, StorageLevel.MEMORY_ONLY());

//Get the Max offset from each RDD Partitions. Each RDD Partition belongs to One Kafka Partition
        JavaPairDStream<Integer, Iterable<Long>> partitonOffset = ProcessedOffsetManager
                .getPartitionOffset(unionStreams, props);

//Start Application Logic
        unionStreams.foreachRDD(new VoidFunction<JavaRDD<MessageAndMetadata<byte[]>>>() {
            @Override
            public void call(JavaRDD<MessageAndMetadata<byte[]>> rdd) throws Exception {
                rdd.foreachPartition(new VoidFunction<Iterator<MessageAndMetadata<byte[]>>>() {

                    @Override
                    public void call(Iterator<MessageAndMetadata<byte[]>> mmItr) throws Exception {
                        System.out.println("走到这里了");
                        while(mmItr.hasNext()) {
                            MessageAndMetadata<byte[]> mm = mmItr.next();
                            System.out.println(" My topic:" + mm.getTopic() + " My content:" + new String(mm.getPayload()));
                            JSONObject json = JSON.parseObject(new String(mm.getPayload()));
                            Integer id = json.getInteger("id");
                            Integer userid = Integer.parseInt(new String(mm.getKey()));
                            String initiator = json.getString("initiator");
                            Boolean success = json.getBoolean("success");
                            List<Item> items = JSON.parseArray(json.getJSONArray("items").toString(),Item.class);
                            Double paid = 0.0;
                            if (success){
                                for (int i = 0;i<items.size();i++) {
                                    paid += items.get(i).getNumber() * items.get(i).getPrice();
                                }
                            }
                            Result res = new Result(id, userid, initiator, success, paid);
                            resultController.saveResult(res);
                        }
                    }
                });
            }
        });

//End Application Logic

//Persists the Max Offset of given Kafka Partition to ZK
        ProcessedOffsetManager.persists(partitonOffset, props);

        try {
            jsc.start();
            jsc.awaitTermination();
        }catch (Exception ex ) {
            jsc.ssc().sc().cancelAllJobs();
            jsc.stop(true, false);
            System.exit(-1);
        }
    }

}
