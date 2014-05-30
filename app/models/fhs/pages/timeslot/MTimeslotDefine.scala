package models.fhs.pages.timeslot

import models.Transactions
import models.persistence.template.TimeSlotTemplate
import scala.collection.JavaConversions._
import java.util.Collections
import models.persistence.enumerations.EDuration

/**
 * @author fabian
 *         on 23.01.14.
 *
 *         this is one of the first classes in the project, sry for the chaos ;)
 */
case class MTimeslotDefine(startHour: Int, startMinutes: Int, stopHour: Int, stopMinutes: Int, unpopular: Boolean, weekdays: List[Int])

case class MTimeslotDisplay(id: Long, startHour: Int, startMinutes: Int, stopHour: Int, stopMinutes: Int, weekday: String, weekdayIndex: Int, duration: EDuration) extends Ordered[MTimeslotDisplay] {
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
    val timeslots = Transactions.hibernateAction {
      implicit session =>
        val dbReslut = session.createCriteria(classOf[TimeSlotTemplate]).list().asInstanceOf[java.util.List[TimeSlotTemplate]]
        Collections.sort(dbReslut)
        dbReslut.toList
    }

    timeslots
  }


}