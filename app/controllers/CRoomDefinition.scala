package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.fhs.pages.roomdefinition.{MTtimeslotCritDefine, MRoomdefintion}
import play.api.Logger
import models.persistence.Room
import models.persistence.criteria.{TimeslotCriteria, AbstractCriteria, CriteriaContainer}
import java.util
import play.db.ebean.Transactional
import models._
import scala.collection.JavaConversions._
import models.persistence.scheduletree.Weekday

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

  @Transactional
  def submitRoom = Action {
    implicit request =>

      val roomResult = roomDefForm.bindFromRequest

      roomResult.fold(
        errors => BadRequest(views.html.roomdefinition("Räume", errors, CTimeslotDefintion.WEEKDAYS)),
        room => {
          Logger.info(room.timeCriterias.toString)

          val roomDAO = new Room(room.capacity, room.house, room.number, room.pcpools, room.beamer)

          roomDAO.criteriaContainer = new CriteriaContainer
          roomDAO.criteriaContainer.criterias = new util.LinkedList[AbstractCriteria]()
          //roomDAO.criteriaContainer.save()
          //roomDAO.save()
          room.timeCriterias foreach {
            crit =>
              crit.weekdays foreach {
                sortIndex =>
                  val weekdayList = WEEKDAY_FINDER.where.eq("sortIndex", sortIndex).findList()

                  val filterList = weekdayList.filter(_.parent == null)

                  val weekday = if (filterList.isEmpty) {
                    val day = Weekday.createWeekdayFromSortIndex(sortIndex)
                    day.save()
                    day
                  } else {
                    filterList(0)
                  }
                  val timeslotCriteria = new TimeslotCriteria(crit.startHour, crit.startMinutes, crit.stopHour, crit.stopMinutes, weekday)
                  timeslotCriteria.priority = 10
                  timeslotCriteria.tolerance = false
                  //timeslotCriteria.save()

                  roomDAO.criteriaContainer.criterias.add(timeslotCriteria)
              }
          }
          roomDAO.save()
          Redirect(routes.CRoomDefinition.page)
        }
      )


  }

}
