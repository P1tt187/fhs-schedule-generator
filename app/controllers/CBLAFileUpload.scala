package controllers

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import controllers.traits.TController
import logic.blaimport.{BlaImportActor, ImportFailure, ImportFile, ImportFinished}
import models.fhs.pages.blaimport.{MBLAFileUpload, OldCourses}
import play.api.Logger
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._
import views.html.blaimport._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * @author fabian
 *         on 07.02.14.
 */
object CBLAFileUpload extends TController{

  val NAV = "PLFILEUPLOAD"

  implicit val timeout = Timeout(5 minutes)
  private var result: Future[Any] = null
  private var actorFinished = false
  private var error = false
  private var ex: Exception = null

  val removeCoursesForm = Form {
    mapping(
      "courseIds" -> list(longNumber(min = 0))
    )(OldCourses.apply)(OldCourses.unapply)
  }


  def page() = Action {
    implicit request =>
      Ok(blaimport("Import", MBLAFileUpload.findCourses()))
  }

  def finished = Action {

    val json = Json.stringify(Json.obj("result" -> actorFinished, "error" -> error))

    Ok(json)
  }

  def errorMessage = Action {
    Ok(Json.stringify(Json.obj("html" -> ex.toString)))
  }

  def uploadFile = Action(parse.multipartFormData) {
    request =>
      request.body.file("fileUpload").map {
        file =>
          actorFinished = false
          import java.io.File
          val filename = file.filename
          val tmpFile = File.createTempFile(filename, "")
          error = false

          tmpFile.deleteOnExit()
          file.ref.moveTo(tmpFile, replace = true)

          def finishAction() {
            result.onSuccess {
              case ImportFinished =>
                actorFinished = true
                Logger.debug("import finished")
              case ImportFailure(exception) =>
                error = true
                ex = exception
            }
          }

          Akka.system.actorSelection("/user/" + IMPORT_ACTOR_NAME).resolveOne(300 millis).onComplete {
            case Success(actor) => result = actor ? ImportFile(tmpFile)
              finishAction()
            case Failure(ex) => result = Akka.system.actorOf(Props[BlaImportActor], name = IMPORT_ACTOR_NAME) ? ImportFile(tmpFile)
              finishAction()
          }

          Redirect(routes.CBLAFileUpload.page).flashing("success" -> "success")
      }.getOrElse(
          Redirect(routes.CBLAFileUpload.page).flashing("error" -> "Missing File")
        )


  }

  def deleteOldCourse() = Action {
    implicit request =>

      val formData = removeCoursesForm.bindFromRequest()

      formData.fold(error => {
        Redirect(routes.CBLAFileUpload.page).flashing("error" -> "nothing selected")
      }, oldcourses => {
        MBLAFileUpload.removeOldCourses(oldcourses.courseIds)
        Redirect(routes.CBLAFileUpload.page).flashing("success" -> "success")
      })
  }

  def renameCourses() = Action {

    MBLAFileUpload.renameCourses()

    Redirect(routes.CBLAFileUpload.page)
  }
}
