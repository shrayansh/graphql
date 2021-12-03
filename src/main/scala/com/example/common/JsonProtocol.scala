package com.example.common
import com.example.model.{Author, Book}
import spray.json._

object JsonProtocol extends DefaultJsonProtocol {
  implicit val bookFormat: JsonFormat[Book] = jsonFormat4(Book)
  implicit val authorFormat: JsonFormat[Author] = jsonFormat3(Author)

}
