package me.rotemfo.squbs.sample

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class HelloActorSpec extends TestKit(ActorSystem("SampleActorSpec")) with FlatSpecLike
with Matchers with ImplicitSender {

  "SampleActor" should "emit single response for PingRequest" in {
    val target = system.actorOf(Props[HelloActor])
    target ! PingRequest("foo")
    expectMsg(1.second, PingResponse("Hello foo welcome to squbs!"))
  }

  "SampleActor" should "emit multiple responses for ChunkRequest" in {

    val chunks = Seq("Hello ", "foo", " welcome ", "to ", "squbs!", "LastChunk")

    val target = system.actorOf(Props[HelloActor])

    import akka.pattern.ask
    implicit val timeout: Timeout = 3.seconds
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    val future = (target ? ChunkRequest("foo", 200.milliseconds)).mapTo[ChunkSourceMessage]
    val srcMessage = Await.result(future, 2.seconds)
    srcMessage.source.runWith(Sink.actorRef(self, "Done!"))
    chunks foreach { chunk => expectMsg(1.second, PingResponse(chunk)) }
    expectMsg("Done!")
  }
}
