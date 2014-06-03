package controllers

import play.api.mvc._
import play.api.libs.json._
import views.html.editschedule.{editschedule, showSchedule}
import models.fhs.pages.editschedule.MEditSchedule._
import models.Transactions
import models.persistence.template.TimeSlotTemplate
import org.hibernate.criterion.CriteriaSpecification
import models.fhs.pages._
import models.fhs.pages.generator.MGenerator._
import scala.collection.JavaConversions._
import models.persistence.scheduletree.{TimeSlot, Weekday}

/**
 * @author fabian 
 *         on 03.06.14.
 */
object CEditSchedule extends Controller {

  val NAV = "EDIT_SCHEDULE"

  def page()=Action{


    Ok(editschedule("Stundenplan bearbeiten",findSemestersWithSchedule))
  }


  def findAndSendSchedule(semesterId:Long)= Action{

    val timeslotTemplates = Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[TimeSlotTemplate]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[TimeSlotTemplate]].toList.sorted
    }

    val semester = findSemesterById(semesterId)

    val timeRanges = findTimeRanges(timeslotTemplates)
    val schedule = findScheduleForSemester(semester)

    val timeSlots = schedule.getRoot.getChildren.flatMap{
      case wd:Weekday=> wd.getChildren.toList.asInstanceOf[List[TimeSlot]]
    }.toList
    Ok(Json.obj("htmlresult" -> showSchedule("",timeRanges ,timeSlots).toString()))
  }
}
