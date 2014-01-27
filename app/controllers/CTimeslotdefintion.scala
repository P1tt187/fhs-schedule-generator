package controllers

import play.api.mvc._
import play.api._

import play.api.data._
import play.api.data.Forms._
import scala.collection.JavaConversions._

import play.db.jpa._


import models.fhs.pages.timeslot.MTimeslot
import models.persistence.Timeslot


/**
 * Created by fabian on 23.01.14.
 */
object CTimeslotDefintion extends Controller {

  val NAV = "timeslotdefinition"

  val timeslotForm: Form[MTimeslot] = Form(
    mapping(
      "startHour" -> number(min = 0, max = 23),
      "startMinute" -> number(min = 0, max = 59),
      "stopHour" -> number(min = 0, max = 23),
      "stopMinute" -> number(min = 0, max = 59)
    )(MTimeslot.apply)(MTimeslot.unapply)
  )

  def page = Action {
    Ok(views.html.timeslotdefinition("Timeslots", timeslotForm))
    //Ok(views.html.index("test"))
  }

  @Transactional
  def submit = Action {
    implicit request =>
      val timeslot = timeslotForm.bindFromRequest

      timeslot.fold(
        errors => BadRequest(views.html.timeslotdefinition("Timeslots", errors)),
        timeslot => {


          val slot = new Timeslot
          slot.startHour = timeslot.startHour
          slot.startMinute = timeslot.startMinutes
          slot.stopHour = timeslot.stopHour
          slot.stopMinute = timeslot.stopMinutes

          slot.save()


          Redirect(routes.CTimeslotDisplay.page)
        }
      )
  }

}
