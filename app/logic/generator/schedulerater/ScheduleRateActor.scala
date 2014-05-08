package logic.generator.schedulerater

import akka.actor.Actor
import logic.generator.schedulerater.rater.ERaters
import scala.collection.JavaConversions._
import models.persistence.scheduletree.{TimeSlot, Weekday}

/**
 * @author fabian 
 *         on 05.05.14.
 */
class ScheduleRateActor extends Actor{

  private var rates=Map[ERaters, Int]()

  override def receive = {

    case Rate( schedule ) =>

      val timeSlots = schedule.getRoot.getChildren.flatMap{
        case w:Weekday=> w.getChildren.toList.asInstanceOf[List[TimeSlot]]
      }.toList

      val lectures = ERaters.values().par.flatMap{
        r =>
          val (theRate, theLectures) =  r.getRater.rate(timeSlots)
          rates+= r -> theRate
          theLectures
      }.toSet

      //lectures.foreach(_.increaseDifficultLevel())

      sender() ! RateAnswer(rates)

    case _ =>
  }

}
