package controllers

import play.api.mvc._
import play.api._

import play.api.data._
import play.api.data.Forms._

import play.db.jpa._

import models.fhs.pages.timeslot.MTimeslotDefine
import java.util
import models.persistence.scheduletree.{Weekday, Node, Timeslot}
import play.api.cache.Cached
import play.api.Play._
import com.avaje.ebean.Ebean

/**
 * Created by fabian on 23.01.14.
 */
object CTimeslotDefintion extends Controller {

  val NAV = "timeslotdefinition"

  val WEEKDAYS = Seq(("1","Montag"),("2","Dienstag"),("3","Mittwoch"),("4","Donnerstag"),("5","Freitag"),("6","Samstag"),("0","Sonntag"))

  val timeslotForm: Form[MTimeslotDefine] = Form(
    mapping(
      "startHour" -> number(min = 0, max = 23),
      "startMinute" -> number(min = 0, max = 59),
      "stopHour" -> number(min = 0, max = 23),
      "stopMinute" -> number(min = 0, max = 59),
      "weekdays" -> list(number(min=0,max=6))
    )(MTimeslotDefine.apply)(MTimeslotDefine.unapply)
  )

  @Transactional(readOnly = true)
  def page =
    Cached("CTIMESLOT") {
      Action {

        Ok(views.html.timeslotdefinition("Timeslots", List[String](), timeslotForm, WEEKDAYS))
        //Ok(views.html.index("test"))
      }
    }

  @Transactional
  def submit = Action {
    implicit request =>
      val timeslotResult = timeslotForm.bindFromRequest


      timeslotResult.fold(
        errors => {
          BadRequest(views.html.timeslotdefinition("Timeslots", List[String](), errors, WEEKDAYS))
        },
        timeslot => {

          Logger.debug("weekdays" + timeslot.weekdays)

          if (timeslot.weekdays.isEmpty) {
            BadRequest(views.html.timeslotdefinition("Timeslots", List("weekdays"), timeslotForm.fill(timeslot), WEEKDAYS))
          } else {

            timeslot.weekdays.foreach {
              sortIndex =>

                val day = Weekday.createWeekdayFromSortIndex(sortIndex)
                day.save()

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


  @Transactional
  def delete(id:Long)= Action {

   Ebean.delete (Ebean.find(classOf[Timeslot],id))

    Redirect(routes.CTimeslotDisplay.page)
  }

}
