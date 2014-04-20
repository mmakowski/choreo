package com.bimbr.choreo.model

import scala.collection.immutable.SortedMap
import scala.reflect.ClassTag

case class Events(asMap: SortedMap[Instant, Seq[Event]]) {
  def apply(instant: Instant): Seq[Event] = asMap.getOrElse(instant, Seq())

  def :+(event: Event): Events = {
    val existingEventsAtInstant = this(event.time)
    val updatedEvents = asMap + (event.time -> (existingEventsAtInstant :+ event))
    copy(asMap = updatedEvents)
  }

  def :++(events: Iterable[Event]) = events.foldLeft(this)(_ :+ _)

  def asSequence: Iterable[Event] = for (bucket <- asMap.values; event <- bucket) yield event

  def without[B <: Event : ClassTag]: Events =
    copy(asMap = asMap.mapValues(_.filterNot(e => implicitly[ClassTag[B]].runtimeClass.isAssignableFrom(e.getClass))))

  def filter(p: Event => Boolean): Events =
    copy(asMap = asMap.mapValues(_.filter(p)).filterNot { case (k, v) => v.isEmpty })
}

object Events {
  def apply(events: Event*): Events = Empty :++ events

  val Empty = Events(SortedMap.empty[Instant, Seq[Event]])
}
