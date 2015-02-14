package controllers.traits

import models.fhs.pages.generator.MGenerator
import models.fhs.pages.timeslot.MTimeslotDisplay
import play.api.mvc._

/**
 * @author fabian
 * @since 13.02.15.
 *        Trait for central functions of every controller
 */
trait TController extends Controller  {

  val NAV:String

  /** landing metod of every controller */
  def page():Action[_]

  /** all timeslots and timeranges */
  def timeRange = {
    val allTimeSlots = MTimeslotDisplay.findAllTimeslots
    val timeRanges = MGenerator.findTimeRanges(allTimeSlots).sorted
    (allTimeSlots, timeRanges)
  }

}
