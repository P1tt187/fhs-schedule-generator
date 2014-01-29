package controllers


import play.api.mvc._
import play.db.ebean.Transactional
import scala.collection.JavaConversions._
import models.fhs.pages.timeslot.MTimeslotDisplay
import com.avaje.ebean.Ebean
import models.persistence.scheduletree.{Timeslot, Weekday}

/**
 * Created by fabian on 27.01.14.
 */
object CTimeslotDisplay extends Controller {

  val NAV = "timeslotdisplay"

  @Transactional
  def page() = Action {


    val timeslotDisplay = Ebean.find(classOf[Timeslot]).fetch("parent").findList().map {
      entry =>

        MTimeslotDisplay(entry.startHour, entry.startMinute, entry.stopHour, entry.stopMinute, entry.parent.asInstanceOf[Weekday].name, entry.parent.asInstanceOf[Weekday].sortIndex)
    }.sortWith(_ < _).toList


    Ok(views.html.timeslotdisplay("Timeslots", timeslotDisplay))


  }

}
