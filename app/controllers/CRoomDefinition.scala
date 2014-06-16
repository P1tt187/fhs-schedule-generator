package controllers

import java.util

import models._
import models.fhs.pages.roomdefinition.{MRoomdefintion, MTtimeslotCritDefine}
import models.persistence.criteria.{AbstractCriteria, CriteriaContainer, TimeSlotCriteria}
import models.persistence.enumerations.EDuration
import models.persistence.location.RoomEntity
import org.hibernate.criterion.Restrictions
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json._
import play.api.mvc._
import views.html.roomdefinition._

import scala.collection.JavaConversions._



/**
 * @author fabian
 *         on 04.02.14.
 */
object CRoomDefinition extends Controller {

  val NAV = "ROOMDEFINITION"

  val roomDefForm: Form[MRoomdefintion] = Form(
    mapping(
      "id" -> optional(longNumber),
      "capacity" -> number,
      "house" -> nonEmptyText,
      "number" -> nonEmptyText,
      "attributes" -> list(nonEmptyText),
      "timeCriterias" -> list(mapping(
        "startHour" -> number(min = 0, max = 23),
        "startMinute" -> number(min = 0, max = 59),
        "stopHour" -> number(min = 0, max = 23),
        "stopMinute" -> number(min = 0, max = 59),
        "weekdays" -> list(number(min = 0, max = 6)),
        "duration" -> text
      )(MTtimeslotCritDefine.apply)(MTtimeslotCritDefine.unapply))

    )(MRoomdefintion.apply)(MRoomdefintion.unapply)
  )

  def page = Action {
implicit request=>
    val rooms = MRoomdefintion.findAllRooms()

    Ok(roomdefinition("Räume", roomDefForm, CTimeslotDefintion.WEEKDAYS, rooms))
  }

  def getCriteriaFields(index: Int) = Action {

    Ok(Json.stringify(Json.obj("htmlresult" -> timeslotcrit(index).toString())))

  }


  def editRoom(id: Long) = Action {
    implicit request=>

    val room = MRoomdefintion.findMRoomDefinitionById(id)
    Logger.debug("edit room - " + room)

    val filledForm = roomDefForm.fill(room)
    Ok(roomdefinition("Räume", filledForm, CTimeslotDefintion.WEEKDAYS, MRoomdefintion.findAllRooms()))
  }

  def deleteRoom(id: Long) = Action {

    Transactions.hibernateAction {
      implicit session =>
        val room = session.createCriteria(classOf[RoomEntity]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[RoomEntity]
        room.getHouse.getRooms.remove(room)
        val house = room.getHouse
        room.setHouse(null)
        session.saveOrUpdate(house)
        session.delete(room)
    }

    Redirect(routes.CRoomDefinition.page())

  }

  def submitRoom = Action {
    implicit request =>

      //Logger.debug("submitRoom - " + request.body)
      val roomResult = roomDefForm.bindFromRequest

      roomResult.fold(
        errors => BadRequest(roomdefinition("Räume", errors, CTimeslotDefintion.WEEKDAYS, MRoomdefintion.findAllRooms())),
        room => {
          Logger.info("submit room - " + room.timeCriterias.toString)
          val roomAttributes = room.attributes map MRoomdefintion.findOrCreateRoomAttribute

          val houseDO = MRoomdefintion.findOrCreateHouseEntityByName(room.house)

          val roomDO = room.id match {
            case None => new RoomEntity(room.capacity, room.number, houseDO)
            case Some(id) => val roomEntity = MRoomdefintion.findRoomById(id)

              val criteriaContainer = roomEntity.getCriteriaContainer
              roomEntity.setCriteriaContainer(null)

              Transactions {
                implicit em =>
                  em.merge(roomEntity)
                  if (criteriaContainer != null) {
                    em.remove(em.merge(criteriaContainer))
                  }

              }
              roomEntity.setCapacity(room.capacity)
              roomEntity.setNumber(room.number)
              roomEntity.setHouse(houseDO)
              roomEntity
          }
          Logger.debug("houseDO:" + houseDO)

          houseDO.getRooms.add(roomDO)
          roomDO.setRoomAttributes(roomAttributes)

          roomDO.setCriteriaContainer(new CriteriaContainer)
          roomDO.getCriteriaContainer.setCriterias(new util.LinkedList[AbstractCriteria]())

          room.timeCriterias foreach {
            crit =>
              crit.weekdays foreach {
                sortIndex =>

                  val weekday = MRoomdefintion.getWeekdayTemplate(sortIndex)

                  val timeslotCriteria = new TimeSlotCriteria(crit.startHour, crit.startMinutes, crit.stopHour, crit.stopMinutes, weekday, EDuration.valueOf(crit.duration))


                  roomDO.getCriteriaContainer.getCriterias.add(timeslotCriteria)
              }
          }

          Logger.debug("" + houseDO + " " + roomDO)
          Transactions {
            implicit entitiManager =>

            /*  if (roomDO.getId == null) {
                entitiManager.persist(roomDO)
              } else {*/
                entitiManager.merge(roomDO)
              //}
          }

          Redirect(routes.CRoomDefinition.page)
        }
      )


  }

}
