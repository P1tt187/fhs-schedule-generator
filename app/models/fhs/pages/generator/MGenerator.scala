package models.fhs.pages.generator

import models.Transactions
import org.hibernate.criterion._
import scala.collection.JavaConversions._
import models.persistence.{Docent, Schedule, Semester}
import models.persistence.subject.AbstractSubject
import models.fhs.pages.JavaList
import models.persistence.template.TimeSlotTemplate
import scala.annotation.tailrec
import models.persistence.scheduletree.{Weekday, TimeSlot}
import models.persistence.participants.Course
import scala.Some
import org.hibernate.FetchMode


/**
 * @author fabian 
 *         on 20.03.14.
 */
object MGenerator {

  def filterScheduleWithCourseAndDocent(schedule: Schedule, course: Course, docent: Docent) = {

    val resultString = new StringBuffer()

    var filteredSchedule = if (course != null) {
      resultString.append(course.getShortName)
      resultString.append(" ")
      schedule.filter(course)
    } else {
      schedule
    }

    filteredSchedule = if (docent != null) {
      resultString.append(docent.getLastName)
      filteredSchedule.filter(docent)
    } else {
      filteredSchedule
    }
    (resultString.toString , collectTimeslotsFromSchedule(filteredSchedule))
  }

  def collectTimeslotsFromSchedule(schedule: Schedule) = {
    schedule.getRoot.getChildren.asInstanceOf[JavaList[Weekday]].flatMap(_.getChildren.asInstanceOf[JavaList[TimeSlot]]).toList.sorted
  }

  def findActiveSubjectsBySemesterId(id: Long) = {
    Transactions.hibernateAction {
      implicit session =>
        val criterion = session.createCriteria(classOf[AbstractSubject]).add(Restrictions.eq("active", true)).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
        criterion.createCriteria("semester").add(Restrictions.idEq(id))
        criterion.setFetchMode("criteriaContainer.house.rooms", FetchMode.JOIN)

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

  def findCourses(): List[Course] = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Course]).addOrder(Order.asc("shortName")).list().asInstanceOf[java.util.List[Course]].toList
    }
  }

  def findCourse(courseId: Long): Course = {
    if (courseId == -1l) {
      return null
    }
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Course]).add(Restrictions.idEq(courseId)).setFetchMode("groups", FetchMode.JOIN).uniqueResult().asInstanceOf[Course]
    }
  }


  @tailrec
  def findTimeRanges(timeslotTemplate: List[TimeSlotTemplate], timeRanges: List[TimeRange]): List[TimeRange] = {
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

  def findDocent(id: Long): Docent = {
    if (id == -1l) {
      return null
    }
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[Docent]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[Docent]
    }
  }

  def findDocents() = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Docent]).setFetchMode("criteriaContainer", FetchMode.SELECT).addOrder(Order.asc("lastName")).list().asInstanceOf[java.util.List[Docent]].toList
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

case class TimeRange(startHour: Int, startMinute: Int, stopHour: Int, stopMinute: Int) extends Ordered[TimeSlot] {


  override def toString = "" + startHour.formatted("%02d") + ":" + startMinute.formatted("%02d") + "-" + stopHour.formatted("%02d") + ":" + stopMinute.formatted("%02d")

  override def compare(that: TimeSlot): Int = {
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

case class GeneratorForm(id: Long, time:Int)