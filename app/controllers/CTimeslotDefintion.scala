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

import models.Transactions
import org.hibernate.criterion.Restrictions
import play.api.Play.current

/**
 * Created by fabian on 23.01.14.
 */
object CTimeslotDefintion extends Controller {

  val NAV = "timeslotdefinition"

  val WEEKDAYS = Seq(("1", "Montag"), ("2", "Dienstag"), ("3", "Mittwoch"), ("4", "Donnerstag"), ("5", "Freitag"), ("6", "Samstag"), ("0", "Sonntag"))

  val timeslotForm: Form[MTimeslotDefine] = Form(
    mapping(
      "startHour" -> number(min = 0, max = 23),
      "startMinute" -> number(min = 0, max = 59),
      "stopHour" -> number(min = 0, max = 23),
      "stopMinute" -> number(min = 0, max = 59),
      "weekdays" -> list(number(min = 0, max = 6))
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


                val slot = new Timeslot(timeslot.startHour, timeslot.startMinutes, timeslot.stopHour, timeslot.stopMinutes)


                slot.setChildren(new util.LinkedList[Node]())

                slot.setParent(day)
                day.getChildren.add(slot)

                Transactions {
                  implicit entitiManager =>

                    entitiManager.persist(slot)
                    entitiManager.persist(day)

                }

            }

            Redirect(routes.CTimeslotDisplay.page)
          }
        }
      )
  }


  def delete(id: Long) = Action {

    Transactions.hibernateAction {
      implicit session =>
        val victom = session.createCriteria(classOf[Timeslot]).add(Restrictions.idEq(id)).uniqueResult()
        session.delete(victom)
    }

    Redirect(routes.CTimeslotDisplay.page)
  }

}
