import java.util
import java.util.Properties
import java.util.concurrent.LinkedBlockingQueue

import twitter4j._
import twitter4j.conf._
import twitter4j.StallWarning
import twitter4j.Status
import twitter4j.StatusDeletionNotice
import twitter4j.StatusListener
import twitter4j.TwitterStream
import twitter4j.TwitterStreamFactory
import twitter4j.conf.ConfigurationBuilder
import twitter4j.json.DataObjectFactory
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import kafka.producer.{KeyedMessage, ProducerConfig}

import scala.Predef._
import scala.util.Random



object KafkaTwitterProducer {
  @throws[Exception]
  def main(args: Array[String]): Unit = {
    val queue = new LinkedBlockingQueue[Status](100000)
    if (args.length < 4) {
      System.out.println("Usage: KafkaTwitterProducer <twitter-consumer-key> <twitter-consumer-secret> <twitter-access-token> <twitter-access-token-secret> <topic-name> <twitter-search-keywords>")
      return
    }
    val consumerKey = args(0).toString
    val consumerSecret = args(1).toString
    val accessToken = args(2).toString
    val accessTokenSecret = args(3).toString
    val topicName = args(4).toString
    val arguments = args.clone
    val keyWords = new Array[String](3)
    keyWords(0) = "Pakistan"
    keyWords(1) = "human"
    val keywordsstring = "pakistan,human"
//    ("human", "pakistan", "party")
    //        String[] keyWords = "human", "abc";
    // Set twitter oAuth tokens in the configuration
    val cb = new ConfigurationBuilder
    cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret).setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret)
    // Create twitterstream using the configuration
    val twitterStream = new TwitterStreamFactory(cb.build).getInstance
    val listener = new StatusListener() {
      override def onStatus(status: Status): Unit = queue.offer(status)

      override

      def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId)

      override

      def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = System.out.println("Got track limitation notice:" + numberOfLimitedStatuses)

      override

      def onScrubGeo(userId: Long, upToStatusId: Long): Unit = System.out.println("Got scrub_geo event userId:" + userId + "upToStatusId:" + upToStatusId)

      override

      def onStallWarning(warning: StallWarning): Unit = System.out.println("Got stall warning:" + warning)

      override

      def onException(ex: Exception): Unit = ex.printStackTrace()
    }
    // Filter keywords
    val query = new FilterQuery().track(keywordsstring)
    twitterStream.addListener(listener)
    twitterStream.filter(query)
    // Thread.sleep(5000);
    // Add Kafka producer config settings
    val props = new Properties
//    props.put("metadata.broker.list", "localhost:9092")
    props.put("bootstrap.servers", "localhost:9092")
    props.put("client.id", "ScalaProducerExample")
    //    props.put("acks", "all")
//    props.put("retries", int2Integer(0))
//    props.put("batch.size", int2Integer(16384))
//    props.put("linger.ms", int2Integer(1))
//    props.put("buffer.memory", int2Integer(33554432))
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    val i = 0
    var j = 0
    val rnd = new Random()
    // poll for new tweets in the queue. If new tweets are added, send them
    // to the topic
    while ( {
      true
    }) {
      val ret = queue.poll
      if (ret == null) {
        Thread.sleep(100)
        // i++;
      }
      else{
        val ip = "192.168.2." + rnd.nextInt(255)
        System.out.println(topicName + ": " + ret.getText)
        producer.send(new ProducerRecord[String, String]("tweets",ip, ret.getText))
      }
      //      else for (hashtage <- ret.getHashtagEntities) {
      //        System.out.println("Hashtag: " + hashtage.getText)
      //        // producer.send(new ProducerRecord<String, String>(
      //        // topicName, Integer.toString(j++), hashtage.getText()));
//      }
    }
    // producer.close();
    // Thread.sleep(500);
    // twitterStream.shutdown();
  }
}