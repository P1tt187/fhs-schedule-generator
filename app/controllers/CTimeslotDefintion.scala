package controllers

import play.api.mvc._
import play.api._

import play.api.data._
import play.api.data.Forms._

import play.db.jpa._

import models.fhs.pages.timeslot.{MTimeslotDisplay, MTimeslotDefine}

import models.Transactions
import org.hibernate.criterion.Restrictions
import models.persistence.template.{TimeslotTemplate, WeekdayTemplate}

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
      Action {

        Ok(views.html.timeslotdefinition("Timeslots", List[String](), timeslotForm, WEEKDAYS,MTimeslotDisplay.findAllTimeslots))
        //Ok(views.html.index("test"))
      }


  @Transactional
  def submit = Action {
    implicit request =>
      val timeslotResult = timeslotForm.bindFromRequest


      timeslotResult.fold(
        errors => {
          BadRequest(views.html.timeslotdefinition("Timeslots", List[String](), errors, WEEKDAYS,MTimeslotDisplay.findAllTimeslots))
        },
        timeslot => {

          Logger.debug("weekdays" + timeslot.weekdays)

          if (timeslot.weekdays.isEmpty) {
            BadRequest(views.html.timeslotdefinition("Timeslots", List("weekdays"), timeslotForm.fill(timeslot), WEEKDAYS,MTimeslotDisplay.findAllTimeslots))
          } else {

            timeslot.weekdays.foreach {
              sortIndex =>

                val dbResult = Transactions.hibernateAction {
                  implicit session =>
                    session.createCriteria(classOf[WeekdayTemplate]).add(Restrictions.eq("sortIndex", sortIndex)).uniqueResult().asInstanceOf[WeekdayTemplate]
                }

                val day = if (dbResult == null) {
                  val theDay = WeekdayTemplate.createWeekdayFromSortIndex(sortIndex)
                  Transactions {
                    implicit emf =>
                      emf.persist(theDay)
                  }
                  theDay
                } else {
                  dbResult
                }


                val slot = new TimeslotTemplate(timeslot.startHour, timeslot.startMinutes, timeslot.stopHour, timeslot.stopMinutes, day)

                day.getChildren.add(slot)

                Transactions {
                  implicit entitiManager =>

                    entitiManager.persist(slot)
                    entitiManager.merge(day)

                }

            }

            Redirect(routes.CTimeslotDefintion.page)
          }
        }
      )
  }


  def delete(id: Long) = Action {

    Transactions.hibernateAction {
      implicit session =>
        val victom = session.createCriteria(classOf[TimeslotTemplate]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[TimeslotTemplate]
        victom.getParent.getChildren.remove(victom)
        session.saveOrUpdate(victom.getParent)
        Logger.debug("delete " + victom)
        session.delete(victom)
    }

    Redirect(routes.CTimeslotDefintion.page)
  }

}
