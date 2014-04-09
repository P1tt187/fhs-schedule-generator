package models.fhs.pages.generator

import models.Transactions
import org.hibernate.criterion._
import scala.collection.JavaConversions._
import models.persistence.{Schedule, Semester}
import models.persistence.subject.AbstractSubject
import models.fhs.pages.JavaList
import models.persistence.template.TimeslotTemplate
import scala.annotation.tailrec
import models.persistence.scheduletree.{Weekday, Timeslot}
import models.persistence.participants.Course
import scala.Some
import org.hibernate.FetchMode


/**
 * @author fabian 
 *         on 20.03.14.
 */
object MGenerator {

  def filterScheduleWithCourses(schedule: Schedule) = {
    val courses = Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Course]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[Course]]
    }

    courses.map(course => (course.getShortName, collectTimeslotsFromSchedule(schedule.filter(course))))
  }

  def collectTimeslotsFromSchedule(schedule: Schedule) = {
    schedule.getRoot.getChildren.asInstanceOf[JavaList[Weekday]].flatMap(_.getChildren.asInstanceOf[JavaList[Timeslot]]).toList.sorted
  }

  def findActiveSubjectsBySemesterId(id: Long) = {
    Transactions.hibernateAction {
      implicit session =>
        val criterion = session.createCriteria(classOf[AbstractSubject]).add(Restrictions.eq("active", true)).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
        criterion.createCriteria("semester").add(Restrictions.idEq(id))
        criterion.setFetchMode("criteriaContainer.house.rooms",FetchMode.JOIN)

        criterion.list().asInstanceOf[JavaList[AbstractSubject]].toList


    }
  }

  def findSemesterById(id: Long) = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Semester]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[Semester]
    }
  }

  def findSemesters() = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Semester]).addOrder(Order.desc("name")).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[Semester]].toList
    }
  }

  @tailrec
  def findTimeRanges(timeslotTemplate: List[TimeslotTemplate], timeRanges: List[TimeRange]): List[TimeRange] = {
    timeslotTemplate.headOption match {
      case None => timeRanges
      case Some(timeslot) =>
        val existingRange = timeRanges.filter {
          case TimeRange(startHour, startMinute, stopHour, stopMinute) =>
            val startEqual = startHour == timeslot.getStartHour && startMinute == timeslot.getStartMinute
            val stopEqual = stopHour == timeslot.getStopHour && stopMinute == timeslot.getStopMinute

            startEqual && stopEqual
        }
        if (existingRange.isEmpty) {
          findTimeRanges(timeslotTemplate.tail, timeRanges :+ TimeRange(timeslot.getStartHour, timeslot.getStartMinute, timeslot.getStopHour, timeslot.getStopMinute))
        } else {
          findTimeRanges(timeslotTemplate.tail, timeRanges)
        }
    }
  }

}

object TimeRange extends Ordering[TimeRange] {
  override def compare(range: TimeRange, that: TimeRange): Int = {
    if (range.startHour.compareTo(that.startHour) != 0) {
      return range.startHour.compareTo(that.startHour)
    }
    if (range.startMinute.compareTo(that.startMinute) != 0) {
      return range.startMinute.compareTo(that.startMinute)
    }
    if (range.stopHour.compareTo(that.stopHour) != 0) {
      return range.stopHour.compareTo(that.stopHour)
    }

    range.stopMinute.compareTo(that.stopMinute)
  }

}

case class TimeRange(startHour: Int, startMinute: Int, stopHour: Int, stopMinute: Int) extends Ordered[Timeslot] {


  override def toString = "" + startHour.formatted("%02d") + ":" + startMinute.formatted("%02d") + "-" + stopHour.formatted("%02d") + ":" + stopMinute.formatted("%02d")

  override def compare(that: Timeslot): Int = {
    if (startHour.compareTo(that.getStartHour) != 0) {
      return startHour.compareTo(that.getStartHour)
    }
    if (startMinute.compareTo(that.getStartMinute) != 0) {
      return startMinute.compareTo(that.getStartMinute)
    }
    if (stopHour.compareTo(that.getStopHour) != 0) {
      return stopHour.compareTo(that.getStopHour)
    }

    stopMinute.compareTo(that.getStopMinute)
  }


}

case class GeneratorForm(id: Long)