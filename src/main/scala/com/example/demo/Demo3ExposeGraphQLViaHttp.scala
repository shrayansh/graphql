package com.example.demo

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import com.example.model.Book
import sangria.execution._
import sangria.macros.derive._
import sangria.marshalling.sprayJson._
import com.example.common.DemoRoute
import sangria.schema._



/** Let's expose our GraphQL schema via HTTP API */
object Demo3ExposeGraphQLViaHttp extends App {

  // STEP: Define some data

  val books = List(
    Book("1", "Harry Potter and the Philosopher's Stone", "J. K. Rowling"),
    Book("2", "A Game of Thrones", "George R. R. Martin"))

  // STEP: Define GraphQL Types & Schema

  val BookType = deriveObjectType[Unit, Book]()

  val QueryType = ObjectType("Query", fields[Unit, Unit](
    Field("books", ListType(BookType), resolve = _ ⇒ books)))

  val schema = Schema(QueryType)

  // STEP: Create akka-http server and expose GraphQL route

  // NEW: define GraphQL route

  implicit val system = ActorSystem("sangria-server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  // NEW: define GraphQL route
  val route = DemoRoute.route { (query, operationName, variables, _) ⇒

    // NEW: execute GraphQL query coming from HTTP request
    Executor.execute(schema, query,
      variables = variables,
      operationName = operationName)
  }

  // NEW: start an HTTP server and serve the GraphQL route
  Http().bindAndHandle(route, "0.0.0.0", 8080).foreach(_ ⇒
    println("Server started on port 8080"))

}