package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka
import logic.ImportFile
import play.api.Play.current
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import models.fhs.pages.editsubjects.MEditSubjects
import play.api.Logger

/**
 * @author fabian 
 *         on 05.03.14.
 */
object CEditSubjects extends Controller {

  val NAV = "EDITSUBJECTS"


  def page = Action {
    Ok(views.html.editsubjects.editsubjects("Fächer editieren", MEditSubjects.findSemesters()))
  }

  def getNamesField(semester: String, subjectType: String) = Action {
    Logger.debug("semester: " + semester + " subjectType: " + subjectType)
    Ok("")
  }

}
