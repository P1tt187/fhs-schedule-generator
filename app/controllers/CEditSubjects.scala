package controllers

import play.api.mvc._
import play.api.libs.json._
import models.fhs.pages.editsubjects.MEditSubjects._
import play.api.Logger
import java.util.regex.Pattern
import views.html.editsubjects._
import models.persistence.subject.{ExersiseSubject, LectureSubject}

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

    if(subject.isInstanceOf[ExersiseSubject]){
      Logger.debug(subject.asInstanceOf[ExersiseSubject].getGroupType)
    }

    Ok(Json.stringify(Json.obj("html" -> subjectfields(subjectType, subject).toString())))
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

}
