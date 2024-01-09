//package com.chenpp.cassandra
//
//import com.datastax.spark.connector.cql.{ColumnDef, RegularColumn, TableDef}
//import com.datastax.spark.connector.types.IntType
//import com.datastax.spark.connector.writer.{TimestampOption, WriteConf}
//import org.apache.spark.sql.SparkSession
//import org.apache.spark.sql.functions.col
//
//
//class Spark2Cassandra {
//  def connect()(implicit spark: SparkSession): Unit ={
//    //Catalog Cass100 for Cluster at 127.0.0.100
//    spark.conf.set(s"spark.sql.catalog.cass100", "com.datastax.spark.connector.datasource.CassandraCatalog")
//    spark.conf.set(s"spark.sql.catalog.cass100.spark.cassandra.connection.host", "127.0.0.100")
//
//    //Catalog Cass200 for Cluster at 127.0.0.200
//    spark.conf.set(s"spark.sql.catalog.cass200", "com.datastax.spark.connector.datasource.CassandraCatalog")
//    spark.conf.set(s"spark.sql.catalog.cass200.spark.cassandra.connection.host", "127.0.0.200")
//
//    spark.sql("INSERT INTO cass200.ks.tab SELECT * from cass100.ks.tab")
//    //Or
//    spark.read.table("cass100.ks.tab").writeTo("cass200.ks.tab").append
//  }
//
//  def create()(implicit spark: SparkSession): Unit = {
//    spark.sql("CREATE DATABASE IF NOT EXISTS mycatalog.testks WITH DBPROPERTIES (class='SimpleStrategy',replication_factor='1')")
//    spark.sql("CREATE TABLE mycatalog.testks.testtab (key Int, value STRING) USING cassandra PARTITIONED BY (key)")
//
//    //List their contents
//    spark.sql("SHOW NAMESPACES FROM mycatalog").show
//    spark.sql("SHOW TABLES FROM mycatalog.testks").show
//
//    //Loads implicit functions
//    import com.datastax.spark.connector._
//
//    val sc = spark.sparkContext
//
//    val rdd = spark.sparkContext.cassandraTable("test", "words")
//    rdd.foreach(row => println(row))
//
//    val emptyRDD = rdd.toEmptyCassandraRDD
//
//    val emptyRDD2 = sc.emptyCassandraTable[String]("ks", "not_existing_table")
//
//    val firstRow = rdd.first
////    firstRow.columnNames    // Stream(word, count)
//    firstRow.size           // 2
//    firstRow.getInt("count")       // 20
//    firstRow.getLong("count")      // 20L
//  }
//
//  def read()(implicit spark: SparkSession): Unit = {
//    val df = spark.read.table("mycatalog.testks.testtab")
//    println(df.count)
//    df.show
//    spark.sql("SELECT * FROM mycatalog.testks.testtab").show
//  }
//
//  def write()(implicit spark: SparkSession): Unit = {
//    spark
//      .range(1, 10)
//      .withColumnRenamed("id", "key")
//      .withColumn("value", col("key").cast("string"))
//      .writeTo("mycatalog.testks.testtab")
//      .append
//  }
//
//  def save()(implicit spark: SparkSession): Unit ={
//    import com.datastax.spark.connector._
//    val sc = spark.sparkContext
//
//    val collection = sc.parallelize(Seq(("cat", 30), ("fox", 40)))
//    collection.saveToCassandra("test", "words", SomeColumns("word", "count"))
//
//    val collection1 = sc.parallelize(Seq((30, "cat"), (40, "fox")))
//    collection1.saveToCassandra("test", "words", SomeColumns("word" as "_2", "count" as "_1"))
//
//    val collection2 = sc.parallelize(Seq(WordCount("dog", 50), WordCount("cow", 60)))
//    collection2.saveToCassandra("test", "words", SomeColumns("word", "count"))
//
//    collection2.saveToCassandra("test", "words2", SomeColumns("word", "num" as "count"))
//
//    collection2.saveAsCassandraTable("test", "words_new", SomeColumns("word", "count"))
//
//    val listElements = sc.parallelize(Seq(
//      (1,Vector("One")),
//      (1,Vector("Two")),
//      (1,Vector("Three"))))
//
//    val prependElements = sc.parallelize(Seq(
//      (1,Vector("PrependOne")),
//      (1,Vector("PrependTwo")),
//      (1,Vector("PrependThree"))))
//
//    listElements.saveToCassandra("ks", "collections_mod", SomeColumns("key", "lcol" append))
//    prependElements.saveToCassandra("ks", "collections_mod", SomeColumns("key", "lcol" prepend))
//
//
//    case class Address(city: String, street: String, number: Int)
//    case class CompanyRow(name: String, address: Address)
//    val address = Address(city = "Oakland", street = "Broadway", number = 3400)
//    sc.parallelize(Seq(CompanyRow("Paul", address))).saveToCassandra("test", "companies")
//
//    case class Company(name: String, address: UDTValue)
//    val address1 = UDTValue.fromMap(Map("city" -> "Santa Clara", "street" -> "Freedom Circle", "number" -> 3975))
//    val company = Company("DataStax", address1)
//    sc.parallelize(Seq(company)).saveToCassandra("test", "companies")
//
//
//    val table1 = TableDef.fromType[WordCount]("test", "words_new")
//    val table2 = TableDef("test", "words_new_2", table1.partitionKey, table1.clusteringColumns,
//      table1.regularColumns :+ ColumnDef("additional_column", RegularColumn, IntType))
//    val collection3 = sc.parallelize(Seq(WordCount("dog", 50), WordCount("cow", 60)))
//    collection3.saveAsCassandraTableEx(table2, SomeColumns("word", "count"))
//  }
//
//
//  def delete()(implicit spark: SparkSession): Unit ={
//    import com.datastax.spark.connector._
//    val sc = spark.sparkContext
//
//    sc.cassandraTable("test", "word_groups")
//      .where("count < 10")
//      .deleteFromCassandra("test", "word_groups")
//
//    sc.parallelize(Seq(("animal", "trex"), ("animal", "mammoth")))
//      .deleteFromCassandra("test", "word_groups")
//
//    sc.parallelize(Seq(("animal", "mammoth")))
//      .deleteFromCassandra("test", "word_groups", SomeColumns("count"))
//
//
//    case class Key (group:String)
//    sc.parallelize(Seq(Key("animal")))
//      .deleteFromCassandra("test", "word_groups", keyColumns = SomeColumns("group"))
//
//    val rdd = spark.sparkContext.cassandraTable("test", "words")
//    rdd.deleteFromCassandra(
//      "test",
//      "tab",
//      writeConf = WriteConf(timestamp = TimestampOption.constant(1L)))
//  }
//
//  def mapper()(implicit spark: SparkSession): Unit ={
//    import com.datastax.spark.connector._
//    val sc = spark.sparkContext
//
//    sc.cassandraTable[(String, Int)]("test", "words").select("word", "count")
//    // Array((bar,20), (foo,10))
//
//    sc.cassandraTable[(Int, String)]("test", "words").select("count", "word")
//    // Array((20,bar), (10,foo))
//
//    sc.cassandraTable[WordCount]("test", "words")
//
//
//    import com.datastax.spark.connector.mapper.JavaBeanColumnMapper
//    class WordCount extends Serializable {
//      private var _word: String = ""
//      private var _count: Int = 0
//      def setWord(word: String) { _word = word }
//      def setCount(count: Int) { _count = count }
//      override def toString = _word + ":" + _count
//    }
//
//    object WordCount {
//      implicit object Mapper extends JavaBeanColumnMapper[WordCount]
//    }
//
//    sc.cassandraTable[WordCount]("test", "words")
//    // Array(bar:20, foo:10)
//
//
//    import com.datastax.spark.connector.types._
//    import scala.reflect.runtime.universe._
//
//    object StringToEMailConverter extends TypeConverter[EMail] {
//      def targetTypeTag = typeTag[EMail]
//      def convertPF = { case str: String => EMail(str) }
//    }
//
//    object EMailToStringConverter extends TypeConverter[String] {
//      def targetTypeTag = typeTag[String]
//      def convertPF = { case EMail(str) => str }
//    }
//
//    TypeConverter.registerConverter(StringToEMailConverter)
//    TypeConverter.registerConverter(EMailToStringConverter)
//  }
//
//
//
//}
//
//case class EMail(email:String)
//case class WordCount(word: String, count: Long)