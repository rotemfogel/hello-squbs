package me.rotemfo.squbs.sample

import akka.http.scaladsl.model.HttpEntity.{Chunk, LastChunk}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, native}
import org.squbs.actorregistry.ActorLookup
import org.squbs.unicomplex.RouteDefinition

import scala.concurrent.duration._
import scala.util.Success

/**
  * The route definition.
  */
class HelloHttpService extends RouteDefinition {

  implicit val timeout: Timeout = 1.seconds

  override def route: Route =
    get {
      anonymous ~ withName ~ chunks
    } ~
      post {
        withPost
      } ~
      complete("Hello!")

  private val anonymous: Route =
    path("hello") {
      onComplete(ActorLookup ? PingRequest("anonymous")) {
        case Success(PingResponse(message)) => complete(message)
        case _ => complete(StatusCodes.BadRequest)
      }
    }

  private val withPost: Route =
    path("hello") {
      // Configuration for Json4sSupport
      implicit val serialization: Serialization.type = native.Serialization
      implicit val formats: DefaultFormats.type = DefaultFormats
      entity(as[PingRequest]) { request =>
        onComplete(ActorLookup ? request) {
          case Success(response: PingResponse) => complete(response)
          case _ => complete(StatusCodes.BadRequest)
        }
      }
    }

  private val withName: Route =
    path("hello" / Segment) { who =>
      // Configuration for Json4sSupport
      implicit val serialization: Serialization.type = native.Serialization
      implicit val formats: DefaultFormats.type = DefaultFormats
      onComplete(ActorLookup ? PingRequest(who)) {
        case Success(response: PingResponse) => complete(response)
        case _ => complete(StatusCodes.BadRequest)
      }
    }

  private val chunks: Route =
    path("hello" / Segment / IntNumber) { (who, delay) =>
      onComplete(ActorLookup ? ChunkRequest(who, delay.milliseconds)) {
        case Success(srcMsg: ChunkSourceMessage) =>
          // This header is added for Chrome to handle chunking responses.  Please see
          // http://stackoverflow.com/questions/26164705/chrome-not-handling-chunked-responses-like-firefox-safari
          respondWithHeader(RawHeader("X-Content-Type-Options", "nosniff")) {
            complete(HttpEntity.Chunked(ContentTypes.`text/plain(UTF-8)`, srcMsg.source.map {
              case PingResponse("LastChunk") => LastChunk
              case PingResponse(msg) => Chunk(msg)
            }))
          }
        case _ => complete(StatusCodes.BadRequest)
      }
    }
}
