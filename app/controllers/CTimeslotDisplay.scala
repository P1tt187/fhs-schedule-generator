package controllers


import play.api.mvc._

import models.fhs.pages.timeslot.MTimeslotDisplay

/**
 * Created by fabian on 27.01.14.
 */
object CTimeslotDisplay extends Controller {

  val NAV = "timeslotdisplay"


  def page() = Action {

    Ok(views.html.timeslotdisplay("Zeitlots", MTimeslotDisplay.findAllTimeslots))

  }

}
