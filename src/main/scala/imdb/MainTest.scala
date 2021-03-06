package imdb

import com.github.nscala_time.time.Imports._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.reflect.ClassTag

//import org.apache.spark.sql.SparkSession
//import org.apache.log4j.Logger
//import org.apache.log4j.Level
//import lsh._
import com.github.tototoshi.csv._

import java.io.File
import sys.process._



object MainTest {

  def main(args: Array[String]) {
    val numPartitions = 16

//    True
//    val stream = List("q1", "q2", "q3", "q4", "q5", "q6", "q7", "q8")
//   stream.map(Runner.process(_))
//    println("achieved")

    //Test single
//    val stream = List("q9", "q10", "q11")
//    stream.foreach(Runner.process(_))
//    println("achieved")

    //test parallel
    val q_list = List("q1", "q2", "q4", "q9", "q10")
    (0 until q_list.length).foreach {
      i =>
        (i until q_list.length).foreach { j =>
          Runner.processParallel(q_list(i), q_list(j))
        }
    }
    println("achieved")





//    val d = new DecisionTree(stream)
//    //random proba
//    val probas = d.getProbas()
//
//    var command = "python src/main/python/schedule_optimizer.py"
//    command = command + " --queries " + toPythonListString(stream)
//    command = command + " --num_partitions 16"
//    command = command + " --probas " + toPythonListString(probas)
//    println(command)
//    //get result of execution
//    val test2:String = command.!!
//
//    val groups = test2.split(';').toList
//    val res = groups.map(x=> x.split(';').toList)
//    println(res)






  }
}
