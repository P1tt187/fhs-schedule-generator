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
import models.persistence.scheduletree.Timeslot
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
    val timeslotsAll = if(schedule==null) {List[Timeslot]()} else {collectTimeslotsFromSchedule(schedule)}
    val timeslotTemplates = Transactions.hibernateAction{
      implicit session =>
        session.createCriteria(classOf[TimeslotTemplate]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[TimeslotTemplate]].toList.sorted
    }

    val timeRanges = findTimeRanges(timeslotTemplates,List[TimeRange]())

     val filteredPages = filterScheduleWithCourses(schedule).sortBy(_._1).map{
       case (title,timeslots)=>
         showSchedule(title, timeRanges ,timeslots).toString()
     }.foldLeft("")(_ + _)

    Ok(Json.stringify(Json.obj("htmlresult"-> (showSchedule("Alle Kurse",timeRanges ,timeslotsAll).toString() + filteredPages))))
  }

  def generatorAction = Action {
    implicit request =>

      val generatorFormResult = form.bindFromRequest()
      generatorFormResult.fold(
        errors => {
          BadRequest(generator("Generator", findSemesters(), errors))
        },
        result => {

          scheduleFuture = ask(Akka.system.actorOf(Props[ScheduleGeneratorActor]), GenerateSchedule(findActiveSubjectsBySemesterId(result.id),findSemesterById(result.id)))
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
