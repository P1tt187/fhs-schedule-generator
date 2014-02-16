package controllers

import play.api.mvc._
import views.html.blaimport._
import play.api.libs.concurrent.Akka
import akka.actor.Props
import logic.{ImportFile, BlaImportActor}
import play.api.Play.current
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import play.api.libs.concurrent.Execution.Implicits._


/**
 * Created by fabian on 07.02.14.
 */
object CBLAFileUpload extends Controller {

  val NAV = "PLFILEUPLOAD"


  def page() = Action { implicit request =>
    Ok(blaimport("Import"))
  }

  def uploadFile = Action(parse.multipartFormData) {
    request =>
      request.body.file("fileUpload").map {
        file =>
          import java.io.File
          val filename = file.filename
          val tmpFile = File.createTempFile(filename, "")

          tmpFile.deleteOnExit()
          file.ref.moveTo(tmpFile, replace = true)


          Akka.system.actorSelection("/user/" + IMPORT_ACTOR_NAME).resolveOne(300 millis).onComplete {
            case Success(actor) => actor ! ImportFile(tmpFile)

            case Failure(ex) => Akka.system.actorOf(Props[BlaImportActor], name = IMPORT_ACTOR_NAME) ! ImportFile(tmpFile)
          }
          Redirect(routes.CBLAFileUpload.page).flashing("success" -> "success")
      }.getOrElse(
          Redirect(routes.CBLAFileUpload.page).flashing("error" -> "Missing File")

        )
  }
}
