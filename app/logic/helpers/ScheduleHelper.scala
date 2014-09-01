package logic.helpers

import com.rits.cloning.{Cloner, ObjenesisInstantiationStrategy}
import models.persistence.Schedule
import models.persistence.docents.Docent
import models.persistence.enumerations.EDuration
import models.persistence.lecture.Lecture
import models.persistence.location.RoomEntity
import models.persistence.participants.Course
import models.persistence.scheduletree._

import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 30.03.14.
 */
object ScheduleHelper {

  private lazy val cloner = new Cloner(new ObjenesisInstantiationStrategy)

  def filterCourse(schedule: Schedule, course: Course) = {

    val copyRoot = new Root
    initRoot(schedule, copyRoot)

    schedule.getRoot.getChildren.foreach {
      case weekday: Weekday =>
        val lectures = weekday.getChildren.map {
          case ts: TimeSlot =>

            val theLectures = ts.getLectures.filter {
              case lecture:Lecture =>
                !lecture.getLectureParticipants.find(_.getCourseName.equals(course.getShortName)).isEmpty
            }.map {
              case lecture: Lecture =>
                val copyLecture = cloner.deepClone(lecture)
                copyLecture.setName(lecture.getLectureSynonyms.getOrDefault(course.getShortName, lecture.getLectureSynonyms.getOrDefault("*",lecture.getName)))
                copyLecture
              case p => p
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

  private def initRoot(schedule: Schedule, copyRoot: Root) {
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
  }

  def filterDocent(schedule: Schedule, docent: Docent) = {
    val copyRoot = new Root
    initRoot(schedule, copyRoot)

    schedule.getRoot.getChildren.foreach {
      case weekday: Weekday =>
        val lectures = weekday.getChildren.map {
          case ts: TimeSlot =>

            val theLectures = ts.getLectures.filter {
              lecture =>
                lecture.getDocents.find(_.compareTo(docent) == 0).nonEmpty
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

  def filterRoom(schedule: Schedule, room: RoomEntity) = {
    val copyRoot = new Root
    initRoot(schedule, copyRoot)

    val lectureRoom = room.roomEntity2LectureRoom()

    schedule.getRoot.getChildren.foreach {
      case weekday: Weekday =>
        val lectures = weekday.getChildren.map {
          case ts: TimeSlot =>

            val theLectures = ts.getLectures.filter {
              lecture =>
                lecture.getRooms.find(_.compareTo(lectureRoom) == 0).nonEmpty
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

  def filterDuration(schedule: Schedule, duration: EDuration) = {
    val copyRoot = new Root
    initRoot(schedule, copyRoot)

    val copyWeekdays = copyRoot.getChildren.toList.asInstanceOf[List[Weekday]]

    schedule.getRoot.getChildren.foreach {
      case wd: Weekday =>
        wd.getChildren.foreach {
          case ts: TimeSlot =>

            val newTs = ts match {
              case _: EvenTimeSlot =>
                new EvenTimeSlot(ts.getStartHour, ts.getStartMinute, ts.getStopHour, ts.getStopMinute, ts.isUnpopular)
              case _: UnevenTimeSlot =>
                new UnevenTimeSlot(ts.getStartHour, ts.getStartMinute, ts.getStopHour, ts.getStopMinute, ts.isUnpopular)
            }
            newTs.setLectures(ts.getLectures)


            val copyDay = copyWeekdays.find(_.getSortIndex == wd.getSortIndex).get
            newTs.setParent(copyDay)
            if (ts.getDuration == duration) {
              copyDay.setChildren(copyDay.getChildren :+ newTs)
            }
        }
    }

    val copySchedule = new Schedule
    copySchedule.setRoot(copyRoot)
    copySchedule.setRate(schedule.getRate)
    copySchedule
  }

}
