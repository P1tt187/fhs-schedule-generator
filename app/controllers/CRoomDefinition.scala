package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.fhs.pages.roomdefinition.{MTtimeslotCritDefine, MRoomdefintion}
import play.api.Logger
import models.persistence.criteria.CriteriaContainer
import java.util
import models._
import models.persistence.criteria.{AbstractCriteria, TimeslotCriteria}
import models.persistence.enumerations.{EDuration, EPriority}
import models.persistence.location.{RoomAttributesEntity, RoomEntity}
import scala.collection.JavaConversions._
import views.html.roomdefinition._
import play.api.libs.json._


/**
 * Created by fabian on 04.02.14.
 */
object CRoomDefinition extends Controller {

  val NAV = "ROOMDEFINITION"

  val roomDefForm: Form[MRoomdefintion] = Form(
    mapping(
      "capacity" -> number,
      "house" -> nonEmptyText,
      "number" -> number,
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

    val rooms = MRoomdefintion.findAllRooms()

    rooms map {
      room =>
        Logger.debug(room.toString)
    }

    Ok(roomdefinition("Räume", roomDefForm, CTimeslotDefintion.WEEKDAYS, rooms))
  }

  def getCriteriaFields(index:Int) = Action{

    Ok(Json.stringify(Json.obj("htmlresult" -> timeslotcrit(index).toString())))

  }

  def submitRoom = Action {
    implicit request =>

      val roomResult = roomDefForm.bindFromRequest

      roomResult.fold(
        errors => BadRequest(roomdefinition("Räume", errors, CTimeslotDefintion.WEEKDAYS, MRoomdefintion.findAllRooms())),
        room => {
          Logger.info(room.timeCriterias.toString)

          val roomAttributes = room.attributes map (new RoomAttributesEntity(_))

          val houseDO = MRoomdefintion.findOrCreateHouseEntityByName(room.house)

          val roomDO = new RoomEntity(room.capacity, room.number, houseDO)
          Logger.debug("houseDO:" + houseDO)

          houseDO.getRooms.add(roomDO)
          roomDO.setRoomAttributes(roomAttributes)

          roomDO.setCriteriaContainer(new CriteriaContainer)
          roomDO.getCriteriaContainer.setCriterias(new util.LinkedList[AbstractCriteria]())

          room.timeCriterias foreach {
            crit =>
              crit.weekdays foreach {
                sortIndex =>

                  val weekday = MRoomdefintion.getWeekayTemplate(sortIndex)

                  val timeslotCriteria = new TimeslotCriteria(crit.startHour, crit.startMinutes, crit.stopHour, crit.stopMinutes, weekday, EDuration.valueOf(crit.duration))
                  timeslotCriteria.setPriority(EPriority.HIGH)
                  timeslotCriteria.setTolerance(false)


                  roomDO.getCriteriaContainer.getCriterias.add(timeslotCriteria)
              }
          }

          Logger.debug("" + houseDO + " " + roomDO)
          Transactions {
            implicit entitiManager =>
              entitiManager.persist(roomDO)
          }

          Redirect(routes.CRoomDefinition.page)
        }
      )


  }

}
