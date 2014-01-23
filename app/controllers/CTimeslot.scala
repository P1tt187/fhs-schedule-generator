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
object CTimeslot extends Controller {

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
    Ok(views.html.timeslot.render("Timeslots",timeslotForm))
    //Ok(views.html.index("test"))
  }

  def submit = Action {
    implicit request=>
      timeslotForm.bindFromRequest.fold(
      errors => BadRequest(views.html.timeslot("Timeslots",errors)),
      timeslot => Ok(views.html.index("hat geklappt"))

      )
  }

}
