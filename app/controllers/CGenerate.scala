package controllers

import play.api.mvc._
import views.html.generator._
import play.api.libs.json._
import akka.util.Timeout
import scala.concurrent.duration._
import models.fhs.pages.generator.MGenerator._
import play.api.data._
import play.api.data.Forms._
import models.fhs.pages.generator.{TimeRange, GeneratorForm}
import scala.concurrent.Future
import logic.generator.schedulegenerator.{GenerateSchedule, ScheduleGeneratorActor, ScheduleAnswer}

import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import akka.pattern.ask
import models.persistence.Schedule
import play.api.Logger
import models.fhs.pages.JavaList
import models.persistence.scheduletree.{Timeslot, Weekday}
import scala.collection.JavaConversions._
import models.Transactions
import models.persistence.template.TimeslotTemplate
import org.hibernate.criterion.CriteriaSpecification

/**
 * @author fabian 
 *         on 20.03.14.
 */
object CGenerate extends Controller {

  private var scheduleFuture: Future[Any] = null

  private var schedule: Schedule = null

  val form: Form[GeneratorForm] = Form(
    mapping("id" -> longNumber)(GeneratorForm.apply)(GeneratorForm.unapply)
  )

  implicit val timeout = Timeout(5 seconds)

  val NAV = "GENERATOR"

  def page() = Action {
    implicit request =>

      Ok(generator("Generator", findSemesters(), form))
  }

  def finished = Action {
    val sb = StringBuilder.newBuilder
    sb.append("{\"result\":")
    if (scheduleFuture == null) {
      sb.append(false.toString)
    } else {
      sb.append(scheduleFuture.isCompleted.toString)
    }
    sb.append("}")
    Ok(sb.toString())
  }

  def sendSchedule = Action{
    val timeslots = if(schedule==null) {List[Timeslot]()} else {schedule.getRoot.getChildren.asInstanceOf[JavaList[Weekday]].flatMap(_.getChildren.asInstanceOf[JavaList[Timeslot]]).toList.sorted}
    val timeslotTemplates = Transactions.hibernateAction{
      implicit session =>
        session.createCriteria(classOf[TimeslotTemplate]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[TimeslotTemplate]].toList.sorted
    }

    Ok(Json.stringify(Json.obj("htmlresult"-> showSchedule(findTimeRanges(timeslotTemplates,List[TimeRange]()) ,timeslots).toString())))
  }

  def generatorAction = Action {
    implicit request =>

      val generatorFormResult = form.bindFromRequest()
      generatorFormResult.fold(
        errors => {
          BadRequest(generator("Generator", findSemesters(), errors))
        },
        result => {

          scheduleFuture = ask(Akka.system.actorOf(Props[ScheduleGeneratorActor]), GenerateSchedule(findActiveSubjectsBySemesterId(result.id)))
          scheduleFuture.onSuccess {
            case ScheduleAnswer(schedule) => this.schedule = schedule
            Logger.debug("" + this.schedule)
          }

          //Logger.debug(findActiveSubjectsBySemesterId(result.id).mkString("\n") )

          Redirect(routes.CGenerate.page()).flashing("startpolling" -> "true")
        }

      )


  }

}
