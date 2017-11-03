import java.util.HashMap

import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.{ Seconds, StreamingContext }

import org.apache.spark.{ SparkContext, SparkConf }

/**
  * A Spark Streaming - Kafka integration to receive twitter
  * data from kafka topic and find the word counts
  *
  * Arguments: <zkQuorum> <consumer-group> <topics> <numThreads>
  * <zkQuorum>       - The zookeeper hostname
  * <consumer-group> - The Kafka consumer group
  * <topics>         - The kafka topic to subscribe to
  * <numThreads>     - Number of kafka receivers to run in parallel
  *
  *
  */

object KafkaSparkTwitterConsumer {

  System.setProperty("hadoop.home.dir", "C:/hadoop")

  val conf = new SparkConf().setMaster("local[6]").setAppName("Spark Streaming - Kafka Producer - PopularHashTags").set("spark.executor.memory", "1g")
  val sc = new SparkContext(conf)
  @throws[Exception]
  def main(args: Array[String]) :Unit =  {

    sc.setLogLevel("WARN")

    // Create an array of arguments: zookeeper hostname/ip,consumer group, topicname, num of threads
    val Array(zkQuorum, group, topics, numThreads) = args

    // Set the Spark StreamingContext to create a DStream for every 2 seconds
    val ssc = new StreamingContext(sc, Seconds(2))
//    ssc.checkpoint("checkpoint")

    // Map each topic to a thread
    val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap
    // Map value from the kafka message (k, v) pair
    val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)
    val words = lines.flatMap(_.split(" "))

//    // Get the top words over the previous 60/10 sec window
    val topCounts60 = words.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(60))
      .map { case (topic, count) => (count, topic) }
      .transform(_.sortByKey(false))


    topCounts60.foreachRDD(rdd => {
      val topList = rdd.take(20)
      println("\nPopular words in last 60 seconds (%s total):".format(rdd.count()))
      topList.foreach { case (count, tag) => println("%s (%s counts)".format(tag, count)) }
    })

//
//    lines.count().map(cnt => "Received " + cnt + " kafka messages.").print()
//
    ssc.start()
    ssc.awaitTermination()
  }
}