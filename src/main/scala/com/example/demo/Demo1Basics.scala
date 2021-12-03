package com.example.demo

import com.example.model.Book
import sangria.schema.{Field, ObjectType, StringType, fields, ListType, Schema}

/** Basic example of GraphQL Schema definition and query execution
  *
  *  lets say we want some data from server in below format
  *
  *
  *  {
  *    data{
  *          books: [
  *           {
  *             id: "id value"
  *             title: "title value"
  *             authorId: "author id value"
  *           },
  *           {
  *           ...
  *           }
  *         ]
  *      }
  *  }
  *
  *
  *
  * */



object Demo1Basics extends App {

  // STEP: Define some data

  val books = List(
    Book("1", "Harry Potter and the Philosopher's Stone", "J. K. Rowling"),
    Book("2", "A Game of Thrones", "George R. R. Martin"))


  // STEP: Define graphql schema and type

  val BookType = ObjectType("Book", fields[Unit, Book](
    Field("id", StringType, resolve = c => c.value.id),
    Field("title", StringType, resolve = _.value.title),
    Field("authorId", StringType, resolve = _.value.authorId)
  ))

  val QueryType = ObjectType("Query", fields[Unit, Unit](
    Field("books", ListType(BookType), resolve = _ => books)))

  /** define schema */

  val schema = Schema(QueryType)


  // STEP:  Define a query

  import sangria.macros._

  val query =
    graphql"""
      {
        books {
          title,
          authorId
        }
      }
      """

  // STEP: Execute query against the schema
  import sangria.execution.{Executor}
  import scala.concurrent.duration._
  import io.circe.Json
  import sangria.marshalling.circe._
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.{Future, Await}

  val result: Future[Json] = Executor.execute(schema, query)

  println(Await.result(result, 1 second))

}
