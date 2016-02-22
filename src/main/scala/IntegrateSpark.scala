package IntegrateSpark

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd._
import org.apache.spark.util.IntParam
import org.apache.spark.storage.StorageLevel
import java.io._
import java.util.Date
import org.apache.spark.HashPartitioner
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import scala.sys._
/**
 * Use DataFrames and SQL to count words in UTF8 encoded, '\n' delimited text received from the
 * network every second.
 *
 * Usage: SqlNetworkWordCount <hostname> <port>
 * <hostname> and <port> describe the TCP server that Spark Streaming would connect to receive data.
 *
 * To run this on your local machine, you need to first run a Netcat server
 *    `$ nc -lk 9999`
 * and then run the example
 *    `$ bin/run-example org.apache.spark.examples.streaming.SqlNetworkWordCount localhost 9999`
 */

object Query {
  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println("Usage: IntegrateSpark <hostname> <port> <textFile>")
      System.exit(1)
    }
    

    // Create the context with a 2 second batch size
    val sparkConf = new SparkConf().setAppName("IntegrateSpark")
    sparkConf.set("spark.driver.allowMultipleContexts", "true")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    ssc.checkpoint(".")
    
    println("*************************")
    // Create a socket stream on target ip:port and count the
    // words in input stream of \n delimited text (eg. generated by 'nc')
    // Note that no duplication in storage level only for running locally.
    // Replication necessary in distributed scenario for fault tolerance.
    
    val initialRDD = ssc.sparkContext.parallelize(List(("hello", List(new Data("rdd",new Date().toString(),"hello test")))))
    
    val lines = ssc.socketTextStream("localhost", 9999, StorageLevel.MEMORY_AND_DISK_SER)
    //val lines2 = ssc.socketTextStream("localhost", 9998, StorageLevel.MEMORY_AND_DISK_SER)
    //val words1 = lines.flatMap(_.split(" "))
    
    val streamRecords = lines.map(x => (x.split(" ")(0), new Data("rdd",new Date().toString(),x)))
    //val streamwordCounts2 = words.map(x => (x, new Date().toString()))
    val mappingFunc = (word: String, name: Option[Data], state: State[List[Data]]) => {
      val list = name.getOrElse(new Data("",new Date().toString(),"")) :: state.getOption.getOrElse(Nil)
      val output = (word, list)
      state.update(list)
      output
    }
    sys.ShutdownHookThread {
      ssc.stop(true, true)
    }
    val stateDstream = streamRecords.mapWithState(
      StateSpec.function(mappingFunc).initialState(initialRDD))

    stateDstream.foreachRDD(rdd => {
        var qkey = readLine("Query key: ")
            rdd.foreach{
                case(key,data)=>{
                    if(key==qkey)
                    println(data)
                }
            }
        })
    
/**

Create a new Class Data, Metadata.
foreachRDD to get query in and output result.

**/


    ssc.start()
    ssc.awaitTermination(Minutes(300).milliseconds)
    ssc.awaitTermination()
    ssc.stop(true)
  }
}

