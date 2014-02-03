package controllers


import play.api.mvc._

import scala.collection.JavaConversions._
import models.fhs.pages.timeslot.MTimeslotDisplay
import models.persistence.scheduletree.Weekday
import models._
import play.db.jpa.Transactional
import play.api.Logger

/**
 * Created by fabian on 27.01.14.
 */
object CTimeslotDisplay extends Controller {

  val NAV = "timeslotdisplay"

  @Transactional(readOnly = true)
  def page() = Action {

    val timeslots= TIMESLOT_FINDER.findList()

    Logger.debug(timeslots.toString)

    val timeslotDisplay = timeslots.map {
      entry =>

        MTimeslotDisplay(entry.id ,entry.startHour, entry.startMinute, entry.stopHour, entry.stopMinute, entry.parent.asInstanceOf[Weekday].name, entry.parent.asInstanceOf[Weekday].sortIndex)
    }.sortWith(_ < _).toList


    Ok(views.html.timeslotdisplay("Zeitlots", timeslotDisplay))

  }

}
