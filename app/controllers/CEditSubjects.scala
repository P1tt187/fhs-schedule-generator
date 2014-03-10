package controllers

import play.api.mvc._
import play.api.libs.json._
import models.fhs.pages.editsubjects.MEditSubjects._
import play.api.Logger
import java.util.regex.Pattern
import views.html.editsubjects._
import models.persistence.subject.{ExersiseSubject, LectureSubject}
import play.api.cache.Cache
import play.api.Play.current

/**
 * @author fabian 
 *         on 05.03.14.
 */
object CEditSubjects extends Controller {

  val NAV = "EDITSUBJECTS"

  val LECTURE = "lecture"
  val EXERSISE = "exersise"


  def page = Action {
    Ok(views.html.editsubjects.editsubjects("Fächer editieren", findSemesters()))
  }

  def getSubjectFields(subjectType: String, idString: String) = Action {
    val id = idString.toLong

    val subject = subjectType match {
      case LECTURE =>
        findSubject(classOf[LectureSubject], id)
      case EXERSISE =>
        findSubject(classOf[ExersiseSubject], id)
    }

    subject match {
      case exersiseSubject: ExersiseSubject =>
        Logger.debug(exersiseSubject.getGroupType)
      case _ =>
    }


    val docents = Cache.getOrElse("docents") {
      val docent = findDocents()
      Cache.set("docents", docent)
      docent
    }


    val courses = Cache.getOrElse("courses") {
      val course = findCourses()
      Cache.set("courses", course)
      course
    }
    Ok(Json.stringify(Json.obj("html" -> subjectfields(subjectType, subject, docents, courses).toString())))
  }

  def getNamesField(semester: String, subjectType: String) = Action {
    val semesterPattern = semester.replaceAll(Pattern.quote("+"), "/").trim
    Logger.debug("semester: " + semesterPattern + " subjectType: " + subjectType)
    //Logger.debug("" +MEditSubjects.findLectureSubjectsForSemester(semester.replaceAll(Pattern.quote("+"),"/").trim))
    subjectType match {
      case LECTURE =>
        Ok(Json.stringify(Json.obj("html" -> namefield(findLectureSubjectsForSemester(semesterPattern), LECTURE).toString())))
      case EXERSISE =>
        Ok(Json.stringify(Json.obj("html" -> namefield(findExersiseSubjectsForSemester(semesterPattern), EXERSISE).toString())))
    }
  }

  def saveData = Action(parse.json) {
    implicit request =>

      val jsVal=request.body

      val subjectId = (jsVal \ "subjectId").as[Long]

    Logger.debug("" + (jsVal \ "activeCheckbox").as[Boolean])

      val subject = (jsVal \ "subjectType").as[String] match {
        case LECTURE =>
          findSubject(classOf[LectureSubject], subjectId)
        case EXERSISE =>
          findSubject(classOf[ExersiseSubject], subjectId)
      }


      Ok("")
  }

}
