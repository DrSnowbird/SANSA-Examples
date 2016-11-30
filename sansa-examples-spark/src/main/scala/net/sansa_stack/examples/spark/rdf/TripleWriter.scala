package net.sansa_stack.examples.spark.rdf

import java.io.File
import scala.collection.mutable
import org.apache.spark.sql.SparkSession
import net.sansa_stack.rdf.spark.model.JenaSparkRDDOps

object TripleWriter {

  def main(args: Array[String]) = {
    if (args.length < 2) {
      System.err.println(
        "Usage: Triple writer <input> <output>")
      System.exit(1)
    }
    val input = args(0)
    val output = args(1)
    val optionsList = args.drop(2).map { arg =>
      arg.dropWhile(_ == '-').split('=') match {
        case Array(opt, v) => (opt -> v)
        case _             => throw new IllegalArgumentException("Invalid argument: " + arg)
      }
    }
    val options = mutable.Map(optionsList: _*)

    options.foreach {
      case (opt, _) => throw new IllegalArgumentException("Invalid option: " + opt)
    }
    println("======================================")
    println("|        Triple writer example       |")
    println("======================================")

    val sparkSession = SparkSession.builder
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .appName("Triple writer example (" + input + ")")
      .getOrCreate()

    val ops = JenaSparkRDDOps(sparkSession.sparkContext)
    import ops._

    val it = sparkSession.sparkContext.textFile(input).collect.mkString("\n")

    val triples = fromNTriples(it, "http://dbpedia.org").toSeq

    val triplesw = toNTriples(triples.toIterable).split("\n").toSeq
    val triplesRDD = sparkSession.sparkContext.parallelize(triplesw)

    triplesRDD.saveAsTextFile(output)

    sparkSession.stop

  }

}