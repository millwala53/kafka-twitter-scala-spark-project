# kafka-twitter-scala-spark-project
A scala-maven based project using kafka, spark and twitter.  
A kakfa producer is sendind a stream of twitter messages ( using twitter4j library ) to kafka broker. 
A spark consumer is consuming the messages from kafka and displaying a word count of the most used word over a period of 60 secs and printign those words with their count. 
