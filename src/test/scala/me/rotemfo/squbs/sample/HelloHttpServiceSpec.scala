package me.rotemfo.squbs.sample

import akka.http.scaladsl.model.HttpEntity.{Chunk, LastChunk}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.RouteTestTimeout
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, native}
import org.scalatest.{FlatSpecLike, Matchers}
import org.squbs.testkit.{CustomRouteTestKit, TestRoute}

import scala.concurrent.duration._

class HelloHttpServiceSpec extends CustomRouteTestKit(resources = Seq.empty, withClassPath = true)
  with FlatSpecLike with Matchers {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds)

  val route: Route = TestRoute[HelloHttpService]

  behavior of "SampleHttpService"

  it should "handle simple request correctly" in {
    Get() ~> route ~> check {
      responseAs[String] should be ("Hello!")
    }
  }

  it should "handle path correctly" in {
    Get("/hello") ~> route ~> check {
      responseAs[String] should be ("Hello anonymous welcome to squbs!")
    }
  }

  it should "handle path segment and serialization" in {
    import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
    implicit val serialization: Serialization.type = native.Serialization
    implicit val formats: DefaultFormats.type = DefaultFormats
    Get("/hello/foo") ~> route ~> check {
      responseAs[PingResponse] should be (PingResponse("Hello foo welcome to squbs!"))
    }
  }

  it should "return bad request for path segment representing space" in {
    Get("/hello/%20") ~> route ~> check {
      status should be (StatusCodes.BadRequest)
    }
  }

  it should "handle path segment, chunking, with delay" in {
    Get("/hello/foo/500") ~> route ~> check {
      val expected = Chunk("Hello ") ::
        Chunk("foo") ::
        Chunk(" welcome ") ::
        Chunk("to ") ::
        Chunk("squbs!") ::
        LastChunk :: Nil
      chunks should be (expected)
    }
  }

  it should "handle path segment, chunking, no delay" in {
    Get("/hello/foo/0") ~> route ~> check {
      val expected = Chunk("Hello ") ::
        Chunk("foo") ::
        Chunk(" welcome ") ::
        Chunk("to ") ::
        Chunk("squbs!") ::
        LastChunk :: Nil
      chunks should be (expected)
    }
  }

  it should "handle post serialization and deserialization" in {
    import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
    implicit val serialization: Serialization.type = native.Serialization
    implicit val formats: DefaultFormats.type = DefaultFormats
    Post("/hello", PingRequest("bar")) ~> route ~> check {
      responseAs[PingResponse] should be (PingResponse("Hello bar welcome to squbs!"))
    }
  }

  it should "return bad request for request with blank field" in {
    import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
    implicit val serialization: Serialization.type = native.Serialization
    implicit val formats: DefaultFormats.type = DefaultFormats
    Post("/hello", PingRequest("")) ~> route ~> check {
      status should be (StatusCodes.BadRequest)
    }
  }
}
