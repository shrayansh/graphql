package com.example.demo
import sangria.schema._
import sangria.macros.derive._
import com.example.model._
import sangria.slowlog.SlowLog
import sangria.execution._
import com.example.common.DemoRoute._
import scala.concurrent.ExecutionContext.Implicits.global
import sangria.marshalling.sprayJson._
import sangria.execution.deferred.{DeferredResolver, Fetcher, HasId}

object Demo7OptimizedQueryUsingFetchers extends App {

  // STEP: Define GraphQL Types & Schema

  // NEW: add fetcher to load authors in batch
  val authorFetcher = Fetcher.caching(
    (ctx: AuthorRepo, ids: Seq[String]) ⇒
      ctx.authors(ids))(HasId(_.id))
  val AuthorType = deriveObjectType[Unit, Author]()

  val BookType = deriveObjectType[AuthorRepo, Book](
    DeprecateField("authorId", "Please use `author` field instead."),
    AddFields(
      Field("author", OptionType(AuthorType),
        // NEW: use fetcher to defer loading author by ID
        resolve = c ⇒ authorFetcher.defer(c.value.authorId)))
  )

  val BookSortingType = deriveEnumType[BookSorting.Value]()

  val LimitArg = Argument("limit", OptionInputType(IntType), defaultValue = 5)
  val OffsetArg = Argument("offset", OptionInputType(IntType), defaultValue = 0)
  val BookSortingArg = Argument("sortBy", OptionInputType(BookSortingType))
  val TitleFilterArg = Argument("title", OptionInputType(StringType))

  val QueryType = ObjectType("Query", fields[BookRepo with AuthorRepo, Unit](
    Field("books", ListType(BookType),
      arguments = LimitArg :: OffsetArg :: BookSortingArg :: TitleFilterArg :: Nil,
      resolve = c => c.withArgs(LimitArg, OffsetArg, BookSortingArg, TitleFilterArg)(c.ctx.allBooks))
  ))

  val schema = Schema(QueryType)

  // STEP: Create akka-http server and expose GraphQL route

  val repo = InMemoryDbRepo.createDatabase

  simpleServer { (query, operationName, variables, tracing) ⇒
    Executor.execute(schema, query, repo,
      variables = variables,
      operationName = operationName,
      // NEW: provide fetcher to load object in batches during execution

      deferredResolver = DeferredResolver.fetchers(authorFetcher),
      middleware = if (tracing) SlowLog.apolloTracing :: Nil else Nil)
  }
}
