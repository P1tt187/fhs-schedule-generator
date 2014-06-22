package models.fhs.pages.editdocents

import java.util.{Date, Calendar}

import models.Transactions
import models.fhs.pages.JavaList
import models.fhs.pages.roomdefinition.MRoomdefintion
import models.persistence.TimewishExpireDate
import models.persistence.criteria._
import models.persistence.docents.Docent
import models.persistence.enumerations.{EDocentTimeKind, EDuration}
import models.persistence.location.{HouseEntity, RoomEntity}
import models.persistence.subject.AbstractSubject
import models.persistence.template.WeekdayTemplate
import org.hibernate.FetchMode
import org.hibernate.criterion.{CriteriaSpecification, Restrictions}

import scala.collection.JavaConversions._


/**
 * @author fabian 
 *         on 13.04.14.
 */
object MEditDocents {
  def persistNewDocent(mDocent: MDocent) {
    val docent = new Docent

    docent.setLastName(mDocent.lastName)
    docent.setUserId(mDocent.userId)

    docent.setCriteriaContainer(new CriteriaContainer)

    Transactions {
      implicit em =>
        em.persist(docent)
    }
  }

  def removeDocent(id: Long): (String, List[String]) = {
    val docent = findDocentById(id)

    Transactions.hibernateAction {
      implicit s =>
        val criterion = s.createCriteria(classOf[AbstractSubject])
        criterion.createCriteria("docents").add(Restrictions.idEq(id))
        criterion.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
        val result = criterion.list().toList.asInstanceOf[List[AbstractSubject]]
        val connectedSubjects = result.map("<li>" + _.getName + "</li>")
        if (!connectedSubjects.isEmpty) {
          return (docent.getLastName, connectedSubjects)
        }
    }

    Transactions {
      implicit em =>
        val attachedObject = em.merge(docent)
        em.remove(attachedObject)
    }
    ("", List[String]())
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

  def findHouseById(id: Long) = {
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[HouseEntity]).setFetchMode("rooms", FetchMode.SELECT).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[HouseEntity]
    }
  }

  def findAllRooms() = {
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[RoomEntity]).setFetchMode("criteriaContainer", FetchMode.SELECT).setFetchMode("house.rooms", FetchMode.SELECT)
          .setFetchMode("roomAttributes", FetchMode.SELECT).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
          .list().toList.asInstanceOf[List[RoomEntity]]
    }
  }

  def findRoomById(id: Long) = {
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[RoomEntity]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[RoomEntity]
    }
  }

  def persistEditedDocent(mDocent: MExistingDocent) = {


    val docent = findDocentById(mDocent.id)

    docent.setLastName(mDocent.lastName)
    docent.setUserId(mDocent.userId)
    docent.setComments(mDocent.comments)
    val oldCriteriaContainer = docent.getCriteriaContainer
    val newCriteriaContainer = new CriteriaContainer
    newCriteriaContainer.setCriterias(List[AbstractCriteria]())

    docent.setCriteriaContainer(newCriteriaContainer)
    val timeSlotCriterias = mDocent.timeslots.map {
      crit =>
        def findOrCreateWeekdayTemplate(sortIndex: Int): WeekdayTemplate = {
          Transactions.hibernateAction {
            implicit s =>
              val result = s.createCriteria(classOf[WeekdayTemplate]).add(Restrictions.eq("sortIndex", sortIndex)).uniqueResult().asInstanceOf[WeekdayTemplate]
              result match {
                case null => val template = WeekdayTemplate.createWeekdayFromSortIndex(sortIndex)
                  s.save(template)
                  template
                case template: WeekdayTemplate => template
              }
          }
        }

        val timeCrit = new DocentTimeWish(crit.startHour, crit.startMinute, crit.stopHour, crit.stopMinute, findOrCreateWeekdayTemplate(crit.weekday), EDuration.valueOf(crit.duration), EDocentTimeKind.valueOf(crit.timeKind))

        timeCrit
    }
    newCriteriaContainer.setCriterias(newCriteriaContainer.getCriterias ++ timeSlotCriterias)

    val houseCriterias = mDocent.houseCriterias.map {
      crit =>
        val house = findHouseById(crit)
        val houseCrit = new RoomCriteria
        houseCrit.setHouse(house)
        houseCrit
    }
    newCriteriaContainer.setCriterias(newCriteriaContainer.getCriterias ++ houseCriterias)

    if (!mDocent.roomAttr.isEmpty) {


      val attributes = mDocent.roomAttr.map {
        attribute =>
          val roomCriteria = new RoomCriteria
          val attr=  MRoomdefintion.findOrCreateRoomAttribute(attribute)
          roomCriteria.setRoomAttributes(List(attr))
          roomCriteria
      }



      newCriteriaContainer.setCriterias(newCriteriaContainer.getCriterias ++ attributes)
    }
    val roomCriterias = mDocent.roomCrit.map {
      crit =>
        val roomCrit = new RoomCriteria
        roomCrit.setRoom(findRoomById(crit))

        roomCrit
    }
    newCriteriaContainer.setCriterias(newCriteriaContainer.getCriterias ++ roomCriterias)
    Transactions {
      implicit em =>
        em.merge(docent)
        val attachedObject = em.merge(oldCriteriaContainer)
        em.remove(attachedObject)
    }
    docent
  }

  implicit def docent2MExistingDocent(docent: Docent) = {
    val timeslotCriterias = docent.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[DocentTimeWish]).toList.asInstanceOf[List[DocentTimeWish]]
    val convertedTimeslotCriterias = timeslotCriterias.map {
      tcrit =>
        MDocentTimeWhish(tcrit.getTimeKind.name(), tcrit.getDuration.name(), tcrit.getWeekday.getSortIndex, tcrit.getStartHour, tcrit.getStartMinute, tcrit.getStopHour, tcrit.getStopMinute)
    }.sortBy(crit=> (crit.timeKind.length, crit.weekday,crit.startHour,crit.startMinute,crit.stopHour,crit.stopMinute))

    val roomCriterias = docent.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[RoomCriteria]).toList.asInstanceOf[List[RoomCriteria]]
    val houseCriterias = roomCriterias.filter(_.getHouse != null).map {
      hCrit =>
        hCrit.getHouse.getId.toLong
    }

    val roomAttributes = roomCriterias.filter(raCrit => raCrit.getRoomAttributes != null && !raCrit.getRoomAttributes.isEmpty).flatMap {
      raCrit =>
        raCrit.getRoomAttributes.map {
          roomAttr =>
            roomAttr.getAttribute
        }
    }

    val roomCrits = roomCriterias.filter(_.getRoom != null).map {
      rCrit =>
        rCrit.getRoom.getId.toLong
    }

    MExistingDocent(docent.getId, docent.getLastName,docent.getUserId,docent.getComments, convertedTimeslotCriterias, houseCriterias, roomAttributes, roomCrits)
  }

  def findExpireDate()={
    Transactions.hibernateAction{
      implicit s=>
        s.createCriteria(classOf[TimewishExpireDate]).uniqueResult().asInstanceOf[TimewishExpireDate]
    }
  }

  def persistExpireDate(ted:TimewishExpireDate){
    val oldExpireDate = findExpireDate()
    Transactions.hibernateAction{
      implicit s=>
        if(oldExpireDate!=null){
          s.saveOrUpdate(oldExpireDate)
          s.delete(oldExpireDate)
        }
        s.saveOrUpdate(ted)
    }
  }

  implicit def timeWishExpireDate2MExpireDate(ted:TimewishExpireDate):MExpireDate={
    MExpireDate(ted.getExpiredate.getTime)
  }

  implicit def mExpireDate2TimewishExpireDate(med:MExpireDate):TimewishExpireDate={
    val result = new TimewishExpireDate
    val date = Calendar.getInstance()
    date.setTime(med.date)
    result.setExpiredate(date)
    result
  }
}

case class MDocent(lastName: String, userId:String)

case class MExpireDate(date:Date)

case class MExistingDocent(id: Long, lastName: String,userId:String,comments:String, timeslots: List[MDocentTimeWhish], houseCriterias: List[Long], roomAttr: List[String], roomCrit: List[Long])

case class MDocentTimeWhish(timeKind: String, duration:String, weekday: Int, startHour: Int, startMinute: Int, stopHour: Int, stopMinute: Int)


