package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.fhs.pages.roomdefinition.{MTtimeslotCritDefine, MRoomdefintion}

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
        "priority" -> number(min = 0, max = 10),
        "tolerance" -> boolean,
        "startHour" -> number(min = 0, max = 23),
        "startMinute" -> number(min = 0, max = 59),
        "stopHour" -> number(min = 0, max = 23),
        "stopMinute" -> number(min = 0, max = 59),
        "weekdays" -> list(nonEmptyText)
      )(MTtimeslotCritDefine.apply)(MTtimeslotCritDefine.unapply))

    )(MRoomdefintion.apply)(MRoomdefintion.unapply)
  )

  def page = Action {
    Ok(views.html.roomdefinition("Räume", roomDefForm))
  }

  def addRoom = Action {
    implicit request =>
      Ok(views.html.roomdefinition("Räume", roomDefForm))
  }

}
