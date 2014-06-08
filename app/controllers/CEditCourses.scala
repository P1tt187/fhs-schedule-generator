package controllers

import play.api.mvc._
import views.html.editcourses._
import models.fhs.pages.editcourses.MEditCourses._
import play.api.libs.json._
import models.persistence.participants.{Course, Group}
import scala.collection.JavaConversions._
import models.Transactions

import play.api._

import play.api.data._
import play.api.data.Forms._
import models.fhs.pages.editcourses.MCourse

/**
 * @author fabian 
 *         on 14.03.14.
 */
object CEditCourses extends Controller {

  val NAV = "EDITCOURSES"

  val courseForm = Form[MCourse](
    mapping("longName" -> nonEmptyText,
      "shortName" -> nonEmptyText,
      "size" -> number(min = 1)
    )(MCourse.apply)(MCourse.unapply)
  )

  def page() = Action {
    implicit request =>
      Ok(editcourses("Kurse editieren", findCourses(), courseForm))
  }


  def getCourseFields(courseId: Long) = Action {
    Ok(Json.stringify(Json.obj("htmlresult" -> courseFields(findCourse(courseId)).toString())))
  }

  def addCourse = Action {
    implicit request =>
      val result = courseForm.bindFromRequest()

      result.fold(
        error => {
          BadRequest(editcourses("Kurse editieren", findCourses(), error))
        },
        mCourse => {
          val newCourse = new Course
          newCourse.setFullName(mCourse.longName)
          newCourse.setShortName(mCourse.shortName)
          newCourse.setSize(mCourse.size)
          newCourse.setGroups(List[Group]())

          Transactions {
            implicit em =>
              em.persist(newCourse)
          }
          Redirect(routes.CEditCourses.page())
        }
      )
  }

  def deleteCourse(courseId: Long) = Action {

    val subjects = findSubjectsWithCourse(courseId)
    if (subjects.isEmpty) {
      removeCourse(courseId)
      Ok( "ok")
    } else {
      val errorSubjects = subjects.map(s => s.getName).mkString(",")
      BadRequest(errorSubjects)
    }


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
        case e: Exception => Logger.error("error", e)
          BadRequest(e.getMessage)
      }
  }

  def saveGroupData = Action(parse.json) {
    implicit request =>
      try {
        val jsVal = request.body
        val course = findCourse((jsVal \ "courseId").as[Long])
        val groupType = (jsVal \ "addGroupTypeName").as[String]
        val groupCount = (jsVal \ "addGroupCount").as[Int]
        val numberOfExistingGroups = getGroupCount(groupType, course)

        val result = (1 to groupCount).map { i =>
          val group = new Group
          group.setGroupType(groupType)
          group.setParent(null)
          group.setCourse(course)
          group.setSize(course.getSize / groupCount)
          group.setSubGroups(List[Group]())
          group.setGroupIndex(numberOfExistingGroups + i)
          group.setIgnoreGroupIndex(false)

          if (i == groupCount) {
            group.setSize(course.getSize / groupCount + course.getSize % groupCount)
          }

          course.setGroups(course.getGroups :+ group)

          Transactions {
            implicit em =>
              em.persist(group)
          }

          group
        }

        Transactions {
          implicit em =>
            em.merge(course)
        }

        Ok(Json.stringify(Json.obj("htmlresult" -> result.map(g => groupFields(g).toString()).foldLeft("")(_ + _))))
      }
      catch {
        case ex: Exception => Logger.error("saveGroupData", ex)
          BadRequest(ex.getMessage)
      }
  }

  def saveSubGroupData = Action(parse.json) {
    implicit request =>
      try {
        val jsVal = request.body
        val parent = findGroup((jsVal \ "groupId").as[Long])
        val groupType = (jsVal \ "addSubGroupTypeName").as[String]
        val groupCount = (jsVal \ "addSubGroupCount").as[Int]
        val numberOfExistingGroups = getGroupCount(groupType, parent.getCourse)

        val result = (1 to groupCount).map { i =>
          val group = new Group
          group.setGroupType(groupType)
          group.setParent(parent)
          group.setCourse(parent.getCourse)
          group.setSize(parent.getSize / groupCount)
          group.setSubGroups(List[Group]())
          group.setGroupIndex(i)
          group.setIgnoreGroupIndex(false)

          group.setGroupIndex(numberOfExistingGroups + i)

          if (i == groupCount) {
            group.setSize(parent.getSize / groupCount + parent.getSize % groupCount)
          }

          parent.setSubGroups(parent.getSubGroups :+ group)

          Logger.debug("saveSubGroupData " + group)
          Logger.debug("parent " + parent.getSubGroups)


          Transactions {
            implicit em =>
              em.persist(group)
              em.merge(parent)
          }

          group
        }



        Ok(Json.stringify(Json.obj("htmlresult" -> result.map(g => groupFields(g).toString()).foldLeft("")(_ + _))))
      }
      catch {
        case ex: Exception => Logger.error("saveGroupData", ex)
          BadRequest(ex.getMessage)
      }
  }

  def updateGroup = Action(parse.json) {
    implicit request =>
      try {
        val jsVal = request.body

        val group = findGroup((jsVal \ "groupId").as[Long])
        val size = (jsVal \ "groupsize").as[Int]

        val groupType = (jsVal \ "grouptype").as[String]

        val ignoreGroupIndex = (jsVal \ "ignoreGroupIndex").as[Boolean]

        group.setGroupType(groupType)
        group.setSize(size)
        group.setIgnoreGroupIndex(ignoreGroupIndex)

        Transactions {
          implicit em =>
            em.merge(group)
        }

        Ok(Json.stringify(Json.obj("result" -> "success")))
      }
      catch {
        case ex: Exception => Logger.error("saveGroupData", ex)
          BadRequest(ex.getMessage)
      }
  }

  def deleteGroup(groupId: Long) = Action {
    try {

      removeGroup(groupId)

      Ok(Json.stringify(Json.obj("result" -> "success")))
    }
    catch {
      case ex: Exception => Logger.error("saveGroupData", ex)
        BadRequest(ex.getMessage)
    }
  }

  def getGroup(groupId: Long) = Action {
    Ok(Json.stringify(Json.obj("htmlresult" -> groupFields(findGroup(groupId)).toString())))
  }
}
