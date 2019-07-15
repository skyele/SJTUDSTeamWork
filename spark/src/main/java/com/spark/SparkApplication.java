package com.spark;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.mysql.pojo.Hibernate4Utils;
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
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class SparkApplication {

    public static Zk zk;
    public static Session session;
    public static Transaction transaction;

    public static void main(String[] args) {

        // 建立ZooKeeper 连接
        zk = new Zk();

        try {
            zk.connectZookeeper("zookeeper:2181");
            zk.createNode("/TotalTransactionAmount", String.valueOf(0.0));

        } catch (Exception e) {
            System.out.println("Zk failed while initializing in Spark");
            e.printStackTrace();
        }


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

        session = Hibernate4Utils.getCurrentSession();
        transaction = session.beginTransaction();

//Start Application Logic
        unionStreams.foreachRDD(new VoidFunction<JavaRDD<MessageAndMetadata<byte[]>>>() {

            @Override
            public void call(JavaRDD<MessageAndMetadata<byte[]>> rdd) throws Exception {
                rdd.foreachPartition(new VoidFunction<Iterator<MessageAndMetadata<byte[]>>>() {

                    @Override
                    public void call(Iterator<MessageAndMetadata<byte[]>> mmItr) throws Exception {
                        while (mmItr.hasNext()) {
                            MessageAndMetadata<byte[]> mm = mmItr.next();
                            System.out.println(" My topic:" + mm.getTopic() + " My content:" + new String(mm.getPayload()));
                            JSONObject json = JSON.parseObject(new String(mm.getPayload()));
                            Integer id = json.getInteger("id");
                            Integer userid = Integer.parseInt(new String(mm.getKey()));
                            String initiator = json.getString("initiator");
                            Boolean success = json.getBoolean("success");
                            List<Item> items = JSON.parseArray(json.getJSONArray("items").toString(), Item.class);
                            Double paid = 0.0;
                            if (success) {
                                for (int i = 0; i < items.size(); i++) {
                                    paid += items.get(i).getNumber() * items.get(i).getPrice();
                                }
                            }
                            Result res = new Result(id, userid, initiator, success, paid);

                            Serializable resid = session.save(res);

                            System.out.println("My id:" +session.get(Result.class, resid));

                            System.out.println("success is:" + success.toString());

                            // 将 Total transaction 保存到Zookeeper里面

                            if (success) {
                                String totalTransactionAmountString = zk.getData("/TotalTransactionAmount", false);
                                double totalTransactionAmount = Double.valueOf(totalTransactionAmountString);
                                totalTransactionAmount += paid;
                                zk.setData("/TotalTransactionAmount", String.valueOf(totalTransactionAmount));
                                totalTransactionAmountString = zk.getData("/TotalTransactionAmount", false);
                                System.out.println("new totalTransactionAmount is" + totalTransactionAmountString);
                            }


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
            transaction.commit();
            Hibernate4Utils.closeCurrentSession();
        } catch (Exception ex) {
            jsc.ssc().sc().cancelAllJobs();
            jsc.stop(true, false);
            System.exit(-1);
        }
    }
}
