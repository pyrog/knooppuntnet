package kpn.core.db.couch

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.ConfigFactory
import kpn.core.app.ActorSystemConfig
import kpn.server.json.TimestampJsonDeserializer
import kpn.server.json.TimestampJsonSerializer
import kpn.shared.Timestamp
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt

object Couch {

  val uiTimeout: Timeout = Timeout(10.seconds)
  val defaultTimeout: Timeout = Timeout(60.seconds)
  val batchTimeout: Timeout = Timeout(5.minutes)

  def run(f: (ActorSystem, Couch) => Unit): Unit = {

    val system = ActorSystemConfig.actorSystem()
    val couchConfig = config

    try {
      val couch = new Couch(system, couchConfig)
      try {
        f(system, couch)
      } finally {
        couch.shutdown()
      }
    } finally {
      Await.result(system.terminate(), Duration.Inf)
      ()
    }
  }

  def oldExecuteIn(dbname: String)(action: OldDatabase => Unit): Unit = {
    oldExecuteIn("localhost", dbname)(action)
  }

  def oldExecuteIn(host: String, dbname: String)(action: OldDatabase => Unit): Unit = {
    oldExecuteIn(ActorSystemConfig.actorSystem(), host, dbname)(action)
  }

  def oldExecuteIn(system: ActorSystem, host: String, dbname: String)(action: OldDatabase => Unit): Unit = {
    val couchConfig = config.copy(host = host)
    try {
      val couch = new Couch(system, couchConfig)
      try {
        val database = new OldDatabaseImpl(couch, dbname)
        action(database)
      } finally {
        couch.shutdown()
      }
    } finally {
      Await.result(system.terminate(), Duration.Inf)
      ()
    }
  }

  def executeIn(databaseName: String)(action: Database => Unit): Unit = {
    executeIn("localhost", databaseName: String)(action: Database => Unit)
  }

  def executeIn(host: String, databaseName: String)(action: Database => Unit): Unit = {
    val couchConfig = config.copy(host = host)
    val database = new DatabaseImpl(couchConfig, objectMapper, databaseName)
    action(database)
  }

  def config: CouchConfig = {
    val properties = {
      val userFile = new File(System.getProperty("user.home") + "/.osm/osm.properties")
      if (userFile.exists()) {
        userFile
      }
      else {
        val currentWorkingDirectoryFile = new File("/kpn/conf/osm.properties")
        if (currentWorkingDirectoryFile.exists()) {
          currentWorkingDirectoryFile
        }
        else {
          val path1 = userFile.getAbsolutePath
          val path2 = currentWorkingDirectoryFile.getAbsolutePath
          val message = s"Couchdb configuration files ('$path1' or '$path2') not found"
          throw new RuntimeException(message)
        }
      }
    }
    try {
      val config = ConfigFactory.parseFile(properties)
      CouchConfig(
        config.getString("couchdb.host"),
        config.getInt("couchdb.port"),
        config.getString("couchdb.user"),
        config.getString("couchdb.password")
      )
    }
    catch {
      case e: Exception =>
        val message = s"Error parsing '${properties.getAbsolutePath}': " + e.getMessage
        throw new RuntimeException(message, e)
    }
  }

  val objectMapper: ObjectMapper = {
    val b = Jackson2ObjectMapperBuilder.json()
    b.serializationInclusion(NON_ABSENT)
    b.annotationIntrospector(new JacksonAnnotationIntrospector)
    b.deserializerByType(classOf[Timestamp], new TimestampJsonDeserializer())
    b.serializerByType(classOf[Timestamp], new TimestampJsonSerializer())

    val om: ObjectMapper = b.build()
    om.registerModule(DefaultScalaModule)
    om
  }

}

class Couch(val sys: ActorSystem, val config: CouchConfig) {
  implicit val system: ActorSystem = sys
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def shutdown(): Unit = {
    Http(system).shutdownAllConnectionPools().andThen { case _ => system.terminate() }

    // TODO AKKA-HTTP shutdown correct ???
    //Http().(system).ask(Http.CloseAll)(15.second).await
    //    Await.result(system.terminate(), Duration.Inf)
    ()
  }

}
