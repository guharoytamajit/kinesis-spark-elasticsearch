package com.tamajit;
import org.apache.commons.collections.bag.SynchronizedSortedBag;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kinesis.KinesisUtils;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
private static final String ENDPOINT = "kinesis.us-east-1.amazonaws.com";

public static void main(String[] args) throws IOException, InterruptedException {
	 Logger.getLogger("org").setLevel(Level.ERROR);
     SparkConf conf = new SparkConf().setAppName("count")
    		 .setMaster("local[*]")
    		 .set("es.index.auto.create","true")
    		 .set("es.nodes","search-test-es-jn2i3cv2sv6stdrrmcw4fcc6lq.us-east-1.es.amazonaws.com")
    		 .set("es.port","80")
//    		 .set("spark.es.nodes.client.only","true")
    		 .set("spark.es.nodes.wan.only","true")
//    		 .set("es.nodes.discovery","false")
    		 ;
     JavaSparkContext sc = new JavaSparkContext(conf);
     
     
     JavaStreamingContext streamingContext = new JavaStreamingContext(sc, Durations.seconds(5));
     
     
/*     AWSCredentialsProvider credentialsProvider = null;
     try {
         credentialsProvider = new ProfileCredentialsProvider("default");
     } catch (Exception e) {
         throw new AmazonClientException(
                 "Cannot load the credentials from the credential profiles file. " +
                 "Please make sure that your credentials file is at the correct " +
                 "location (~/.aws/credentials), and is in valid format.",
                 e);
     }*/
     
     AmazonKinesisClient kinesisClient =
    	        new AmazonKinesisClient(new DefaultAWSCredentialsProviderChain());
    	kinesisClient.setEndpoint(ENDPOINT);
     
// KinesisUtils.createStream(
     JavaReceiverInputDStream<byte[]> kinesisStream = KinesisUtils.createStream(
    	     streamingContext,"StockTradeStream7", "test-kinesis", ENDPOINT,
    	     "us-east-1", InitialPositionInStream.LATEST, Durations.seconds(5), StorageLevel.MEMORY_AND_DISK_2());

/*     List<String> inputWords = Arrays.asList("spark", "hadoop", "spark", "hive", "pig", "cassandra", "hadoop");
     JavaRDD<String> wordRdd = sc.parallelize(inputWords);

     System.out.println("Count: " + wordRdd.count());

     Map<String, Long> wordCountByValue = wordRdd.countByValue();

     System.out.println("CountByValue:");

     for (Map.Entry<String, Long> entry : wordCountByValue.entrySet()) {
         System.out.println(entry.getKey() + " : " + entry.getValue());
     }*/
     
/*     String json2 = "{\"participants\" : 6,\"airport\" : \"OTP\"}";
     JavaRDD<String> stringRDD =sc.parallelize(Arrays.asList(json2));
     JavaEsSpark.saveJsonToEs(stringRDD, "spark/json-trips"); */

     kinesisStream.foreachRDD(rdd->{
    	 List<byte[]> list = rdd.collect();
    	 list.stream().forEach(x->{
    		System.out.println(new String(x)); 
    	 });
    	JavaRDD<String> newRDD = rdd.map(b->new String(b));
    	 JavaEsSpark.saveJsonToEs(newRDD, "spark/json-trips");
     });
     
     streamingContext.start();
     System.out.println("Running...");
     streamingContext.awaitTermination();
//     credentialsProvider.c
}
}
