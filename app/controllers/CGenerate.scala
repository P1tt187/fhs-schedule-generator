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
import akka.actor.{PoisonPill, Props}
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import akka.pattern.ask
import models.persistence.Schedule
import play.api.Logger
import models.fhs.pages.JavaList
import models.persistence.scheduletree.TimeSlot
import scala.collection.JavaConversions._
import models.Transactions
import models.persistence.template.TimeSlotTemplate
import org.hibernate.criterion.CriteriaSpecification
import java.util.Calendar

/**
 * @author fabian 
 *         on 20.03.14.
 */
object CGenerate extends Controller {

  private var scheduleFuture: Future[Any] = null

  private var schedule: Schedule = null

  private var startTime: Calendar = null

  private var finishTime: Calendar = null

  private var actorFinished = false

  private var rate = Int.MaxValue

  val form: Form[GeneratorForm] = Form(
    mapping("id" -> longNumber,
    "time"-> number(min=0)
    )(GeneratorForm.apply)(GeneratorForm.unapply)
  )



  val NAV = "GENERATOR"

  def page() = Action {
    implicit request =>

      val chooseSemesterForm = request.flash.get("lastchoosen") match {
        case Some(value) =>
          val parts = value.split(",")
          val idString = parts(0)
          val time = parts(1)
          form.fill(GeneratorForm(idString.toLong, time.toInt))
        case None => form
      }

      Ok(generator("Generator", findSemesters(), chooseSemesterForm, findCourses(), findDocents()))
  }

  def finished = Action {
    val sb = StringBuilder.newBuilder
    sb.append("{\"result\":")
    sb.append(actorFinished)
    sb.append("}")
    Ok(sb.toString())
  }


  def sendSchedule(courseId: Long, docentId:Long) = Action {
    val timeslotsAll = if (schedule == null) {
      List[TimeSlot]()
    } else {
      collectTimeslotsFromSchedule(schedule)
    }
    val timeslotTemplates = Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[TimeSlotTemplate]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[TimeSlotTemplate]].toList.sorted
    }

    val timeRanges = findTimeRanges(timeslotTemplates, List[TimeRange]())

    val filteredPage = if (courseId == -1 && docentId == -1) {
      showSchedule("Alle Kurse", timeRanges, timeslotsAll, rate).toString()
    } else {
      val (courseName, timeslots) = filterScheduleWithCourseAndDocent(schedule, findCourse(courseId), findDocent(docentId))
      showSchedule(courseName, timeRanges, timeslots, rate).toString()
    }
    Ok(Json.stringify(Json.obj("htmlresult" -> filteredPage)))
  }

  def generatorAction = Action {
    implicit request =>

      val generatorFormResult = form.bindFromRequest()
      generatorFormResult.fold(
        errors => {
          BadRequest(generator("Generator", findSemesters(), errors, findCourses(), findDocents()))
        },
        result => {
          actorFinished = false
          val subjects = findActiveSubjectsBySemesterId(result.id)
          val semester = findSemesterById(result.id)
          startTime = Calendar.getInstance()

          implicit val timeout = Timeout(result.time+1 minutes)

          val generatorActor = Akka.system.actorOf(Props[ScheduleGeneratorActor])
          val endTime= Calendar.getInstance()
          endTime.add(Calendar.MINUTE, result.time)
          scheduleFuture = ask(generatorActor, GenerateSchedule(subjects, semester, endTime))
          scheduleFuture.onSuccess {
            case ScheduleAnswer(theSchedule, theRate) => this.schedule = theSchedule
              rate = theRate
              actorFinished = true
              finishTime = Calendar.getInstance()
              Logger.debug("created in " + (finishTime.getTimeInMillis - startTime.getTimeInMillis) + "ms")
          }

          scheduleFuture.onFailure {
            case e: Exception =>
              generatorActor ! PoisonPill

          }

          //Logger.debug(findActiveSubjectsBySemesterId(result.id).mkString("\n") )

          Redirect(routes.CGenerate.page()).flashing("startpolling" -> "true", "lastchoosen" -> (result.id.toString + "," + result.time.toString) , "generating" -> "disabled")
        }

      )


  }

}
