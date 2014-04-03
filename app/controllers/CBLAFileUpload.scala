package controllers

import play.api.mvc._
import views.html.blaimport._
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.Play.current
import scala.util.{Failure, Success}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import logic.blaimport.{ImportFinished, ImportFile, BlaImportActor}
import play.api.Logger

/**
 * @author fabian
 *         on 07.02.14.
 */
object CBLAFileUpload extends Controller {

  val NAV = "PLFILEUPLOAD"

  implicit val timeout = Timeout(30 seconds)
  private var result: Future[Any] = null
  private var actorFinished = false


  def page() = Action {
    implicit request =>
      Ok(blaimport("Import"))
  }

  def finished = Action {

    val sb = StringBuilder.newBuilder
    sb.append("{\"result\":")
    sb.append(actorFinished)
    sb.append("}")
    Ok(sb.toString())
  }

  def uploadFile = Action(parse.multipartFormData) {
    request =>
      request.body.file("fileUpload").map {
        file =>
          actorFinished = false
          import java.io.File
          val filename = file.filename
          val tmpFile = File.createTempFile(filename, "")

          tmpFile.deleteOnExit()
          file.ref.moveTo(tmpFile, replace = true)

          def finishAction(){
            result.onSuccess {
              case ImportFinished =>
                actorFinished = true
                Logger.debug("import finished")
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
}
