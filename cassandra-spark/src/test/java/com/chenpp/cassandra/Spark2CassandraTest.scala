package com.chenpp.cassandra

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * @author April.Chen
 * @date 2023/10/23 2:57 下午
 * */
object Spark2CassandraTest {


  def buildSparkSession(): SparkSession = {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("Java Spark SQL basic example")
      .master("local[*]")
      .config("spark.cassandra.connection.host", "10.58.12.60")
//      .config("spark.cassandra.auth.username", "busuanzi")
//      .config("spark.cassandra.auth.password", "busuanzi.org")
      .config("spark.cassandra.connection.port", "9042")
      .getOrCreate();

    spark
  }

  def write(df: DataFrame): Unit = {
    df.write
      .format("org.apache.spark.sql.cassandra")
      .option("keyspace", "busuanzi_org")
      .option("table", "top_n_url")
      .mode("append").save();
  }

  def read(): Unit = {
    val spark = buildSparkSession()
    val df: DataFrame = spark.read
      .format("org.apache.spark.sql.cassandra")
      .option("keyspace", "test")
      .option("table", "tutorial")
      .load();
    df.show();
  }

  def main(args: Array[String]): Unit = {
      read()
  }
}
