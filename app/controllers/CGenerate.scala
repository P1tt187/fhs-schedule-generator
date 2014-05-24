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
import logic.generator.schedulegenerator.{InplacebleSchedule, GenerateSchedule, ScheduleGeneratorActor, ScheduleAnswer}

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
import models.persistence.lecture.Lecture
import logic.generator.lecturegenerator.{LectureAnswer, GenerateLectures, LectureGeneratorActor}
import scala.collection.mutable.Buffer
import com.rits.cloning.{ObjenesisInstantiationStrategy, Cloner}

/**
 * @author fabian 
 *         on 20.03.14.
 */
object CGenerate extends Controller {

  private var schedule: Schedule = null

  private var startTime: Calendar = null

  private var finishTime: Calendar = null

  private var actorFinished = false

  private var end: Calendar = null

  private var hasError = false

  private var errorList: List[Lecture] = null

  private lazy val schedules = Buffer[Schedule]()

  private lazy val scheduleFutures = Buffer[Future[Any]]()

  val form: Form[GeneratorForm] = Form(
    mapping("id" -> longNumber,
      "threads" -> number(min = 1),
      "time" -> number(min = 0),
      "randomRatio" -> number(min = 0),
      "maxIterationDeep" -> number(min = 0)
    )(GeneratorForm.apply)(GeneratorForm.unapply)
  )


  val NAV = "GENERATOR"

  def loadScheduleForSemester(id: Long) = Action {
    implicit request =>
      val parts = request.session.get("lastchoosen").getOrElse("-1,2,10,10,50").split(",").toSeq

      Logger.debug("lastChoosen " + parts)
      schedule = findScheduleForSemester(findSemesterById(id))
      hasError = false
      actorFinished = true

      Redirect(routes.CGenerate.page()).withSession("lastchoosen" -> (Seq(id.toString) ++ parts.subList(1, parts.size)).mkString(","))
  }

  def page() = Action {
    implicit request =>

      var flashing = if (schedule != null) {
        flash +("startpolling", "true")
      } else {
        flash
      }

      flashing = if (end != null) {
        flashing +("endTime", end.getTimeInMillis.toString) +("startTime", startTime.getTimeInMillis.toString) +("startpolling", "true") +("generating", "disabled")
      } else {
        flashing
      }

      val chooseSemesterForm = request.session.get("lastchoosen") match {
        case Some(value) =>
          val parts = value.split(",")
          val idString = parts(0).toLong
          val threads = parts(1).toInt
          val time = parts(2).toInt
          val randomRatio = parts(3).toInt
          val maxIterationDeep = parts(4).toInt
          form.fill(GeneratorForm(idString, threads, time, randomRatio, maxIterationDeep))
        case None => form.fill(GeneratorForm(-1, 2, 10, 10, 50))
      }

      Ok(generator("Generator", findSemesters(), chooseSemesterForm, findCourses(), findDocents())(flashing))
  }

  def saveSchedule() = Action {
    Ok(Json.stringify(Json.obj("result" -> persistSchedule(schedule))))
  }

  def finished = Action {


    val json = Json.stringify(Json.obj("result" -> actorFinished, "error" -> hasError))
    Ok(json)
  }


  def sendSchedule(courseId: Long, docentId: Long, filterDuration: String) = Action {
    if (hasError) {
      Ok(Json.stringify(Json.obj("htmlresult" -> errorpage(errorList).toString())))
    } else {

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

      val filteredPage = if (courseId == -1 && docentId == -1 && filterDuration.equals("-1")) {
        showSchedule("Alle Kurse", timeRanges, timeslotsAll, schedule.getRate).toString()
      } else {
        val (courseName, timeslots) = filterScheduleWithCourseAndDocent(schedule, findCourse(courseId), findDocent(docentId), filterDuration)
        showSchedule(courseName, timeRanges, timeslots, schedule.getRate).toString()
      }
      Ok(Json.stringify(Json.obj("htmlresult" -> filteredPage)))
    }
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
          hasError = false
          schedule = null
          schedules.clear()
          scheduleFutures.clear()

          val subjects = findActiveSubjectsBySemesterId(result.id)
          val semester = findSemesterById(result.id)
          startTime = Calendar.getInstance()

          val cloner = new Cloner(new ObjenesisInstantiationStrategy)

          implicit val timeout = Timeout(result.time + 1 minutes)

          val lectureGeneratorActor = Akka.system.actorOf(Props[LectureGeneratorActor])

          val lectureFuture = lectureGeneratorActor ? GenerateLectures(subjects)

          val endTime = Calendar.getInstance()
          endTime.add(Calendar.MINUTE, result.time)
          lectureFuture.onSuccess {
            case LectureAnswer(lecturesAnswer) =>
              (1 to result.threads).foreach {
                i =>
                  val generatorActor = Akka.system.actorOf(Props[ScheduleGeneratorActor])

                  val scheduleFuture = ask(generatorActor, GenerateSchedule(cloner.deepClone(lecturesAnswer), semester, endTime, result.randomRatio, result.maxIterationDeep))
                  scheduleFutures += scheduleFuture

                  scheduleFuture.onSuccess {
                    case ScheduleAnswer(theSchedule) =>

                      schedules += theSchedule

                      val completetList = scheduleFutures.map(_.isCompleted).toSet
                      actorFinished = completetList.size == 1 && completetList.head

                      if (actorFinished) {
                        schedule = schedules.sortBy(_.getRate.toInt).head
                      }

                      finishTime = Calendar.getInstance()
                      end = null
                      Logger.debug("created in " + (finishTime.getTimeInMillis - startTime.getTimeInMillis) + "ms")

                    case InplacebleSchedule(lectures) => errorList = lectures
                      hasError = true
                      actorFinished = true
                  }

                  scheduleFuture.onFailure {
                    case e: Exception =>
                      generatorActor ! PoisonPill
                      end = null

                  }
              }
          }
          end = endTime
          //Logger.debug(findActiveSubjectsBySemesterId(result.id).mkString("\n") )

          Redirect(routes.CGenerate.page()).flashing("startpolling" -> "true", "generating" -> "disabled")
            .withSession("lastchoosen" -> Seq(result.id, result.threads, result.time, result.randomRatio, result.maxIterationDeep).mkString(","))
        }

      )


  }

}
