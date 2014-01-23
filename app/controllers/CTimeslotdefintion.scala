package controllers

import play.api.mvc._
import play.api._

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import models.fhs.pages.timeslot.MTimeslot

/**
 * Created by fabian on 23.01.14.
 */
object CTimeslotdefintion extends Controller {

  val timeslotForm: Form[MTimeslot] = Form(
    mapping(
      "startHour" -> number(min = 0, max = 23),
      "startMinute" -> number(min = 0, max = 59),
      "stopHour" -> number(min = 0, max = 23),
      "stopMinute" -> number(min = 0, max = 59),
      "tolerance" ->  boolean
    )(MTimeslot.apply)(MTimeslot.unapply)
  )

  def page = Action {
    Ok(views.html.timeslotdefinition("Timeslots",timeslotForm))
    //Ok(views.html.index("test"))
  }

  def submit = Action {
    implicit request=>
      val timeslot= timeslotForm.bindFromRequest

      timeslot.fold(
      errors => BadRequest(views.html.timeslotdefinition("Timeslots",errors)),
      timeslot => Ok(views.html.timeslotdisplay("hat geklappt",List(timeslot)))

      )
  }

}
