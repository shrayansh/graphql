package com.example.demo
import sangria.schema._
import sangria.macros.derive._
import com.example.model._
import sangria.execution._
import sangria.slowlog.SlowLog
import com.example.common.DemoRoute

import scala.concurrent.ExecutionContext.Implicits.global
import sangria.marshalling.sprayJson._
/** Representing book-author relation with an object type field */

object Demo6BookAuthorRelation extends App {

  // STEP: Define GraphQL Types & Schema

  val AuthorType = deriveObjectType[Unit, Author]()

  val BookType = deriveObjectType[AuthorRepo, Book](

    // NEW: deprecate `authorId` & add `author` field

    DeprecateField("authorId", "Please use `author` field instead."),
    AddFields(
      Field("author", OptionType(AuthorType),
        resolve = c ⇒ c.ctx.author(c.value.authorId)))
  )

  val BookSortingType = deriveEnumType[BookSorting.Value]()

  val LimitArg = Argument("limit", OptionInputType(IntType), defaultValue = 5)
  val OffsetArg = Argument("offset", OptionInputType(IntType), defaultValue = 0)
  val BookSortingArg = Argument("sortBy", OptionInputType(BookSortingType))
  val TitleFilterArg = Argument("title", OptionInputType(StringType))

  val QueryType = ObjectType("Query", fields[BookRepo, Unit](
    Field("books", ListType(BookType),

      arguments = LimitArg :: OffsetArg :: BookSortingArg :: TitleFilterArg :: Nil,

      resolve = c => c.withArgs(LimitArg, OffsetArg, BookSortingArg, TitleFilterArg)(c.ctx.allBooks))
  ))

  val schema = Schema(QueryType)

  // STEP: Create akka-http server and expose GraphQL route

  val repo = InMemoryDbRepo.createDatabase

  DemoRoute.simpleServer { (query, operationName, variables, tracing) ⇒
    Executor.execute(schema, query, repo,
      variables = variables,
      operationName = operationName,
      // NEW: add middleware to show tracing info in the playground
      middleware = if (tracing) SlowLog.apolloTracing :: Nil else Nil)
  }
}
