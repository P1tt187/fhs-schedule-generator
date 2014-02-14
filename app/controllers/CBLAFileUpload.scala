package controllers

import play.api.mvc._
import views.html.blaimport._

/**
 * Created by fabian on 07.02.14.
 */
object CBLAFileUpload extends Controller {

  val NAV = "PLFILEUPLOAD"


  def page() = Action {
    Ok(blaimport("Import"))
  }

  def uploadFile = Action((parse.multipartFormData)) {
    request =>
      request.body.file("fileUpload").map {
        file =>
          import java.io.File
          val filename = file.filename
          val tmpFile = File.createTempFile(filename, "")

          if (tmpFile.exists()) {
            tmpFile.delete()
          }
          tmpFile.deleteOnExit()
          file.ref.moveTo(tmpFile)
          Ok("File Uploaded")
      }.getOrElse(
          Redirect(routes.CBLAFileUpload.page).flashing("error" -> "Missing File")

        )
  }
}
