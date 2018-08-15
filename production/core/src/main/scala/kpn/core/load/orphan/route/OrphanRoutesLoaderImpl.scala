package kpn.core.load.orphan.route

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.routing.BalancingPool
import akka.util.Timeout
import kpn.core.engine.changes.data.AnalysisData
import kpn.core.repository.BlackListRepository
import kpn.core.repository.OrphanRepository
import kpn.core.tools.analyzer.CouchIndexer
import kpn.core.util.Log
import kpn.shared.NetworkType
import kpn.shared.Timestamp

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

object OrphanRoutesLoaderImpl {

  case class LoadRoute(messages: Seq[String], timestamp: Timestamp, routeId: Long)

}

class OrphanRoutesLoaderImpl(
  system: ActorSystem,
  analysisData: AnalysisData,
  routeIdsLoader: RouteIdsLoader,
  orphanRepository: OrphanRepository,
  blackListRepository: BlackListRepository,
  analysisDatabaseIndexer: CouchIndexer,
  worker: OrphanRoutesLoaderWorker
) extends OrphanRoutesLoader {

  private val log = Log(classOf[OrphanRoutesLoaderImpl])

  private implicit val askTimeout: Timeout = Timeout(32.hour)
  private implicit val executionContext: ExecutionContext = system.dispatcher

  import OrphanRoutesLoaderImpl._

  class WorkerActor extends Actor {
    def receive: Actor.Receive = {
      case LoadRoute(messages, timestamp, routeId) =>
        Log.context(messages) {
          sender() ! worker.process(timestamp, routeId)
        }
    }
  }

  private val workerPool = {
    val props = Props(classOf[WorkerActor], this)
    system.actorOf(BalancingPool(3).props(props), "orphan-routes-loader")
  }

  def load(timestamp: Timestamp): Unit = {
    NetworkType.all.foreach { networkType =>
      Log.context(networkType.name) {
        analysisDatabaseIndexer.index()
        val routeIds = routeIdsLoader.load(timestamp, networkType)
        val ignoredRouteIds = orphanRepository.ignoredRouteIds(networkType).toSet
        val blackListedRouteIds = blackListRepository.get.routes.map(_.id).toSet
        val candidateOrphanRouteIds = (routeIds -- ignoredRouteIds -- blackListedRouteIds).filterNot(isReferenced).toSeq.sorted

        log.info(s"Found ${routeIds.size} routes, ${ignoredRouteIds.size} ignored routes, ${candidateOrphanRouteIds.size} candidate orphan routes (unreferenced)")

        val futures = candidateOrphanRouteIds.zipWithIndex.map { case (routeId, index) =>
          Log.context(s"${index + 1}/${candidateOrphanRouteIds.size}") {
            val messages = Log.contextMessages
            workerPool ? LoadRoute(messages, timestamp, routeId)
          }
        }
        Await.result(Future.sequence(futures), Duration.Inf)
      }
    }
  }

  private def isReferenced(routeId: Long): Boolean = {
    analysisData.networks.isReferencingRelation(routeId)
  }
}
