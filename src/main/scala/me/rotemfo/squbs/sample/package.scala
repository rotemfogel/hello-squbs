package me.rotemfo.squbs

import akka.stream.scaladsl.Source

import scala.concurrent.duration.FiniteDuration

/**
  * project: squbs-sample
  * package: me.rotemfo.squbs.sample
  * file:    package
  * created: 2019-04-23
  * author:  rotem
  */
package object sample {
  case class  PingRequest(who: String)
  case class  PingResponse(message: String)
  case class  ChunkRequest(who: String, delay: FiniteDuration)
  case class  ChunkSourceMessage(source: Source[PingResponse, Any])
  case object EmptyRequest
}
