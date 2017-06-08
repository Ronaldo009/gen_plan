import java.text.{DecimalFormat, SimpleDateFormat}
import java.util.Date

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

import math.random
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}



/**
  * Created by yanfa on 2017/4/26.
  */



object Uniqueway {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("sparkpi")
    val sc = new SparkContext(conf).setLogLevel("WARN")

    val sparkSession = SparkSession.builder
      .master("local")
      .appName("tidyGenPaln")
      .getOrCreate()

    import sparkSession.implicits._
    import org.apache.spark.sql.types._

    val GIVEN_QUERY = "{'days': [4,14], 'countries': [{'country_id': 28, 'day': None}]," +
      "'regions': [{'region_id': 2, 'day': None}, {'region_id': 27, 'day': None}, {'region_id': 69, 'day': None}], 'pois': [3810,6666]," +
      "'regionNotGo': [70], 'poiNotGo': [4815], 'regionSorted': [2, 27], 'availableMonths': [1,2,3,4,5,6,7,8,9,10],'price': [0, 80000], 'hotelRating': None, 'arrivalRegionId': None, 'departRegionId': None}"
    val Query = GIVEN_QUERY.replace("'", "\"").replace("None", "null")
    val mapper = new ObjectMapper() with ScalaObjectMapper

    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    mapper.registerModule(DefaultScalaModule)
    val obj = mapper.readValue[Map[String, Any]](Query)

    val limit_day = obj("days") match {
      case List(x, y) => (x.toString.toInt, y.toString.toInt)
    }
    val countries = obj("countries").asInstanceOf[List[Any]].map(x => x.asInstanceOf[Map[String, Int]]).groupBy(x => x("country_id")).map((x => x._1 -> x._2.last("day")))
    val country_id = countries.keys.toList
    val regions = obj("regions").asInstanceOf[List[Any]].map(x => x.asInstanceOf[Map[String, Int]])
      .groupBy(x => x("region_id")).map(x => x._1 -> x._2.last("day"))
    val region_id = regions.keys.toList
    val poi = obj("pois").asInstanceOf[List[Int]]
    val poiNotGo = obj("poiNotGo").asInstanceOf[List[Int]]
    val regionNotGo = obj("regionNotGo").asInstanceOf[List[Int]]
    val regionSorted = obj("regionSorted").asInstanceOf[List[Int]]
    val availableMonths = obj("availableMonths").asInstanceOf[List[Int]]
    val price = obj("price").asInstanceOf[List[Any]] match {
      case List(x, y) => (x.toString.toInt, y.toString.toInt)
    }
    val hotelRating = obj("hotelRating").asInstanceOf[Int]
    val arrivalRegionId = obj("arrivalRegionId").asInstanceOf[Int]
    val departRegionId = obj("departRegionId").asInstanceOf[Int]

    val iterNums = 5

    val url = "jdbc:mysql://127.0.0.1:3306/uniqueway_development"
    val table1 = "tidy_parts"
    val sparkTidy_parts = sparkSession.read.format("jdbc")
      .option("url", url)
      .option("dbtable", table1)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()
    sparkTidy_parts.createOrReplaceTempView("tidy_parts")

    val table2 = "regions"
    val sparkRegions = sparkSession.read.format("jdbc")
      .option("url", url)
      .option("dbtable", table2)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()

    sparkRegions.createOrReplaceTempView(table2)

    val table3 = "pois"
    val sparkPois = sparkSession.read.format("jdbc")
      .option("url", url)
      .option("dbtable", table3)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()
    sparkPois.createOrReplaceTempView(table3)

    val table4 = "currencies"
    val sparkCurrencies = sparkSession.read.format("jdbc")
      .option("url", url)
      .option("dbtable", table4)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()
    sparkCurrencies.createOrReplaceTempView(table4)

    val table5 = "places"
    val sparkPlaces = sparkSession.read.format("jdbc")
      .option("url", url)
      .option("dbtable", table5)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()
    sparkPlaces.createOrReplaceTempView(table5)

    val table6 = "countries"
    val sparkCountries = sparkSession.read.format("jdbc")
      .option("url", url)
      .option("dbtable", table6)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()
    sparkCountries.createOrReplaceTempView(table6)

