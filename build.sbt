name := "untitled1"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.scala-graph" %% "graph-core" % "1.11.4"

//libraryDependencies += "org.tpolecat" %% "doobie-core" % "0.3.0" // or any supported release above
//
//libraryDependencies += "com.typesafe.slick" %% "slick" % "3.1.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.12"

//val mysqlDriver = "mysql" % "mysql-connector-java" % "5.1.12"
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.0.1"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.0.1"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.0.1"

libraryDependencies +="org.json4s" %% "json4s-native" % "3.2.11"

//libraryDependencies ++= Seq("com.roundeights" %% "hasher" % "1.2.0")

// https://mvnrepository.com/artifact/org.apache.spark/spark-graphx_2.11
libraryDependencies += "org.apache.spark" % "spark-graphx_2.11" % "2.0.1"

//resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"
//libraryDependencies += "neo4j-contrib" % "neo4j-spark-connector" % "2.0.0-M2"
//
// https://mvnrepository.com/artifact/org.elasticsearch/elasticsearch-spark-20_2.11
libraryDependencies += "org.elasticsearch" % "elasticsearch-spark-20_2.11" % "5.2.2"

//// https://mvnrepository.com/artifact/org.elasticsearch/elasticsearch-hadoop
//libraryDependencies += "org.elasticsearch" % "elasticsearch-hadoop" % "5.2.2"
//

//libraryDependencies ++= Seq(
//  "com.typesafe.slick" %% "slick" % "3.2.0",
//  "org.slf4j" % "slf4j-nop" % "1.6.4",
//  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0"
//)

//libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "3.2.0"

//libraryDependencies += "org.scalaj" % "scalaj-http_2.11" % "2.3.0"

//libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.6"

libraryDependencies += "org.deeplearning4j" % "deeplearning4j-core" % "0.6.0"
//

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.3"
)

//libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.3.0"

//libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-core" % "2.1.1"

//resolvers ++= Seq(
//  "anormcypher" at "http://repo.anormcypher.org/",
//  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
//)

// https://mvnrepository.com/artifact/com.typesafe.play/play-ws_2.11
// libraryDependencies += "com.typesafe.play" % "play-ws_2.11" % "2.4.3"

//libraryDependencies ++= Seq(
//  "org.anormcypher" %% "anormcypher" % "0.9.1"
//)

//mainClass in assembly := some("package.MainClass")
//assemblyJarName := "desired_jar_name_after_assembly.jar"
//
//val meta = """META.INF(.)*""".r
//assemblyMergeStrategy in assembly := {
//  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
//  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
//  case n if n.startsWith("reference.conf") => MergeStrategy.concat
//  case n if n.endsWith(".conf") => MergeStrategy.concat
//  case meta(_) => MergeStrategy.discard
//  case x => MergeStrategy.first
//}


        