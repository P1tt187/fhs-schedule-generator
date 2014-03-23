package models.fhs.pages.roomdefinition

import models.Transactions
import org.hibernate.criterion.{CriteriaSpecification, Restrictions}
import models.persistence.template.WeekdayTemplate
import models.fhs.pages.timeslot.MTimeslotDisplay
import scala.collection.JavaConversions._
import models.persistence.criteria.TimeslotCriteria
import models.persistence.location.{HouseEntity, RoomAttributesEntity, RoomEntity}
import org.hibernate.FetchMode

/**
 * Created by fabian on 04.02.14.
 */
case class MRoomdefintion(capacity: Int, house: String, number: Int, attributes:List[String], timeCriterias: List[MTtimeslotCritDefine])

case class MRoomdisplay(id: Long, capacity: Int, house: String, number: Int, roomAttributes:List[RoomAttributesEntity], timeCriterias: List[MTimeslotDisplay])

object MRoomdefintion {
  /**
   * predefinded constants for the attribute
   */
  final val ATTRIBUTES: Array[String] = Array[String]("PC-Pool", "Beamer", "Whiteboard", "Blackboard","Overhead")


  def findOrCreateHouseEntityByName(name:String):HouseEntity={
    Transactions.hibernateAction{
      implicit session =>
        var result = session.createCriteria(classOf[HouseEntity]).add(Restrictions.eq("name",name)).setFetchMode("rooms",FetchMode.JOIN).uniqueResult().asInstanceOf[HouseEntity]
        if(result ==null){
         result = new HouseEntity(name)
          result.setRooms(new java.util.LinkedList[RoomEntity]())
          session.saveOrUpdate(result)
        }

        result
    }
  }

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
          case tcrit: TimeslotCriteria => MTimeslotDisplay(tcrit.getId, tcrit.getStartHour, tcrit.getStartMinute, tcrit.getStopHour, tcrit.getStartMinute, tcrit.getWeekday.getName, tcrit.getWeekday.getSortIndex)

        }
        MRoomdisplay(element.getId, element.getCapacity, element.getHouse.getName, element.getNumber, element.getRoomAttributes.toList , timeslotCrit.toList)
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