    val table7 = "tidy_schedule_places"
    val sparkSchedulePlaces = sparkSession.read.format("jdbc")
      .option("url", url)
      .option("dbtable", table7)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()
    sparkSchedulePlaces.createOrReplaceTempView(table7)

    val table8 = "plans"
    val sparkPlans = sparkSession.read.format("jdbc")
      .option("url", url)
      .option("dbtable", table8)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()
    sparkPlans.createOrReplaceTempView(table8)

    val table9 = "poi_calendars"
    val sparkPoiCalendars = sparkSession.read.format("jdbc")
      .option("url", url)
      .option("dbtable", table9)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()
    sparkPoiCalendars.createOrReplaceTempView(table9)

    val table10 = "gen_plan_tags"
    val sparkPlanTags = sparkSession.read.format("jdbc")
      .option("url", url)
      .option("dbtable", table10)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()
    sparkPlanTags.createOrReplaceTempView(table10)

    val table11 = "tidy_schedule_pois"
    val sparkSchedulePois = sparkSession.read.format("jdbc")
      .option("url", url)
      .option("dbtable", table11)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()
    sparkSchedulePois.createOrReplaceTempView(table11)

    val table12 = "tags"
    val sparkTags = sparkSession.read.format("jdbc").option("url", url)
      .option("dbtable", table12)
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "").load()
    sparkTags.createOrReplaceTempView(table12)

    import java.time.LocalDateTime
    import java.sql.Timestamp

    val partPairs = sparkSession.sql("SELECT tidy_parts.id, parts2.id AS next_id FROM tidy_parts ,tidy_parts AS parts2 " +
      "WHERE tidy_parts.deleted_at is null " +
      "AND parts2.deleted_at is null " +
      "AND tidy_parts.tail_place_id = parts2.prev_place_id " +
      "AND tidy_parts.next_place_id = parts2.head_place_id " +
      "AND tidy_parts.tail = parts2.head " +
      "AND tidy_parts.departure_transit = parts2.arrive_transit " +
      "AND tidy_parts.state != 'canceled' " +
      "AND parts2.state != 'canceled' " +
      "AND (CAST(tidy_parts.end_time as long)-CAST(parts2.start_time as long)) < 7201 " +
      "AND (CAST(tidy_parts.end_time as long)-CAST(parts2.start_time as long)) >=0 ").repartition(3)
    val partRecords = sparkSession.sql("SELECT id,poi_ids,days,start_time, end_time, head_place_id, tail_place_id, place_id, destination_id, " +
      "plan_id, planner_id, self_drive, head, tail, schedules, next_place_id, prev_place_id,region_id from tidy_parts " +
      "where poi_ids is not NULL and state!='canceled' and deleted_at is null")
    var startPart = Seq.empty[Int].toDF("id").repartition(3)
    for (i <- country_id) {
      startPart = sparkSession.sql("SELECT tidy_parts.id FROM tidy_parts INNER JOIN regions ON tidy_parts.region_id = regions.id " +
        s"WHERE tidy_parts.is_start = 1 and tidy_parts.poi_ids is not NULL and tidy_parts.state!='canceled' " +
        s"and tidy_parts.deleted_at is null and regions.country_id=${i}").union(startPart).distinct().repartition(3)
    }
    val poiRecords = sparkSession.sql("SELECT id, price_number, rating, category, is_feature, is_prepaid, price, country_id, " +
      "is_forbid, type, rental_company, arrival_poi_id, transport_method, terminal_station_id ,longitude, latitude, place_id ," +
      "name ,display_name , currency_id, suitable_month FROM pois  ").repartition(3)
    //    val ww =poiRecords.collect().map(x=>(x(0), x(1), x(2), x(3), x(4), x(5), x(6), x(7), x(8), x(9), x(10), x(11), x(12), x(13), x(14), x(15), x(16), x(17), x(18), x(19)))
    //      .asInstanceOf[Array[(Int,Float,Float,String,Int,Int,Float,Int,Int,String,String,Int,String,Int,Float,Float,Int,String,String,String)]]


    val planRecords = sparkSession.sql("SELECT id, planner_id FROM plans")
    val schedulePoiRecords = sparkSession.sql("SELECT id, poi_id, times ,cross_days, start_time, end_time FROM tidy_schedule_pois")
    val placeRecords = sparkSession.sql("SELECT id, country_id, region_id, name FROM places")
    val regionRecords = sparkRegions.sqlContext.sql("SELECT id,name from regions ").repartition(3)
    val countryRecords = sparkSession.sql("SELECT id, name FROM countries")
    val schedulePlaceRecords = sparkSession.sql("SELECT id, place_id FROM tidy_schedule_places")
    val poiTagsRecords = sparkSession.sql("SELECT name, taggable_id as id FROM tags WHERE taggable_type = 'Poi'")
    val currencyRecords = sparkSession.sql("SELECT id, abbreviation, rate FROM currencies")
    val poiCalendarRecords = sparkSession.sql("SELECT distinct(poi_id) as id , month(date) as month FROM poi_calendars WHERE state in (\"available\",\"part_available\")")
    val tagsIdRecords = sparkSession.sql("SELECT id, name from gen_plan_tags")
    val endPart = sparkTidy_parts.sqlContext.sql("SELECT tidy_parts.id from tidy_parts where tidy_parts.is_end=1")


    val endParts = endPart.join(endPart, Seq("id")).rdd.flatMap(x => List(x(0))).collect()
    val start = partPairs.join(startPart, Seq("id")).rdd.map(x => List(x(0), x(1)))
    //    val start=startParts.rdd.map(x=>List(x(0)))

    //    制造字典
    val nextDic = partPairs.collect().map { x => (x(0), x(1)) }.groupBy(_._1).map { case (x, y) => (x.toString.toInt, y.toList) }.asInstanceOf[Map[Int, List[(Int, Int)]]]
    //    val nextDic=partPairs.collect().map{x=>(x(0),x(1))}.groupBy(_._1).map(x=>x._1 -> x._2.)
    //    nextDic.take(10).foreach(x=>x.))
    //    val dayDic=partRecords.rdd.map(x=>(x(0),(x(2)))).collect().toMap

    val poiDic = partRecords.rdd.map(x => (x(0), x(1).toString.replace("\n", "").
      replace("----", "").replace("- ", ",").replace(" ", "").split(",").map(x => x.toInt))).collect().toMap
    val poiInfoDic = poiRecords.collect().map(x => (x(0), x(1), x(2), x(3), x(4), x(5), x(6), x(7), x(8), x(9), x(10), x(11), x(12), x(13), x(14), x(15), x(16), x(17), x(18), x(19), x(20)))
      .map { x =>
        if (x._20 == null) {
          (x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16, x._17, x._18, x._19, 1, x._21)
        } else {
          (x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16, x._17, x._18, x._19, x._20, x._21)
        }

      }.groupBy(_._1)
    val partInfoDic = partRecords.collect().map(x => (x(0).toString.toInt, x(1), x(2), x(3), x(4), x(5), x(6), x(7), x(8), x(9), x(10), x(11), x(12), x(13), x(14), x(15), x(16), x(17))).groupBy(_._1)

    val currencyDic = currencyRecords.collect().map(x => (x(0), x(1), x(2))).groupBy(_._1)


    //    ------------------ 初始化days、price、poisList，并不断更新---------------------
    //    更新days、poisList
    def updatePoisAndDays(genPlan: (List[Int], Int, Int, List[Int], List[Int])): (List[Int], Int, Int, List[Int], List[Int]) = {
      val pois = genPlan._4.union(poiDic(genPlan._1.last))
      var days = 0
      if (genPlan._1.length < 2) {
        days = genPlan._2 + partInfoDic(genPlan._1.last)(0)._3.toString.toInt
      }
      else {
        val part1 = genPlan._1.init.last
        val part2 = genPlan._1.last
        if (partInfoDic(part1)(0)._14 == partInfoDic(part2)(0)._13 && partInfoDic(part2)(0)._13 == "incomplete") {
          days = genPlan._2 + partInfoDic(genPlan._1.last)(0)._3.toString.toInt - 1
        } else {
          days = genPlan._2 + partInfoDic(genPlan._1.last)(0)._3.toString.toInt
        }
      }
      val genPlans = genPlan.copy(_2 = days, _4 = pois)
      return genPlans
    }

    //    更新price 由于price依托于最新的poi，因此不能与poi一起更新
    def updatePriceAndAvailableMonths(genPlan: (List[Int], Int, Int, List[Int], List[Int])): (List[Int], Int, Int, List[Int], List[Int]) = {
      val price = genPlan._3 + getPrice(genPlan)
      var availableMonths = List().asInstanceOf[List[Int]]
      var monthsList = List().asInstanceOf[List[Int]]
      for (i <- genPlan._4) {
        val months = poiInfoDic(i)(0)._21
        if (months == null) {
          monthsList = List().asInstanceOf[List[Int]]
        }
        else {
          monthsList = months.toString.replace("月", "").split(",").toList.asInstanceOf[List[Int]]
        }
        if (genPlan._1.length < 2) {
          availableMonths = genPlan._5.union(monthsList)
        }
        else {
          availableMonths = genPlan._5.intersect(monthsList)
        }
      }
      val genPlans = genPlan.copy(_3 = price, _5 = availableMonths)
      return genPlans
    }

    //    在迭代过程中不断的获取价格，并保留
    def getPrice(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Int = {
      //TODO 获取旅馆以及飞机的价格
      val pricePoi = math.round(genPlan._4.map(x => poiInfoDic(x)(0)._2.toString.toFloat * currencyDic(poiInfoDic(x)(0)._20)(0)._3.toString.toFloat).reduce((x, y) => x + y))
      return pricePoi
    }

    //    满足到达regionId  最开始
    def arrivalRegionConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {
      if (arrivalRegionId!= 0) {
        if (partInfoDic(genPlan._1.head)(0)._18 != arrivalRegionId) {
          return false
        }
      }
      return true
    }

    //---------------------循环迭代 得到满足的part-----------------------------------------
    var partSatisfy = startPart.rdd.map(x => (List(x(0)).asInstanceOf[List[Int]], 0, 0, List(), List())).map(updatePoisAndDays)
      .map(updatePriceAndAvailableMonths).filter(arrivalRegionConstraint)
    println("count111:"+partSatisfy.count())
    for (i <- 1 to iterNums) {
      println("-----第" + i + "轮开始-----")
      val startPropTime = NowDate()
      //      val endResultss = partSatisfy.join(partPairs,Seq("id")).rdd.map(x=>(x,0,0,List()))
      val endResult = partSatisfy.filter(x => endParts.contains(x._1.last))
        .filter(x=>regionDayConstraint(x,regions)).filter(x=>poisMustContraint(x,poi)).filter(daysConstraint)
        .filter(x=>regionSortedConstraint(x,regionSorted)).filter(priceConstraint).filter(carRentalConstraint).repartition(3)

      if (!endResult.isEmpty()) {
        // 如果找到了结束的方案， 执行以下操作
        //        endResult=endResult.filter(x=>daysConstraint(x)).filter(x=>poisMustContraint(x,poi))
        //          .filter(x=>poisMustNotConstraint(x,poiNot))
        println("count222", endResult.count())
        endResult.take(10).foreach(x => "endResult:" + println(x._1))
      }
      if (i!=iterNums){
        partSatisfy = partSatisfy.filter(x => !(endParts.contains(x._1.last))).repartition(3)
        partSatisfy = partSatisfy.filter(x => nextDic.contains(x._1.last)).flatMap { x =>
          val aa = nextDic(x._1.last).map(v => v._2)
          val newParts = nextDic(x._1.last).map(v => v._2).map(k => x._1 :+ k)
          val newX = newParts.map(s => x.copy(_1 = s))
          newX
        }.map(updatePoisAndDays).map(updatePriceAndAvailableMonths)
          .filter(x=>regionConstraint(x,regionNotGo)).filter(x=>poisMustNotConstraint(x,poiNotGo)).filter(deDuplicate)
          .filter(daysConstraintMaxLimit).filter(priceConstraintMaxLimit).filter(stationConstraint).repartition(3)

      }
      println("count3333:"+partSatisfy.count())
      var endPropTime = NowDate()
      println("-----第" + i + "轮需要的时间为:" + (getCoreTime(startPropTime, endPropTime)) + "秒-----")
    }

    //    ------------------限制条件-------------------
    def processConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {
      val flagDaysMaxLimit = daysConstraintMaxLimit(genPlan)
      val flagPoiMustNot = poisMustNotConstraint(genPlan, poiNotGo)
      val flagDupulicate = deDuplicate(genPlan)
      val flagPricemaxLimit = priceConstraintMaxLimit(genPlan)
      val flagStation = stationConstraint(genPlan)
      val flagRegionNot = regionConstraint(genPlan, regionNotGo)
      if (flagDaysMaxLimit && flagPoiMustNot && flagDupulicate && flagPricemaxLimit && flagStation && flagRegionNot) {
        return true
      }
      return false
    }

    def resultConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {
      val flagDays = daysConstraint(genPlan)
      val flagPoiMust = poisMustContraint(genPlan, poi)
      val flagPrice = priceConstraint(genPlan)
      val flagRegionDays = regionDayConstraint(genPlan, regions)
      val flagAvaiMonths = availableMonthsConstraint(genPlan)
      val flagDeparture = departRegionConstraint(genPlan)
      val flagCarRental = carRentalConstraint(genPlan)
      if (flagDays && flagPoiMust && flagPrice && flagRegionDays && flagAvaiMonths && flagDeparture && flagCarRental) {
        return true
      }
      return false
    }

    //    最终的结果必须满足天数限制  最终
    def daysConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {
      val days = genPlan._2
      if (days > limit_day._1 && days < limit_day._2) {
        return true
      }
      return false
    }

    // 在迭代运行期间，删除大于最大限制天数的plan 过程
    def daysConstraintMaxLimit(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {
      val days = genPlan._2
      if (days > limit_day._2) {
        return false
      }
      return true
    }

    //在最终的结果中 必须包含想去的poi
    def poisMustContraint(genPlan: (List[Int], Int, Int, List[Int], List[Int]), pois: List[Int]): Boolean = {
      val poisGet = genPlan._4
      if (poisGet.intersect(pois).length == pois.length) {
        return true
      }
      return false
    }

    //    在最终结果中，尽可能的包含想去的poi
    def poisAsManyAsPossibleContraint(genPlan: (List[Int], Int, Int, List[Int], List[Int]), pois: List[Int]): Boolean = {
      val poisGet = genPlan._4
      if (poisGet.union(pois).length == poisGet.length) {
        return true
      }
      return false
    }

    //    必须不能包含不想去的poi  过程
    def poisMustNotConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int]), poisNot: List[Int]): Boolean = {
      val poisGet = genPlan._4
      if (poisNot.length != 0) {
        if (poisGet.intersect(poisNot).length == 0) {
          return true
        }
        return false
      }
      else {
        return true
      }
    }

    //    迭代运行期间去掉出现重复poi的plan  (有些poi是可以重复的，方法z中已判断重复的poi是否为该类型） 过程
    def deDuplicate(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {
      val poisGet = genPlan._4
      if (poisGet.distinct.length == poisGet.length) {
        return true
      }
      else {
        val dup = poisGet.diff(poisGet.distinct).distinct
        for (i <- dup) {
          if (!(List("Pois::CarRental", "Pois::Airport", "Pois::Hub").contains(poiInfoDic(i)(0)._10))) {
            return false
          }
        }
        return true
      }
    }

    //    获取现在时间
    def NowDate(): String = {
      val now: Date = new Date()
      val dateFormat: SimpleDateFormat = new SimpleDateFormat("HH:mm:ss")
      val date = dateFormat.format(now)
      return date
    }

    //    解析时间，求de时间差
    def getCoreTime(start_time: String, end_Time: String): Long = {
      var df: SimpleDateFormat = new SimpleDateFormat("HH:mm:ss")
      var begin: Date = df.parse(start_time)
      var end: Date = df.parse(end_Time)
      var between: Long = (end.getTime() - begin.getTime()) / 1000 //转化成秒
      return between
    }

    //    在最终的结果中，必须满足价格区间  最终
    def priceConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {

      if (genPlan._3 > price._1 && genPlan._3 < price._2) {
        return true
      }
      return false
    }

    //    在迭代运行期间，必须删除大于最大价格的plan  过程
    def priceConstraintMaxLimit(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {
      if (genPlan._3 > price._2) {
        return false
      }
      return true
    }

    //    获取前一个part的最后一个非Tip类型的poi->departurePoi
    def getLastNonTipPoi(part: Int): Int = {
      val poisInPart = poiDic(part).toList
      for (i <- poisInPart.reverse) {
        val poiType = poiInfoDic(i)(0)._10
        if (poiType != "Pois::Tip") {
          return i
        }
      }
      return 0
    }

    //    获取当前part的第一个非Tip类型的poi->arrivalPoi
    def getFirstNonTip(part: Int): Int = {
      val poisInPart = poiDic(part).toList
      for (i <- poisInPart) {
        val poiType = poiInfoDic(i)(0)._10
        if (poiType != "Pois::Tip") {
          return i
        }
      }
      return 0
    }

    //    departurePoi的离开交通方式 必须要与arrivalPoi的交通方式相同  过程
    def stationConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {
      val part1 = genPlan._1.init.last
      val part2 = genPlan._1.last
      val departurePoi = getLastNonTipPoi(part1)
      val arrivalPoi = getFirstNonTip(part2)
      if (poiInfoDic(departurePoi)(0)._10 == "Pois::Flight" && List(11, 27).contains(poiInfoDic(departurePoi)(0)._8)) {
        return true
      }
      if (poiInfoDic(departurePoi)(0)._10 == "Pois::Flight" && poiInfoDic(departurePoi)(0)._12 != arrivalPoi) {
        return false
      }
      if ((List("火车", "轮渡", "缆车", "地铁").contains(poiInfoDic(departurePoi)(0)._13)) && poiInfoDic(departurePoi)(0)._14 != arrivalPoi) {
        return false
      }
      return true
    }

    // 如果有不想去的region 则删除  过程
    def regionConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int]), regionNot: List[Int]): Boolean = {

      if (regionNot.length != 0) {
        if (regionNot.contains(partInfoDic(genPlan._1.last)(0)._18)) {
          return false
        }
      }
      return true
    }

    //  想去的region  而且该region必须满足相应的天数  最终
    def regionDayConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int]), regionDaysMap: Map[Int, Int]): Boolean = {
      val arr = new ArrayBuffer[Int]()
      for (i <- genPlan._1) {
        val a = partInfoDic(i)(0)._18.asInstanceOf[Int]
        arr += a
      }
      val keysList = regionDaysMap.keys.toList
      if (keysList.length != 0) {
        if (arr.toList.intersect(keysList).length >= keysList.length) {
          for (i <- genPlan._1) {
            if (regionDaysMap(partInfoDic(i)(0)._18.asInstanceOf[Int]) != null) {
              if (partInfoDic(i)(0)._3 != regionDaysMap(partInfoDic(i)(0)._18.asInstanceOf[Int])) {
                return false
              }
            }
            else {
              return true
            }
          }
        }
        else {
          return false
        }
      }
      return true
    }

    //    所有的poi必须满足想去的月份  zuizhong
    def availableMonthsConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {
      if (availableMonths.length!=0) {
        if (genPlan._5.intersect(availableMonths).length != availableMonths.length) {
          return false
        }
      }
      return true
    }

    //  满足离开regionId   最终
    def departRegionConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {
      if (departRegionId != 0) {
        if (partInfoDic(genPlan._1.last)(0)._18!= departRegionId) {
          return false
        }
      }
      return true
    }

    //  租车的poi必须是偶数 有借有还。最终
    def carRentalConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int])): Boolean = {
      val arrBuffer = new ArrayBuffer[Int]()
      for (i <- genPlan._4) {
        if (poiInfoDic(i)(0)._10 == "Pois::CarRental") {
          arrBuffer += i
        }
      }
      if (arrBuffer.length % 2 == 0) {
        return true
      }
      return false
    }
//    想要去的region 必须符合要求的顺序  最终
    def regionSortedConstraint(genPlan: (List[Int], Int, Int, List[Int], List[Int]),regionSort:List[Int]):Boolean={
      if (regionSort.length!=0){
        if (genPlan._4.intersect(regionSort)!=regionSort){
          return false
        }
      }
      return true

    }

  }


}

