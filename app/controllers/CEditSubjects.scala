package controllers

import play.api.mvc._
import play.api.libs.json._
import models.fhs.pages.editsubjects.MEditSubjects._
import play.api.Logger
import java.util.regex.Pattern
import views.html.editsubjects._
import models.persistence.subject.{ExerciseSubject, LectureSubject}
import play.api.cache.Cache
import play.api.Play.current
import scala.collection.JavaConversions._
import models.Transactions

/**
 * @author fabian 
 *         on 05.03.14.
 */
object CEditSubjects extends Controller {

  val NAV = "EDITSUBJECTS"

  val LECTURE = "lecture"
  val EXERCISE = "exercise"


  def page = Action {
    Ok(views.html.editsubjects.editsubjects("Fächer editieren", findSemesters(), findDocents(), findCourses()))
  }

  def getSubjectFields(subjectType: String, idString: String) = Action {

    if (idString.equals("null")) {

      BadRequest("")

    } else {
      val id = idString.toLong

      val subject = subjectType match {
        case LECTURE =>
          findSubject(classOf[LectureSubject], id)
        case EXERCISE =>
          findSubject(classOf[ExerciseSubject], id)
      }

      subject match {
        case exersiseSubject: ExerciseSubject =>
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
  }

  def getNamesField(semester: String, subjectType: String, filterDocentId: Long, filterCourseId: Long, filterActive: String) = Action {
    val semesterPattern = semester.replaceAll(Pattern.quote("+"), "/").trim
    Logger.debug("semester: " + semesterPattern + " subjectType: " + subjectType)
    //Logger.debug("" +MEditSubjects.findLectureSubjectsForSemester(semester.replaceAll(Pattern.quote("+"),"/").trim))
    subjectType match {
      case LECTURE =>
        Ok(Json.stringify(Json.obj("html" -> namefield(findLectureSubjectsForSemester(semesterPattern, filterDocentId, filterCourseId, filterActive), LECTURE).toString())))
      case EXERCISE =>
        Ok(Json.stringify(Json.obj("html" -> namefield(findExersiseSubjectsForSemester(semesterPattern, filterDocentId, filterCourseId, filterActive), EXERCISE).toString())))
    }
  }


  def saveData = Action(parse.json) {
    implicit request =>
      try {
        val jsVal = request.body

        val subjectId = (jsVal \ "subjectId").as[Long]

        val activeCheckbox = (jsVal \ "activeCheckbox").as[Boolean]

        val nameInput = (jsVal \ "nameInput").as[String]

        val unitInput = (jsVal \ "unitInput").as[String].trim.toFloat

        val selectedCourseIds = (jsVal \ "selectCourse").as[JsArray].value.map(_.as[String].toLong).toList

        val selectDocentsIds = (jsVal \ "selectDocents").as[JsArray].value.map(_.as[String].toLong).toList

        val selectedCourse = findCourses(selectedCourseIds).toSet

        val selectedDocents = findDocents(selectDocentsIds).toSet

        //Logger.debug("" + (jsVal \ "selectCourse"))


        val subject = (jsVal \ "subjectType").as[String] match {
          case LECTURE =>
            findSubject(classOf[LectureSubject], subjectId)
          case EXERCISE =>
            val groupTypeInput = (jsVal \ "groupTypeInput").as[String]
            val exercise = findSubject(classOf[ExerciseSubject], subjectId)
            exercise.setGroupType(groupTypeInput)
            exercise
        }

        subject.setActive(activeCheckbox)
        subject.setName(nameInput)
        subject.setUnits(unitInput)
        subject.setCourses(selectedCourse)
        subject.setDocents(selectedDocents)

        Transactions {
          implicit entitymanager =>
            entitymanager.merge(subject)
        }

        Ok(Json.stringify(Json.obj("result" -> "succsess")))
      }
      catch {
        case e: Exception =>
          Logger.error("submit error", e)
          NotAcceptable(Json.stringify(Json.obj("result" -> e.getMessage)))
      }
  }

}
