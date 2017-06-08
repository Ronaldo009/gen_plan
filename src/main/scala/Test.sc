import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

val GIVEN_QUERY="{'days': [4,14], 'countries': [{'country_id': 28, 'day': None}]," +
  "'regions': [{'region_id': 2, 'day': None}, {'region_id': 3, 'day': None}, {'region_id': 4, 'day': None}], 'pois': [3810,6666]," +
  "'regionNotGo': [], 'poiNotGo': [4815], 'regionSorted': [], 'availableMonths': [1,2,3,4,5,6,7,8,9,10],'price': [0, 80000]," +
  " 'hotelRating': None, 'arrivalRegionId': None, 'departRegionId': 3}"
val Query=GIVEN_QUERY.replace("'","\"").replace("None","null")
val mapper = new ObjectMapper() with ScalaObjectMapper

mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
mapper.registerModule(DefaultScalaModule)
val obj = mapper.readValue[Map[String,Any]](Query)


val limit_day = obj("days") match {
  case List(x, y) => (x.toString.toInt, y.toString.toInt)
}
limit_day
val availableMonths = obj("availableMonths").asInstanceOf[List[Int]]

val departRegionId=obj("departRegionId")
val arrivalRegionId = obj("arrivalRegionId").asInstanceOf[Int]

val regionSort=List(2,23,7)
val region=List(2,4,5,23,8,9,7)

region.intersect(regionSort)==regionSort
val regionSorted = obj("regionSorted").asInstanceOf[List[Int]]





//region_id.length






