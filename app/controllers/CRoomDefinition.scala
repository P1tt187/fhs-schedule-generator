package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.fhs.pages.roomdefinition.{MTtimeslotCritDefine, MRoomdefintion}
import play.api.Logger
import models.persistence.criteria.CriteriaContainer
import java.util
import models._
import models.persistence.scheduletree.Weekday
import models.persistence.Room
import models.persistence.criteria.{AbstractCriteria, TimeslotCriteria}

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
      "pcpool" -> boolean,
      "beamer" -> boolean,
      "timeCriterias" -> list(mapping(
        "startHour" -> number(min = 0, max = 23),
        "startMinute" -> number(min = 0, max = 59),
        "stopHour" -> number(min = 0, max = 23),
        "stopMinute" -> number(min = 0, max = 59),
        "weekdays" -> list(number(min = 0, max = 6))
      )(MTtimeslotCritDefine.apply)(MTtimeslotCritDefine.unapply))

    )(MRoomdefintion.apply)(MRoomdefintion.unapply)
  )

  def page = Action {
    Ok(views.html.roomdefinition("Räume", roomDefForm, CTimeslotDefintion.WEEKDAYS))
  }

  def submitRoom = Action {
    implicit request =>

      val roomResult = roomDefForm.bindFromRequest

      roomResult.fold(
        errors => BadRequest(views.html.roomdefinition("Räume", errors, CTimeslotDefintion.WEEKDAYS)),
        room => {
          Logger.info(room.timeCriterias.toString)

          val roomDAO = new Room(room.capacity, room.house, room.number, room.pcpools, room.beamer)

          roomDAO.setCriteriaContainer(new CriteriaContainer)
          roomDAO.getCriteriaContainer.setCriterias(new util.LinkedList[AbstractCriteria]())

          room.timeCriterias foreach {
            crit =>
              crit.weekdays foreach {
                sortIndex =>
                  val dbResult = MRoomdefintion.findWeekdayBySortIndex(sortIndex)

                  val weekday = if (dbResult == null) {
                    val day = Weekday.createWeekdayFromSortIndex(sortIndex)
                    day
                  } else {
                    dbResult
                  }
                  val timeslotCriteria = new TimeslotCriteria(crit.startHour, crit.startMinutes, crit.stopHour, crit.stopMinutes, weekday)
                  timeslotCriteria.setPriority( 10)
                  timeslotCriteria.setTolerance( false)


                  roomDAO.getCriteriaContainer.getCriterias.add(timeslotCriteria)
              }
          }
          Transactions {
            implicit entitiManager =>
              entitiManager.persist(roomDAO)
          }

          Redirect(routes.CRoomDefinition.page)
        }
      )


  }

}
