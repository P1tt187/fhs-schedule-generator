package models.fhs.pages.editdocents

import models.persistence.Docent
import models.persistence.criteria.{RoomCriteria, TimeslotCriteria, CriteriaContainer}
import models.Transactions
import org.hibernate.criterion.{Restrictions, CriteriaSpecification}
import org.hibernate.FetchMode
import models.fhs.pages.JavaList
import scala.collection.JavaConversions._
import models.persistence.location.HouseEntity

/**
 * @author fabian 
 *         on 13.04.14.
 */
object MEditDocents {
  def persistNewDocent(mDocent: MDocent) {
    val docent = new Docent

    docent.setLastName(mDocent.lastName)

    docent.setCriteriaContainer(new CriteriaContainer)

    Transactions {
      implicit em =>
        em.persist(docent)
    }
  }

  def findAllDocents() = {
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[Docent]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).setFetchMode("criteriaContainer", FetchMode.SELECT).list().asInstanceOf[JavaList[Docent]].toList.sorted
    }
  }

  def findDocentById(id: Long) = {
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[Docent]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[Docent]
    }
  }

  def findHouses(): List[HouseEntity] = {
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[HouseEntity]).setFetchMode("rooms", FetchMode.SELECT).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[HouseEntity]].toList
    }
  }

  implicit def docent2MExistingDocent(docent: Docent) = {
    val timeslotCriterias = docent.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[TimeslotCriteria]).toList.asInstanceOf[List[TimeslotCriteria]]
    val convertedTimeslotCriterias = timeslotCriterias.map {
      tcrit =>
        MTimeslotCriteria(tcrit.isTolerance, tcrit.getWeekday.getSortIndex, tcrit.getStartHour, tcrit.getStartMinute, tcrit.getStopHour, tcrit.getStopMinute)
    }

    val roomCriterias = docent.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[RoomCriteria]).toList.asInstanceOf[List[RoomCriteria]]
    val houseCriterias = roomCriterias.filter(_.getHouse != null).map {
      hCrit =>
        MHouseCriteria(hCrit.getHouse.getName)
    }

    val roomAttributes = roomCriterias.filter(raCrit => raCrit.getRoomAttributes != null && !raCrit.getRoomAttributes.isEmpty).flatMap {
      raCrit =>
        raCrit.getRoomAttributes.map {
          roomAttr =>
            MRoomAttribute(roomAttr.getAttribute)
        }
    }

    val roomCrits = roomCriterias.filter(_.getRoom != null).map {
      rCrit =>
        MRoomCriteria(rCrit.getRoom.getHouse.getName, rCrit.getRoom.getNumber)
    }

    MExistingDocent(docent.getId, docent.getLastName, convertedTimeslotCriterias, houseCriterias, roomAttributes, roomCrits)
  }
}

case class MDocent(lastName: String)

case class MExistingDocent(id: Long, lastName: String, timeslots: List[MTimeslotCriteria], houseCriterias: List[MHouseCriteria], roomAttr: List[MRoomAttribute], roomCrit: List[MRoomCriteria])

case class MTimeslotCriteria(tolerant: Boolean, weekday: Int, startHour: Int, startMinute: Int, stopHour: Int, stopMinute: Int)

case class MHouseCriteria(name: String)

case class MRoomAttribute(name: String)

case class MRoomCriteria(houseName: String, number: String)
