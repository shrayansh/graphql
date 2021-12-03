package com.example.demo
import sangria.macros.derive._
import com.example.model._
import sangria.schema.ObjectType
import sangria.schema._
import sangria.execution._
import sangria.marshalling.sprayJson._
import com.example.common.DemoRoute
import scala.concurrent.ExecutionContext.Implicits.global


object Demo4AddingDatabase extends App {

  // STEP: Define GraphQL Types & Schema

  val BookType = deriveObjectType[Unit, Book]()
   val AuthorType = deriveObjectType[Unit, Author]()


  // NEW: use repository type a context object


  val QueryType = ObjectType("Query", fields[BookRepo, Unit](
    Field("books", ListType(BookType),
      description = Some("Gives the list of books"),

      // NEW: load books from context object repo
      resolve = c => c.ctx.allBooks())
  ))

  val schema = Schema(QueryType)


  // NEW: crete new DB and repository
  val repo = InMemoryDbRepo.createDatabase

  DemoRoute.simpleServer { (query, operationName, variables, _) ⇒
    // NEW: provide a `repo` to a query executor
    Executor.execute(schema, query, repo,
      variables = variables,
      operationName = operationName)
  }
}
