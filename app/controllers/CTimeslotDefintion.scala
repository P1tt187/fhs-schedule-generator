package controllers

import play.api.mvc._
import play.api._

import play.api.data._
import play.api.data.Forms._
import scala.collection.JavaConversions._

import play.db.jpa._

import models.fhs.pages.timeslot.MTimeslotDefine
import models._
import java.util
import models.persistence.scheduletree.{Node, Timeslot}
import play.api.cache.Cached
import play.api.Play._

/**
 * Created by fabian on 23.01.14.
 */
object CTimeslotDefintion extends Controller {

  val NAV = "timeslotdefinition"

  val timeslotForm: Form[MTimeslotDefine] = Form(
    mapping(
      "startHour" -> number(min = 0, max = 23),
      "startMinute" -> number(min = 0, max = 59),
      "stopHour" -> number(min = 0, max = 23),
      "stopMinute" -> number(min = 0, max = 59),
      "weekdays" -> list(nonEmptyText)
    )(MTimeslotDefine.apply)(MTimeslotDefine.unapply)
  )

  @Transactional(readOnly = true)
  def page =
    Cached("CTIMESLOT") {
      Action {
        val weekdays = WEEKDAY_FINDER.all().map(_.name).toList
        Ok(views.html.timeslotdefinition("Timeslots", List[String](), timeslotForm, weekdays))
        //Ok(views.html.index("test"))
      }
    }

  @Transactional
  def submit = Action {
    implicit request =>
      val timeslotResult = timeslotForm.bindFromRequest


      timeslotResult.fold(
        errors => {
          val weekdays = WEEKDAY_FINDER.all().map(_.name).toList
          BadRequest(views.html.timeslotdefinition("Timeslots", List[String](), errors, weekdays))
        },
        timeslot => {

          Logger.debug("weekdays" + timeslot.weekdays)

          if (timeslot.weekdays.isEmpty) {
            val weekdays = WEEKDAY_FINDER.all().map(_.name).toList
            BadRequest(views.html.timeslotdefinition("Timeslots", List("weekdays"), timeslotForm.fill(timeslot), weekdays))
          } else {

            timeslot.weekdays.foreach {
              dayname =>

                val day = WEEKDAY_FINDER.where().eq("name", dayname).findUnique()

                val slot = new Timeslot
                slot.startHour = timeslot.startHour
                slot.startMinute = timeslot.startMinutes
                slot.stopHour = timeslot.stopHour
                slot.stopMinute = timeslot.stopMinutes

                slot.children = new util.LinkedList[Node]()

                slot.parent = day
                day.children.add(slot)

                slot.save()
                day.update()

            }

            Redirect(routes.CTimeslotDisplay.page)
          }
        }
      )
  }

}
