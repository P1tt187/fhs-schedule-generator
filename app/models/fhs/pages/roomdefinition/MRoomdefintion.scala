package models.fhs.pages.roomdefinition

import models.Transactions
import org.hibernate.criterion.{CriteriaSpecification, Restrictions}
import models.persistence.template.WeekdayTemplate
import models.fhs.pages.timeslot.MTimeslotDisplay
import scala.collection.JavaConversions._
import models.persistence.criteria.TimeslotCriteria
import models.persistence.location.{HouseEntity, RoomAttributesEntity, RoomEntity}
import org.hibernate.FetchMode
import play.api.Play.current
/**
 * @author fabian
 *         on 04.02.14.
 */
case class MRoomdefintion(id: Option[Long], capacity: Int, house: String, number: String, attributes: List[String], timeCriterias: List[MTtimeslotCritDefine])

case class MRoomdisplay(id: Long, capacity: Int, house: String, number: String, roomAttributes: List[RoomAttributesEntity], timeCriterias: List[MTimeslotDisplay])

object MRoomdefintion {
  /**
   * predefinded constants for the attribute
   */
  //final val ATTRIBUTES: Array[String] = Array[String]("Seminar-Room", "PC-Pool", "Beamer", "Whiteboard", "Blackboard", "Overhead")
  lazy val ATTRIBUTES=current.configuration.getString("roomattributes").getOrElse("").split(",")


  def findOrCreateHouseEntityByName(name: String): HouseEntity = {
    Transactions.hibernateAction {
      implicit session =>
        var result = session.createCriteria(classOf[HouseEntity]).add(Restrictions.eq("name", name)).setFetchMode("rooms", FetchMode.JOIN).uniqueResult().asInstanceOf[HouseEntity]
        if (result == null) {
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

  def findMRoomDefinitionById(id: Long) = {
    Transactions.hibernateAction {
      implicit session =>
        val room = session.createCriteria(classOf[RoomEntity]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[RoomEntity]
        val attributes = room.getRoomAttributes.map(_.getAttribute).toList
        val criterias = room.getCriteriaContainer.getCriterias.map {
          case tcrit: TimeslotCriteria => MTtimeslotCritDefine(tcrit.getStartHour, tcrit.getStartMinute, tcrit.getStopHour, tcrit.getStopMinute, List(tcrit.getWeekday.getSortIndex), tcrit.getDuration.name)
        }.toList.sortBy(_.weekdays.sum)
        MRoomdefintion(Some(id), room.getCapacity, room.getHouse.getName, room.getNumber, attributes, criterias)
    }
  }

  def findRoomById(id: Long) = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[RoomEntity]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[RoomEntity]
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
          case tcrit: TimeslotCriteria => MTimeslotDisplay(tcrit.getId, tcrit.getStartHour, tcrit.getStartMinute, tcrit.getStopHour, tcrit.getStartMinute, tcrit.getWeekday.getName, tcrit.getWeekday.getSortIndex, tcrit.getDuration)
        }
        MRoomdisplay(element.getId, element.getCapacity, element.getHouse.getName, element.getNumber, element.getRoomAttributes.toList, timeslotCrit.toList.sortBy(_.weekdayIndex))
    }.sortBy(_.house)
  }

  def getWeekdayTemplate(sortIndex: Int): WeekdayTemplate = {
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

  def findOrCreateRoomAttribute(name: String) = {
    Transactions.hibernateAction {
      implicit session =>
        val roomAttr = session.createCriteria(classOf[RoomAttributesEntity]).add(Restrictions.eq("attribute", name)).uniqueResult().asInstanceOf[RoomAttributesEntity]
        if (roomAttr == null) {
          val ra = new RoomAttributesEntity()
          ra.setAttribute(name)
          ra
        } else {
          roomAttr
        }
    }
  }

}
