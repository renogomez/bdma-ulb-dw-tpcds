import java.io.{File, FileWriter}
import java.util.Properties

import Config._
import com.github.tototoshi.csv.CSVWriter
import com.typesafe.scalalogging.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

import scala.collection.immutable.SortedMap
import scala.collection.mutable.ListBuffer
import scala.io.Source


object TcpDsBenchmark {

  val EMPTY : String = ""
  val cores: Int = Runtime.getRuntime.availableProcessors
  //val cores: Int = 4
  val logger = Logger(LoggerFactory.getLogger(TcpDsBenchmark.getClass))
  val parallelismLevel = 400

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("DB Warehouse Project SQL")
    sparkConf.set("spark.sql.crossJoin.enabled", "true")

    val spark = SparkSession.builder.config(sparkConf).master("local").getOrCreate

    logger.info("Attempting to Start Apache Spark")
    val mysqlConnProperties = new Properties()
    mysqlConnProperties.setProperty("user", USERNAME)
    mysqlConnProperties.setProperty("password", PASSWORD)

    //We use partitioning only for big tables
    //if we are using item key as partitiong key, then the Upper bound of 1 million means that we are assuming that there are million items.
    //TODO : The upper bound should be dynamic (i.e should vary with data set size)
    val callCenterTable = spark.read.jdbc(URL, CALL_CENTER,  mysqlConnProperties)
    val catalogPageTable = spark.read.jdbc(URL, CATALOG_PAGE, mysqlConnProperties)
    val catalogReturnsTable = spark.read.jdbc(URL, CATALOG_RETURNS, mysqlConnProperties)
    val catalogSalesTable = spark.read.jdbc(URL, CATALOG_SALES, CATALOG_SALES_PARTITIONING_KEY, 10000, 2000000, parallelismLevel , mysqlConnProperties)
    val customerTable = spark.read.jdbc(URL, CUSTOMER, mysqlConnProperties)
    val dateTimTable = spark.read.jdbc(URL, DATE_DIM, mysqlConnProperties)
    val customAddressTable = spark.read.jdbc(URL, CUSTOMER_ADDRESS, mysqlConnProperties)
    val customerDemographicsTable = spark.read.jdbc(URL, CUSTOMER_DEMOGRAPHICS, CUSTOMER_DEMOGRAPHICS_PARTITIONING_KEY, 10000, 100000, parallelismLevel, mysqlConnProperties)
    val dbGenVersionTable = spark.read.jdbc(URL, DBGEN_VERSION, mysqlConnProperties)
    val houseHoldDemographicsTable = spark.read.jdbc(URL, HOUSEHOLD_DEMOGRAPHICS, mysqlConnProperties)
    val incomeBandTable = spark.read.jdbc(URL, INCOME_BAND, mysqlConnProperties)
    val inventoryTable = spark.read.jdbc(URL, INVENTORY,INVENTORY_PARTITIONING_KEY, 10000, 200000, parallelismLevel, mysqlConnProperties)
    val itemTable = spark.read.jdbc(URL, ITEM, mysqlConnProperties)
    val promotionTable = spark.read.jdbc(URL, PROMOTION, mysqlConnProperties)
    val reasonsTable = spark.read.jdbc(URL, REASON, mysqlConnProperties)
    val shipModeTable = spark.read.jdbc(URL, SHIP_MODE, mysqlConnProperties)
    val storeTable = spark.read.jdbc(URL, STORE, mysqlConnProperties)
    val storeReturnsTable = spark.read.jdbc(URL, STORE_RETURNS, mysqlConnProperties)
    val storesSalesTable = spark.read.jdbc(URL, STORE_SALES, STORES_SALES_PARTITIONING_KEY,  10000, 2000000, parallelismLevel , mysqlConnProperties)
    val timeDimTable = spark.read.jdbc(URL, TIME_DIM, TIME_PARTITIONING_KEY,  10000, 2000000, parallelismLevel , mysqlConnProperties)
    val webPageTable = spark.read.jdbc(URL, WEB_PAGE, mysqlConnProperties)
    val webReturnsTable = spark.read.jdbc(URL, WEB_RETURNS, mysqlConnProperties)
    val warehouseTable = spark.read.jdbc(URL, WAREHOUSE, mysqlConnProperties)
    val webSalesTable = spark.read.jdbc(URL, WEB_SALES, WEB_SALES_PARTITIONING_KEY, 10000, 2000000, parallelismLevel, mysqlConnProperties)
    val webSiteTable = spark.read.jdbc(URL, WEB_SITE, mysqlConnProperties)

