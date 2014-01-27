package controllers


import play.api.mvc._
import play.db.ebean.Transactional
import models.persistence.Timeslot
import scala.collection.JavaConversions._
import models._

/**
 * Created by fabian on 27.01.14.
 */
object CTimeslotDisplay extends Controller{

  val NAV="timeslotdisplay"

  @Transactional
  def page() = Action {


    Ok(views.html.timeslotdisplay("Timeslots", TIMESLOT_FINDER.all().toList))


  }

}
