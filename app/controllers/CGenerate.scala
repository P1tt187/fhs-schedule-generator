package controllers

import play.api.mvc._
import views.html.generator._
import akka.util.Timeout
import scala.concurrent.duration._
import models.fhs.pages.generator.MGenerator._
import play.api.data._
import play.api.data.Forms._
import models.fhs.pages.generator.GeneratorForm
import play.api.Logger
import scala.concurrent.Future
import logic.generator.schedulegenerator.{GenerateSchedule, ScheduleGeneratorActor, ScheduleAnswer}

import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Play.current
import scala.util.Failure
import play.api.libs.concurrent.Execution.Implicits._
import akka.pattern.ask

/**
 * @author fabian 
 *         on 20.03.14.
 */
object CGenerate extends Controller {

  private var scheduleFuture:Future[Any] = null

  val form: Form[GeneratorForm] = Form(
    mapping("id" -> longNumber)(GeneratorForm.apply)(GeneratorForm.unapply)
  )

  implicit val timeout = Timeout(5 seconds)

  val NAV = "GENERATOR"

  def page() = Action {
    implicit request =>
      Ok(generator("Generator", findSemesters(), form))
  }

  def generatorAction = Action{
    implicit request =>

    val generatorFormResult = form.bindFromRequest()
    generatorFormResult.fold(
    errors => {
      BadRequest(generator("Generator",findSemesters(),errors))
    },
    result => {

      scheduleFuture = ask(Akka.system.actorOf(Props[ScheduleGeneratorActor]), GenerateSchedule(findActiveSubjectsBySemesterId(result.id)))

      //Logger.debug(findActiveSubjectsBySemesterId(result.id).mkString("\n") )

      Redirect(routes.CGenerate.page())
    }

    )



  }

}
