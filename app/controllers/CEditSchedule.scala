package controllers

import play.api.mvc._
import play.api.libs.json._
import views.html.editschedule.{editschedule, showSchedule}
import models.fhs.pages.editschedule.MEditSchedule._
import models.Transactions
import models.persistence.template.TimeSlotTemplate
import org.hibernate.criterion.{Restrictions, CriteriaSpecification}
import models.fhs.pages._
import models.fhs.pages.generator.MGenerator._
import scala.collection.JavaConversions._
import models.persistence.scheduletree.{TimeSlot, Weekday}
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.duration._
import com.rits.cloning.{ObjenesisInstantiationStrategy, Cloner}
import models.persistence.lecture.{Lecture, AbstractLecture}
import models.persistence.enumerations.EDuration
import models.persistence.location.RoomEntity
import models.fhs.pages.generator.TimeRange

/**
 * @author fabian 
 *         on 03.06.14.
 */
object CEditSchedule extends Controller {

  val NAV = "EDIT_SCHEDULE"

  lazy val TIME_TO_LIFE = 10 minutes

  def page() = Action {
    implicit request =>
      Ok(editschedule("Stundenplan bearbeiten", findSemestersWithSchedule))
  }


  def findAndSendSchedule(semesterId: Long) = Action {

    val timeslotTemplates = Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[TimeSlotTemplate]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[TimeSlotTemplate]].toList.sorted
    }

    val rooms = Cache.getOrElse("rooms") {
      val room = findAllRooms
      Cache.set("rooms", room, expiration = TIME_TO_LIFE)
      room
    }

    val semester = findSemesterById(semesterId)

    val timeRanges = findTimeRanges(timeslotTemplates)
    val schedule = findScheduleForSemester(semester)

    val timeSlots = schedule.getRoot.getChildren.flatMap {
      case wd: Weekday => wd.getChildren.toList.asInstanceOf[List[TimeSlot]]
    }.toList
    Ok(Json.obj("htmlresult" -> showSchedule("", timeRanges, timeSlots, rooms, semesterId).toString())).withSession("editschedule" -> semesterId.toString)
  }

  def saveEditedSchedule = Action(parse.json) {
    implicit request =>
      val cloner = new Cloner(new ObjenesisInstantiationStrategy)
      val jsVal = request.body
      val semester = findSemesterById((jsVal \ "semester").as[Long])
      val oldSchedule = findScheduleForSemester(semester)

      val newSchedule = cloner.deepClone(oldSchedule)
      newSchedule.setId(null)
      newSchedule.getRoot.setId(null)
      newSchedule.getRoot.getChildren.par.foreach { child => child.setId(null)
        child.getChildren.foreach {
          case ts: TimeSlot =>
            ts.setId(null)
            ts.setLectures(List[AbstractLecture]())
        }
      }

      val newScheduleTimeSlots = newSchedule.getRoot.getChildren.par.flatMap {
        case wd: Weekday =>
          wd.getChildren.toList.asInstanceOf[List[TimeSlot]]
      }


      (jsVal \ "lectures").as[JsArray].value.foreach {
        jsElement =>

          val startHour = (jsElement \ "startHour").as[Int]
          val startMinute = (jsElement \ "startMinute").as[Int]
          val stopHour = (jsElement \ "stopHour").as[Int]
          val stopMinute = (jsElement \ "stopMinute").as[Int]
          val day = (jsElement \ "day").as[Int]

          val lectureId = (jsElement \ "lectureId").as[String].toLong
          val duration = EDuration.valueOf((jsElement \ "duration").as[String])
          val roomId = (jsElement \ "room").as[String].toLong

          val lecture = Transactions.hibernateAction {
            implicit s =>
              s.createCriteria(classOf[Lecture]).add(Restrictions.idEq(lectureId)).uniqueResult().asInstanceOf[Lecture]
          }
          val room = Transactions.hibernateAction {
            implicit s =>
              s.createCriteria(classOf[RoomEntity]).add(Restrictions.idEq(roomId)).uniqueResult().asInstanceOf[RoomEntity]
          }
          lecture.setId(null)
          lecture.setDuration(duration)
          lecture.setRoom(room)

          val timeRange = TimeRange(startHour, startMinute, stopHour, stopMinute)

          duration match {
            case EDuration.WEEKLY =>
              val timeSlots = newScheduleTimeSlots.filter(slot => timeRange.compare(slot) == 0 && slot.getParent.asInstanceOf[Weekday].getSortIndex.toInt == day)
              timeSlots.foreach {
                ts => ts.setLectures(ts.getLectures :+ lecture)
              }
            case _ =>
              val timeSlot = newScheduleTimeSlots.filter(slot => timeRange.compare(slot) == 0 && slot.getParent.asInstanceOf[Weekday].getSortIndex.toInt == day && slot.getDuration == duration).head
              timeSlot.setLectures(timeSlot.getLectures :+ lecture)
          }
      }




      persistSchedule(newSchedule)

      Ok(Json.obj("restult" -> "Ok"))
  }
}