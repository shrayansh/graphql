package com.example.demo
import sangria.schema._
import sangria.macros.derive._
import com.example.model._
import sangria.execution._
import scala.concurrent.ExecutionContext.Implicits.global
import sangria.marshalling.sprayJson._
import com.example.common.DemoRoute


/** Use field arguments to provide pagination, sorting and filtering */

object Demo5PaginationSortingFiltering extends App{

  // STEP: Define GraphQL Types & Schema

  val BookType = deriveObjectType[Unit, Book]()

  val AuthorType = deriveObjectType[Unit, Author]()

  val BookSortingType = deriveEnumType[BookSorting.Value]()

  // NEW: define pagination, sorting & filter arguments

  val LimitArg = Argument("limit", OptionInputType(IntType), defaultValue = 5)
  val OffsetArg = Argument("offset", OptionInputType(IntType), defaultValue = 0)
  val BookSortingArg = Argument("sortBy", OptionInputType(BookSortingType))
  val TitleFilterArg = Argument("title", OptionInputType(StringType))

  val QueryType = ObjectType("Query", fields[BookRepo, Unit](
    Field("books", ListType(BookType),
    // NEW: declare arguments
      arguments = LimitArg :: OffsetArg :: BookSortingArg :: TitleFilterArg :: Nil,
      //NEW: retrive srgumesnt values  and pass them to `allbooks`
      resolve = c => c.withArgs(LimitArg, OffsetArg, BookSortingArg, TitleFilterArg)(c.ctx.allBooks))
  ))

  val schema = Schema(QueryType)

  // STEP: Create akka-http server and expose GraphQL route
  val repo = InMemoryDbRepo.createDatabase


  DemoRoute.simpleServer { (query, operationName, variables, _) ⇒
    Executor.execute(schema, query, repo,
      variables = variables,
      operationName = operationName)
  }

}