    callCenterTable.createOrReplaceTempView(CALL_CENTER)
    catalogPageTable.createOrReplaceTempView(CATALOG_PAGE)
    catalogReturnsTable.createOrReplaceTempView(CATALOG_RETURNS)
    catalogSalesTable.createOrReplaceTempView(CATALOG_SALES)
    customerTable.createOrReplaceTempView(CUSTOMER)
    customAddressTable.createOrReplaceTempView(CUSTOMER_ADDRESS)
    customerDemographicsTable.createOrReplaceTempView(CUSTOMER_DEMOGRAPHICS)
    dateTimTable.createOrReplaceTempView(DATE_DIM)
    dbGenVersionTable.createOrReplaceTempView(DBGEN_VERSION)
    houseHoldDemographicsTable.createOrReplaceTempView(HOUSEHOLD_DEMOGRAPHICS)
    incomeBandTable.createOrReplaceTempView(INCOME_BAND)
    inventoryTable.createOrReplaceTempView(INVENTORY)
    itemTable.createOrReplaceTempView(ITEM)
    promotionTable.createOrReplaceTempView(PROMOTION)
    reasonsTable.createOrReplaceTempView(REASON)
    shipModeTable.createOrReplaceTempView(SHIP_MODE)
    storeTable.createOrReplaceTempView(STORE)
    storeReturnsTable.createOrReplaceTempView(STORE_RETURNS)
    storesSalesTable.createOrReplaceTempView(STORE_SALES)
    timeDimTable.createOrReplaceTempView(TIME_DIM)
    webPageTable.createOrReplaceTempView(WEB_PAGE)
    webReturnsTable.createOrReplaceTempView(WEB_RETURNS)
    warehouseTable.createOrReplaceTempView(WAREHOUSE)
    webSalesTable.createOrReplaceTempView(WEB_SALES)
    webSiteTable.createOrReplaceTempView(WEB_SITE)

    val sparkSqlContext = spark.sqlContext

    val sqlQueriesMap = SortedMap(getQueriesMap.toSeq.sortBy(_._1):_*)
    val benchmarkStatistics =  ListBuffer[List[String]]()

    for((index, sqlQueries) <- sqlQueriesMap) {
      val queryNo = index + 21
      if(queryNo == 1000){
        logger.info ("Skipping Query {}", queryNo)
      }
      else{
        logger.info ("Executing Query {}", queryNo)
        val start = System.currentTimeMillis()

        for(sqlQuery <- sqlQueries) {
          val dataFrame = sparkSqlContext.sql(sqlQuery)
          dataFrame.show(1000000)
        }

        val stop = System.currentTimeMillis()
        val timeTakenMs = stop-start
        val timeTakenSeconds = toSeconds(timeTakenMs)
        benchmarkStatistics += List(queryNo.toString, parallelismLevel.toString, timeTakenSeconds.toString)
        appendToCsv(queryNo + ",  " + cores + ",  " + parallelismLevel + ", " + timeTakenSeconds.toString()+ "\n")
        logger.info ("Time taken for Query {} : Milliseconds : {}, Seconds : {}", queryNo, timeTakenMs, timeTakenSeconds )
      }
    }
    writeToCsv(benchmarkStatistics)
    logger.info("Stopping Apache Spark")
    spark.stop()
  }


  private def getQueriesMap : Map[Int, Seq[String]] = {
    val queriesDirectory = new File(getClass.getResource("/queries").getFile)
    queriesDirectory.listFiles()
        .map(file => Source.fromFile(file).getLines.toList)
        .map(lines => {
          val stringBuilder = new StringBuilder
          //Queries with comments were causing problems. Therefore, all comments are ignored when building the sql query
          for(line <- lines if !isSqlComment(line)) {
            stringBuilder.append(line).append("\n")
          }
          //The last new line character was also creating a problem, therefore we strip the sql query of a newline character at the very end
          stringBuilder.toString.stripSuffix("\n")
        })
        //We have few cases where a single file has multiple sql queries. If we have any string that is not whitespace after a ';', then we confirm that we have multiple queries
        .map(query =>  if(query.contains(";")) query.split(";").toSeq else Seq(query))
        //We also need to strip every sql query of a ';'. This is done because Spark throws an error if there is a ';' present at end of query
        .map(queries => {
          queries.map(query => if(query.contains(";")) query.replaceAll(";", EMPTY).trim else query.trim)
        })
       .zipWithIndex
       .map({
          case (k, v) => (v, k)
       })
      .toMap
  }

  private def writeToCsv(records : ListBuffer[List[String]]) : Unit = {
    val file = new File("./benchmarkAll.csv")
    if (!file.exists) {
      file.createNewFile
    }
    val writer = CSVWriter.open(file)
    writer.writeAll(records.toList)
  }
  private def appendToCsv(records : String) : Unit = {


    val fw = new FileWriter("./benchmark.csv", true)
    try {
      fw.write(records)
    }
    finally fw.close()
  }

  private def isSqlComment(line : String) : Boolean = {
    if(line.startsWith("--")) true else false
  }

  private def toSeconds(ms : Long) : Int = {
    (ms / 1000).toInt
  }

}
