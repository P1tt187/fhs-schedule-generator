package logic.generator.schedulegenerator

import akka.actor.Actor
import models.persistence.scheduletree._
import models.Transactions
import models.persistence.location.RoomEntity
import org.hibernate.criterion.CriteriaSpecification
import models.fhs.pages.JavaList
import scala.collection.JavaConversions._
import models.persistence.template.{TimeSlotTemplate, WeekdayTemplate}
import models.persistence.Schedule
import models.persistence.participants.Course
import models.persistence.lecture.{Lecture, AbstractLecture}
import play.api.Logger
import models.persistence.criteria.RoomCriteria
import org.hibernate.FetchMode
import scala.concurrent.duration._
import akka.util.Timeout
import logic.generator.schedulegenerator.placingprocessor.GenericPlacer


/**
 * @author fabian 
 *         on 23.03.14.
 */
class ScheduleGeneratorSlave extends Actor {

  private var placed = 0

  private var notPlaced = 0

  private var noRoom = 0

  val TIMEOUT_VAL = 30

  implicit val timeout = Timeout(TIMEOUT_VAL seconds)

  private def filterRoomsWithCapacity(rooms: List[RoomEntity], lecture: Lecture) = rooms.par.filter(_.getCapacity >= lecture.calculateNumberOfParticipants()).toList

  override def receive = {

    case SlaveGenerate(lectures) =>
      try {
        placeLectures(lectures.sortBy(l => (-l.getDifficulty, l.getName)))
      }
      catch {
        case e: Exception => Logger.error("error", e)
      }

    case _ =>
  }

  private def placeLectures(lectures: List[Lecture]) {


    val rooms = Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[RoomEntity]).setCacheable(true).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).setFetchMode("house.rooms", FetchMode.JOIN).list().asInstanceOf[JavaList[RoomEntity]].toList
    }

    val weekdays: List[Weekday] = Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[WeekdayTemplate]).setCacheable(true).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[WeekdayTemplate]].toList.sortBy(_.getSortIndex)
    }

    val root = new Root

    root.setChildren(weekdays)

    val allTimeSlots = root.getChildren.flatMap(_.getChildren).toList.asInstanceOf[List[TimeSlot]]

   // doPlacing(lectures, allTimeSlots, rooms, root)


    val genericPlacer = new GenericPlacer(lectures, allTimeSlots, rooms)

    if(!genericPlacer.place()){
      sender() ! PlacingFailure
      return
    }


    if (notPlaced == 0) {
      val schedule = new Schedule
      Logger.debug("placed: " + placed + ", not placed: " + notPlaced + ", no room: " + noRoom)
      schedule.setRoot(root)
      sender() ! ScheduleAnswer(schedule)
    }

  }



  private implicit def weekdayTemplateList2WeekdayList(wl: List[WeekdayTemplate]): List[Weekday] = {
    wl.map(weekdayTemplate2Weekday)
  }

  private implicit def weekdayTemplate2Weekday(weekdayTemplate: WeekdayTemplate): Weekday = {
    val ret = new Weekday()

    ret.setName(weekdayTemplate.getName)
    ret.setSortIndex(weekdayTemplate.getSortIndex)
    ret.setChildren(weekdayTemplate.getChildren.flatMap(timeSlotTemplate2TimeSlot(_, ret)).sorted)
    ret
  }

  private def timeSlotTemplate2TimeSlot(timeslotTemplate: TimeSlotTemplate, weekday: Weekday): List[TimeSlot] = {
    def initTimeSlot(timeSlotTemplate: TimeSlotTemplate,ret:TimeSlot , weekday: Weekday) ={
      ret.setStartHour(timeSlotTemplate.getStartHour)
      ret.setStartMinute(timeSlotTemplate.getStartMinute)
      ret.setStopHour(timeSlotTemplate.getStopHour)
      ret.setStopMinute(timeSlotTemplate.getStopMinute)
      ret.setParent(weekday)
      ret.setLectures(List[AbstractLecture]())
      ret
    }

    List(initTimeSlot(timeslotTemplate, new EvenTimeSlot, weekday), initTimeSlot(timeslotTemplate, new UnevenTimeSlot, weekday))
  }




}

