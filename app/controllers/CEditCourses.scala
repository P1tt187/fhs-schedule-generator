package controllers

import play.api.mvc._
import views.html.editcourses._
import models.fhs.pages.editcourses.MEditCourses._
import play.api.libs.json._
import play.api.Logger

/**
 * @author fabian 
 *         on 14.03.14.
 */
object CEditCourses extends Controller {

  val NAV = "EDITCOURSES"

  def page() = Action {
    implicit request =>
      Ok(editcourses("Kurse editieren", findCourses()))
  }


  def getCourseFields(courseId: Long) = Action {
    Ok(Json.stringify(Json.obj("html" -> courseFields(findCourse(courseId)).toString())))
  }

  def saveCourseData = Action(parse.json) {
    implicit request =>
      try {
        val jsVal = request.body
        val course = findCourse((jsVal \ "courseId").as[Long])
        course.setFullName((jsVal \ "courseName").as[String])
        course.setShortName((jsVal \ "courseShortName").as[String])
        course.setSize((jsVal \ "courseSize").as[String].toInt)

        updateCourse(course)
        Ok(Json.stringify(Json.obj("result" -> "success")))
      }
      catch {
        case e:Exception => Logger.error("error",e)
        BadRequest(e.getMessage)
      }
  }
}
