package models.fhs.pages.generator

import models.Transactions
import models.fhs.pages.JavaList
import models.fhs.pages.editdocents.MDocentTimeWhish
import models.fhs.pages.roomdefinition.MTtimeslotCritDefine
import models.persistence.docents.Docent
import models.persistence.enumerations.EDuration
import models.persistence.lecture.Lecture
import models.persistence.participants.Course
import models.persistence.scheduletree.{TimeSlot, Weekday}
import models.persistence.subject.AbstractSubject
import models.persistence.template.TimeSlotTemplate
import models.persistence.{Schedule, Semester}
import org.hibernate.FetchMode
import org.hibernate.criterion._
import play.api.Logger

import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 20.03.14.
 */
object MGenerator {

  def filterScheduleWithCourseAndDocent(schedule: Schedule, course: Course, docent: Docent, durationStr: String) = {

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

    filteredSchedule = if (!durationStr.equals("-1")) {
      val duration = EDuration.valueOf(durationStr)
      filteredSchedule.filter(duration)
    } else {
      filteredSchedule
    }

    (resultString.toString, collectTimeslotsFromSchedule(filteredSchedule))
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
  def findTimeRanges(timeslotTemplate: List[TimeSlotTemplate], timeRanges: List[TimeRange] = List[TimeRange]()): List[TimeRange] = {
    timeslotTemplate.headOption match {
      case None => timeRanges
      case Some(timeslot) =>
        val existingRange = timeRanges.find {
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


  def findScheduleForSemester(semester: Semester): Schedule = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Schedule]).add(Restrictions.eq("semester", semester)).uniqueResult().asInstanceOf[Schedule]
    }
  }

  def validateSchedule(schedule: Schedule) = {


    val allTimeSlots = schedule.getRoot.getChildren.flatMap {
      wd => wd.getChildren.map {
        case ts: TimeSlot => ts
      }
    }

    val invalidTimeSlots = allTimeSlots.par.filter {
      ts =>
        val docents = ts.getLectures.flatMap(_.getDocents).toSet
        val rooms = ts.getLectures.flatMap { case l: Lecture => l.getRooms}.toSet

        val docentCounts = docents.map {
          d =>
            ts.getLectures.count(_.getDocents.contains(d))
        }

        val roomCounts = rooms.map {
          r =>
            ts.getLectures.count { case l: Lecture => l.getRooms.contains(r)}
        }


        /** each docent can only one time in a timeslot*/
        val docentInvalid = docentCounts.find(_ > 1).nonEmpty
        /** each room can only one time in a timeslot */
        val roomInvalid = roomCounts.find(_ > 1).nonEmpty

        docentInvalid || roomInvalid
    }.toList

    invalidTimeSlots
  }

  def persistSchedule(schedule: Schedule): Boolean = {
    if (schedule == null) {
      return false
    }
    try {
      val oldSchedule = findScheduleForSemester(schedule.getSemester)
      Transactions.hibernateAction {
        implicit session =>

          if (oldSchedule != null) {
            session.saveOrUpdate(oldSchedule)
            session.delete(oldSchedule)
          }
          session.saveOrUpdate(schedule)
          true
      }
    } catch {
      case e: Exception =>
        Logger.error("cannot persist schedule", e)
        false
    }
  }

}


object TimeRange {

  implicit val TimeRangeOrdering = Ordering.by((range: TimeRange) => (range.startHour, range.startMinute, range.startHour, range.stopMinute))
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

  def compare(that: TimeSlotTemplate): Int = {
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

  def compare(that: MDocentTimeWhish): Int = {
    if (startHour.compareTo(that.startHour) != 0) {
      return startHour.compareTo(that.startHour)
    }
    if (startMinute.compareTo(that.startMinute) != 0) {
      return startMinute.compareTo(that.startMinute)
    }
    if (stopHour.compareTo(that.stopHour) != 0) {
      return stopHour.compareTo(that.stopHour)
    }

    stopMinute.compareTo(that.stopMinute)
  }

  def compare(that: MTtimeslotCritDefine): Int = {
    if (startHour.compareTo(that.startHour) != 0) {
      return startHour.compareTo(that.startHour)
    }
    if (startMinute.compareTo(that.startMinutes) != 0) {
      return startMinute.compareTo(that.startMinutes)
    }
    if (stopHour.compareTo(that.stopHour) != 0) {
      return stopHour.compareTo(that.stopHour)
    }

    stopMinute.compareTo(that.stopMinutes)
  }
}

case class GeneratorForm(id: Long, threads: Int, time: Int, randomRatio: Int, maxIterationDeep: Int)