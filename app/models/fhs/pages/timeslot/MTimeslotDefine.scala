package models.fhs.pages.timeslot

import models.Transactions
import models.persistence.template.TimeslotTemplate
import scala.collection.JavaConversions._

/**
 * Created by fabian on 23.01.14.
 */
case class MTimeslotDefine(startHour: Int, startMinutes: Int, stopHour: Int, stopMinutes: Int, weekdays: List[Int])

case class MTimeslotDisplay(id: Long, startHour: Int, startMinutes: Int, stopHour: Int, stopMinutes: Int, weekday: String, weekdayIndex: Int) extends Ordered[MTimeslotDisplay] {
  def compare(that: MTimeslotDisplay): Int = {
    val retSeq = Seq(this.weekdayIndex.compareTo(that.weekdayIndex), this.startHour.compareTo(that.startHour), this.stopHour.compareTo(that.stopHour), this.stopMinutes.compareTo(that.stopMinutes)).filter(_ != 0)
    if (retSeq.isEmpty) {
      0
    } else {
      retSeq(0)
    }
  }
}

object MTimeslotDisplay {

  def findAllTimeslots = {
    val timeslots = Transactions.hibernateAction{
      implicit session =>
        session.createCriteria(classOf[TimeslotTemplate]).list().asInstanceOf[java.util.List[TimeslotTemplate]]
    }



     timeslots.map {
      entry =>
        MTimeslotDisplay(entry.getId, entry.getStartHour, entry.getStartMinute, entry.getStopHour, entry.getStopMinute, entry.getParent.getName, entry.getParent.getSortIndex)
    }.sortWith(_ < _).toList
  }

}