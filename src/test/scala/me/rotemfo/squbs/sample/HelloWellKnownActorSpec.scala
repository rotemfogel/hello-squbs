package me.rotemfo.squbs.sample

import akka.testkit.ImplicitSender
import org.scalatest.{FlatSpecLike, Matchers}
import org.squbs.actorregistry.ActorLookup
import org.squbs.testkit.CustomTestKit

import scala.concurrent.duration._

class HelloWellKnownActorSpec extends CustomTestKit(resources = Seq.empty, withClassPath = true)
  with FlatSpecLike with Matchers with ImplicitSender {

  "SampleWellKnownActor" should "forward message to SampleActor and get a response from SampleActor" in {
    ActorLookup("squbs-sample") ! PingRequest("foo")
    expectMsg(1.second, PingResponse("Hello foo welcome to squbs!"))
  }
}