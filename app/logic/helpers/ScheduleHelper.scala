package logic.helpers

import models.persistence.{Docent, Schedule}
import models.persistence.participants.Course
import models.persistence.scheduletree._
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 30.03.14.
 */
object ScheduleHelper {

  def filterCourse(schedule: Schedule, course: Course) = {

    val copyRoot = new Root
    copyRoot.setChildren(List[Node]())
    copyRoot.setChildren(copyRoot.getChildren ++ schedule.getRoot.getChildren.map {
      case w: Weekday =>
        val newDay = new Weekday()
        newDay.setName(w.getName)
        newDay.setSortIndex(w.getSortIndex)
        newDay.setChildren(List[Node]())
        newDay.setParent(copyRoot)

        newDay
    })

    schedule.getRoot.getChildren.foreach {
      case weekday: Weekday =>
        val lectures = weekday.getChildren.map {
          case ts: TimeSlot =>

            val theLectures = ts.getLectures.filter {
              lecture =>
                val courses = lecture.getParticipants.map(_.getCourse)
                lecture.getParticipants.contains(course) || courses.contains(course)
            }

            val newTs = ts match {
              case _: EvenTimeSlot =>
                new EvenTimeSlot(ts.getStartHour, ts.getStartMinute, ts.getStopHour, ts.getStopMinute, ts.isUnpopular)
              case _: UnevenTimeSlot =>
                new UnevenTimeSlot(ts.getStartHour, ts.getStartMinute, ts.getStopHour, ts.getStopMinute, ts.isUnpopular)
            }
            newTs.setLectures(theLectures)
            (weekday.getSortIndex, newTs)
        }

        lectures.foreach {
          case (sortIndex, ts) =>
            val copyDay = copyRoot.getChildren.filter {
              case day: Weekday => day.getSortIndex.equals(sortIndex)
            }.head
            ts.setParent(copyDay)
            copyDay.setChildren(copyDay.getChildren :+ ts)
        }

    }

    val copySchedule = new Schedule
    copySchedule.setRoot(copyRoot)
    copySchedule.setRate(schedule.getRate)
    copySchedule

  }

  def filterDocent(schedule: Schedule, docent: Docent) = {
    val copyRoot = new Root
    copyRoot.setChildren(List[Node]())
    copyRoot.setChildren(copyRoot.getChildren ++ schedule.getRoot.getChildren.map {
      case w: Weekday =>
        val newDay = new Weekday()
        newDay.setName(w.getName)
        newDay.setSortIndex(w.getSortIndex)
        newDay.setChildren(List[Node]())
        newDay.setParent(copyRoot)

        newDay
    })

    schedule.getRoot.getChildren.foreach {
      case weekday: Weekday =>
        val lectures = weekday.getChildren.map {
          case ts: TimeSlot =>

            val theLectures = ts.getLectures.filter {
              lecture =>
                lecture.getDocents.contains(docent)
            }

            val newTs = ts match {
              case _: EvenTimeSlot =>
                new EvenTimeSlot(ts.getStartHour, ts.getStartMinute, ts.getStopHour, ts.getStopMinute, ts.isUnpopular)
              case _: UnevenTimeSlot =>
                new UnevenTimeSlot(ts.getStartHour, ts.getStartMinute, ts.getStopHour, ts.getStopMinute, ts.isUnpopular)
            }
            newTs.setLectures(theLectures)
            (weekday.getSortIndex, newTs)
        }

        lectures.foreach {
          case (sortIndex, ts) =>
            val copyDay = copyRoot.getChildren.filter {
              case day: Weekday => day.getSortIndex.equals(sortIndex)
            }.head
            ts.setParent(copyDay)
            copyDay.setChildren(copyDay.getChildren :+ ts)
        }

    }

    val copySchedule = new Schedule
    copySchedule.setRoot(copyRoot)
    copySchedule.setRate(schedule.getRate)
    copySchedule
  }

}
