package controllers


import play.api.mvc._
import play.db.ebean.Transactional
import scala.collection.JavaConversions._
import models.fhs.pages.timeslot.MTimeslotDisplay
import models.persistence.scheduletree.Weekday
import models._

/**
 * Created by fabian on 27.01.14.
 */
object CTimeslotDisplay extends Controller {

  val NAV = "timeslotdisplay"

  @Transactional
  def page() = Action {


    val timeslotDisplay = TIMESLOT_FINDER.findList().map {
      entry =>

        MTimeslotDisplay(entry.startHour, entry.startMinute, entry.stopHour, entry.stopMinute, entry.parent.asInstanceOf[Weekday].name, entry.parent.asInstanceOf[Weekday].sortIndex)
    }.sortWith(_ < _).toList


    Ok(views.html.timeslotdisplay("Zeitlots", timeslotDisplay))


  }

}
