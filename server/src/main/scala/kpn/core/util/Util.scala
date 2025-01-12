package kpn.core.util

import kpn.api.common.Bounds

import java.lang.management.ManagementFactory
import scala.annotation.tailrec

object Util {

  def isDigits(string: String): Boolean = string.nonEmpty && string.forall(_.isDigit)

  def hasDigits(string: String): Boolean = string.nonEmpty && string.exists(_.isDigit)

  def withoutQuotes(string: String): String = {
    if (string.startsWith("\"") && string.endsWith("\"")) {
      string.substring(1, string.length - 1)
    }
    else {
      string
    }
  }

  def humanReadableBytes(bytes: Long): String = {

    val G = 1L << 30
    val M = 1L << 20
    val K = 1L << 10

    if (bytes >= G) {
      f"${bytes.toDouble / G}%.1fG"
    } else if (bytes >= M) {
      f"${bytes.toDouble / M}%.1fM"
    } else if (bytes >= K) {
      f"${bytes.toDouble / K}%.1fK"
    } else {
      bytes.toString
    }
  }

  def memoryUsed(): Long = {
    System.gc()
    val mbean = ManagementFactory.getMemoryMXBean
    mbean.getHeapMemoryUsage.getUsed
  }

  def withoutSuccessiveDuplicates[A](objects: Seq[A]): Seq[A] = {
    recursiveWithoutSuccessiveDuplicates(List(), objects).reverse
  }

  @tailrec
  private def recursiveWithoutSuccessiveDuplicates[A](objects: List[A], remainingObjects: Seq[A]): Seq[A] = {
    if (remainingObjects.isEmpty) {
      objects
    }
    else {
      val obj = remainingObjects.head
      if (objects.isEmpty || objects.head != obj) {
        recursiveWithoutSuccessiveDuplicates(obj :: objects, remainingObjects.tail)
      }
      else {
        recursiveWithoutSuccessiveDuplicates(objects, remainingObjects.tail)
      }
    }
  }

  def classNameOf(obj: Any): String = obj.getClass.getSimpleName.filterNot(_ == '$')

  def mergeBounds(boundss: Seq[Bounds]): Bounds = {
    if (boundss.isEmpty) {
      Bounds()
    }
    else {
      val minLat = boundss.map(_.minLat).min
      val maxLat = boundss.map(_.maxLat).max
      val minLon = boundss.map(_.minLon).min
      val maxLon = boundss.map(_.maxLon).max
      Bounds(
        minLat,
        minLon,
        maxLat,
        maxLon
      )
    }
  }

  def split[T](separator: T, list: Seq[T]): Seq[Seq[T]] = {
    split(separator, list, Seq.empty)
  }

  @tailrec
  private def split[T](separator: T, list: Seq[T], tmp: Seq[Seq[T]]): Seq[Seq[T]] = {
    list.indexOf(separator) match {
      case -1 => tmp ++ Seq(list)
      case i =>
        val s = list.splitAt(i)
        split(separator, s._2.tail, tmp ++ Seq(s._1))
    }
  }
}
