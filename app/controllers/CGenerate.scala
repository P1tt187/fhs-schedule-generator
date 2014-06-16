package controllers

import java.util.{Calendar, UUID}

import akka.actor.{PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.rits.cloning.{Cloner, ObjenesisInstantiationStrategy}
import logic.generator.lecturegenerator.{GenerateLectures, LectureAnswer, LectureGeneratorActor}
import logic.generator.schedulegenerator.{GenerateSchedule, InplacebleSchedule, ScheduleAnswer, _}
import models.Transactions
import models.fhs.pages.JavaList
import models.fhs.pages.generator.GeneratorForm
import models.fhs.pages.generator.MGenerator._
import models.persistence.Schedule
import models.persistence.lecture.Lecture
import models.persistence.scheduletree.TimeSlot
import models.persistence.template.TimeSlotTemplate
import org.hibernate.criterion.CriteriaSpecification
import play.api.Logger
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._
import views.html.generator._

import scala.collection.JavaConversions._
import scala.collection.mutable.Buffer
import scala.concurrent.Future
import scala.concurrent.duration._

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

  private lazy val schedules = scala.collection.mutable.Map[UUID, Schedule]()

  private lazy val scheduleFutures = Buffer[Future[Any]]()

  private var errorMessage: String = null

  private var docentWishTimeError = false


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
      Logger.debug("" +(session + ("lastchoosen" -> (Seq(id.toString) ++ parts.subList(1, parts.size)).mkString(","))))
      schedule = findScheduleForSemester(findSemesterById(id))
      hasError = false
      actorFinished = true

      Redirect(routes.CGenerate.page()).withSession( session + ("lastchoosen" -> (Seq(id.toString) ++ parts.subList(1, parts.size)).mkString(",")))
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

      Ok(generator("Generator", findSemesters(), chooseSemesterForm, findCourses(), findDocents())(flashing,request.session))
  }

  def saveSchedule() = Action {
    Ok(Json.stringify(Json.obj("result" -> persistSchedule(schedule))))
  }

  def finished = Action {


    val json = Json.stringify(Json.obj("result" -> actorFinished, "error" -> hasError))
    Ok(json)
  }

  def switchSchedule(idString: String) = Action {
  implicit request=>
    schedule = schedules(schedules.keySet.find(_.toString.equals(idString)).get)
    Redirect(routes.CGenerate.page()).withSession(session + ("selectedSchedule" -> idString))
  }

  def sendSchedule(courseId: Long, docentId: Long, filterDuration: String) = Action {
    implicit request =>

      if (hasError) {
        if (docentWishTimeError) {
          Ok(Json.stringify(Json.obj("htmlresult" -> docentWishTimeErrorPage(errorMessage).toString())))
        } else {
          Ok(Json.stringify(Json.obj("htmlresult" -> errorpage(errorList).toString())))
        }
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

        val timeRanges = findTimeRanges(timeslotTemplates)

        val filteredPage = if (courseId == -1 && docentId == -1 && filterDuration.equals("-1")) {
          showSchedule("Alle Kurse", timeRanges, timeslotsAll, schedule.getRate, schedules.toMap).toString()
        } else {
          val (courseName, timeslots) = filterScheduleWithCourseAndDocent(schedule, findCourse(courseId), findDocent(docentId), filterDuration)
          showSchedule(courseName, timeRanges, timeslots, schedule.getRate, schedules.toMap).toString()
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
          docentWishTimeError = false
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

                      schedules += UUID.randomUUID() -> theSchedule

                      val completetList = scheduleFutures.map(_.isCompleted).toSet
                      actorFinished = completetList.size == 1 && completetList.head

                      if (actorFinished) {
                        schedule = schedules.values.toList.sortBy(s => (s.getRate.toInt, s.getRateSum)).head
                      }

                      finishTime = Calendar.getInstance()
                      end = null
                      Logger.debug("created in " + (finishTime.getTimeInMillis - startTime.getTimeInMillis) + "ms")

                    case InplacebleSchedule(lectures) => errorList = lectures
                      hasError = true
                      actorFinished = true
                    case TimeWishNotMatch(docents) =>
                      hasError = true
                      docentWishTimeError = true
                      errorMessage = docents.map( _.getLastName ).sorted.mkString(",")
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
            .withSession(session + ("lastchoosen" -> Seq(result.id, result.threads, result.time, result.randomRatio, result.maxIterationDeep).mkString(",")))
        }

      )


  }

}
