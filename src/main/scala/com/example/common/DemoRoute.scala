package com.example.common

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, QueryAnalysisError}
import sangria.marshalling.sprayJson._
import sangria.parser._
import spray.json.{JsObject, JsString, JsValue}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object DemoRoute {


  def route(exeFn: (Document, Option[String], JsValue, Boolean) => Future[JsValue])(implicit ex: ExecutionContext): Route =
    (optionalHeaderValueByName("X-Apollo-Tracing")) { (tracing) ⇒

      path("graphql") {
        get {
          getFromResource("assets/playground.html")
        } ~
          post {
            parameters('query.?, 'operatonName.?, 'variables.?) { (queryParam, operationNameParam, variablesParam) =>
              entity(as[JsValue]) { body =>

                val JsObject(fields) = body
                val JsString(query) = fields("query")
                val operation = fields.get("operationName") collect {
                  case JsString(op) => op
                }
                val vars = fields.get("variables") match {
                  case Some(obj: JsObject) => obj
                  case _ => JsObject.empty
                }

                val queryToPrase = queryParam orElse Some(query)
                val operationNameExtracted = operationNameParam orElse operation

                queryToPrase.map(QueryParser.parse(_)) match {
                  case Some(Success(ast)) => DemoRoute.exeGQL(exeFn, ast, operationNameExtracted, vars, tracing.isDefined)
                  case Some(Failure(error)) => complete(BadRequest, JsObject("error" -> JsString(error.getMessage)))
                  case None => complete(BadRequest, JsObject("error" -> JsString("No query to execute")))
                }
              }
            }

          }
      }
    } ~
      (get & pathPrefix("assets")) {
        getFromResourceDirectory("assets")
      } ~  (get & pathEndOrSingleSlash) {
      redirect("/graphql", PermanentRedirect)
    }

  def exeGQL(
              exeFn: (Document, Option[String], JsValue, Boolean) => Future[JsValue],
              query: Document,
              operationName: Option[String],
              variables: JsObject,
              tracing: Boolean
            )(implicit executionContext: ExecutionContext)=
    complete(
      exeFn(query, operationName, variables, tracing)
        .map(OK -> _)
        .recover {
          case error: QueryAnalysisError => BadRequest -> error.resolveError
          case error: ErrorWithResolver => InternalServerError -> error.resolveError
        }
    )


  def simpleServer(exeFn: (Document, Option[String], JsValue, Boolean) => Future[JsValue]) = {
    implicit val system = ActorSystem("sangria-server")
    implicit val materializer = ActorMaterializer()

    import system.dispatcher

    val route = DemoRoute.route(exeFn)

    Http().bindAndHandle(route, "0.0.0.0", 8080).foreach(_ ⇒
      println("Server started on port 8080"))
  }
}