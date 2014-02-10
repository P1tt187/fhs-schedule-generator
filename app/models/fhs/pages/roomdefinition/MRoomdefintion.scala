package models.fhs.pages.roomdefinition

import models.Transactions
import org.hibernate.criterion.{CriteriaSpecification, Restrictions}
import models.persistence.template.WeekdayTemplate
import models.fhs.pages.timeslot.MTimeslotDisplay
import scala.collection.JavaConversions._
import models.persistence.criteria.TimeslotCriteria
import models.persistence.location.RoomEntity

/**
 * Created by fabian on 04.02.14.
 */
case class MRoomdefintion(capacity: Int, house: String, number: Int, pcpools: Boolean, beamer: Boolean, timeCriterias: List[MTtimeslotCritDefine])

case class MRoomdisplay(id: Long, capacity: Int, house: String, number: Int, pcpools: Boolean, beamer: Boolean, timeCriterias: List[MTimeslotDisplay])

object MRoomdefintion {
  def findWeekdayBySortIndex(sortIndex: Int): WeekdayTemplate = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[WeekdayTemplate]).add(Restrictions.eq("sortIndex", sortIndex)).uniqueResult().asInstanceOf[WeekdayTemplate]
    }
  }

  def findAllRooms(): List[MRoomdisplay] = {
    val dbResult = Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[RoomEntity]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[java.util.List[RoomEntity]]
    }

    dbResult.toList.map {
      element =>
        val timeslotCrit = element.getCriteriaContainer.getCriterias map {
          crit =>
            crit match {
              case tcrit: TimeslotCriteria => MTimeslotDisplay(tcrit.getId, tcrit.getStartHour, tcrit.getStartMinute, tcrit.getStopHour, tcrit.getStartMinute, tcrit.getWeekday.getName, tcrit.getWeekday.getSortIndex)

            }
        }
        MRoomdisplay(element.getId, element.getCapacity, element.getHouse.getName, element.getNumber, element.getRoomAttributes.getPcpool, element.getRoomAttributes.getBeamer, timeslotCrit.toList)
    }
  }

  def getWeekayTemplate(sortIndex: Int): WeekdayTemplate = {
    val dbResult = MRoomdefintion.findWeekdayBySortIndex(sortIndex)

    if (dbResult == null) {
      val day = WeekdayTemplate.createWeekdayFromSortIndex(sortIndex)
      Transactions {
        implicit entitiManager =>
          entitiManager.persist(day)
      }

      day
    } else {
      dbResult
    }
  }

}
