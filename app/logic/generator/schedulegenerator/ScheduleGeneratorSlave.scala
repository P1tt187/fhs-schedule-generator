package logic.generator.schedulegenerator

import akka.actor.Actor
import models.persistence.scheduletree.{Timeslot, Weekday, Root}
import models.Transactions
import models.persistence.location.RoomEntity
import org.hibernate.criterion.CriteriaSpecification
import models.fhs.pages.JavaList
import scala.collection.JavaConversions._
import models.persistence.template.{TimeslotTemplate, WeekdayTemplate}


/**
 * @author fabian 
 *         on 23.03.14.
 */
class ScheduleGeneratorSlave extends Actor {

  override def receive = {

    case SlaveGenerate(lectures) =>

      val rooms = Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[RoomEntity]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[RoomEntity]].toList
      }

      val weekdays:List[Weekday] = Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[WeekdayTemplate]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[WeekdayTemplate]].toList
      }

      val root = new Root

      root.setChildren(weekdays.sortBy(_.getSortIndex))

      lectures.foreach{
        lecture =>

      }
    case _ =>
  }

  implicit def weekdayTemplateList2WeekdayList(wl: List[WeekdayTemplate]): List[Weekday] = {
    wl.map(weekdayTemplate2Weekday)
  }

  implicit def weekdayTemplate2Weekday(weekdayTemplate: WeekdayTemplate): Weekday = {
    val ret = new Weekday()

    ret.setName(weekdayTemplate.getName)
    ret.setSortIndex(weekdayTemplate.getSortIndex)
    ret.setChildren(weekdayTemplate.getChildren.map(timeslotTemplate2Timeslot(_, ret)).sorted)
    ret
  }

  def timeslotTemplate2Timeslot(timeslotTemplate: TimeslotTemplate, weekday: Weekday): Timeslot = {
    val ret = new Timeslot
    ret.setStartHour(timeslotTemplate.getStartHour)
    ret.setStartMinute(timeslotTemplate.getStartMinute)
    ret.setStopHour(timeslotTemplate.getStopHour)
    ret.setStopMinute(timeslotTemplate.getStopMinute)
    ret.setParent(weekday)
    ret
  }


}

