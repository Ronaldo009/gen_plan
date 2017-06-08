import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}

/**
  * Created by yanfa on 2017/6/1.
  */
object pagerank {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("spark")
    val sc= new SparkContext(conf)

    val links = sc.parallelize(List(
      ("A",List("B","C")),
      ("B",List("A","D")),
      ("C",List("A")),
      ("D",List("A","B","C"))
    )).partitionBy(new HashPartitioner(10))
      .persist()

    var ranks = links.mapValues(v => 0.25)
    ranks.foreach(x=>println(x))

    for(i <- 0 until 10){
      val contributions = links.join(ranks)
      val aa=contributions.flatMap{
        case(pageId, (links, rank)) =>
          links.map(link => (link, rank / links.size))
      }
      ranks = aa
        .reduceByKey((x,y) => x+y)
        .mapValues(v => 0.2*0.25 + 0.8*v)
    }
    ranks.collect().foreach(println)
  }
  def foo():Boolean={
    return true
  }

}
