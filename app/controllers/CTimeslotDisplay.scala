package controllers


import play.api.mvc._

import scala.collection.JavaConversions._
import models.fhs.pages.timeslot.MTimeslotDisplay
import models.persistence.scheduletree.Timeslot
import models._
import play.db.jpa.Transactional
import play.api.Logger
import models.persistence.template.TimeslotTemplate

/**
 * Created by fabian on 27.01.14.
 */
object CTimeslotDisplay extends Controller {

  val NAV = "timeslotdisplay"

  @Transactional(readOnly = true)
  def page() = Action {

    val timeslots = Transactions.hibernateAction{
      implicit session =>
        session.createCriteria(classOf[TimeslotTemplate]).list().asInstanceOf[java.util.List[TimeslotTemplate]]
    }

    Logger.debug(timeslots.toString)

    val timeslotDisplay = timeslots.map {
      entry =>
        MTimeslotDisplay(entry.getId, entry.getStartHour, entry.getStartMinute, entry.getStopHour, entry.getStopMinute, entry.getParent.getName, entry.getParent.getSortIndex)
    }.sortWith(_ < _).toList


    Ok(views.html.timeslotdisplay("Zeitlots", timeslotDisplay))

  }

}
