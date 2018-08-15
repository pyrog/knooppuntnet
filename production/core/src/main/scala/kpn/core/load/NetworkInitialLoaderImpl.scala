package kpn.core.load

import akka.actor.ActorSystem
import kpn.core.util.Log
import kpn.shared.Timestamp

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

class NetworkInitialLoaderImpl(
  system: ActorSystem,
  worker: NetworkInitialLoaderWorker
) extends NetworkInitialLoader {

  private val log = Log(classOf[NetworksLoaderImpl])

  val pool = new Pool[NetworkInitialLoad, Unit](system, "network-initial-loader")(worker.load)

  def load(timestamp: Timestamp, networkIds: Seq[Long]): Unit = {
    implicit val executionContext: ExecutionContext = system.dispatcher
    val futures = networkIds.zipWithIndex.map { case (networkId, index) =>
      Log.context(s"${index + 1}/${networkIds.size}") {
        pool.execute(NetworkInitialLoad(timestamp, networkId))
      }
    }
    Await.result(Future.sequence(futures), Duration.Inf)
  }
}
