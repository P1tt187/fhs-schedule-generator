package controllers

import play.api.mvc._
import play.api.libs.json._
import models.fhs.pages.editsubjects.MEditSubjects._
import play.api.Logger
import java.util.regex.Pattern
import views.html.editsubjects._
import models.persistence.subject.{AbstractSubject, ExerciseSubject, LectureSubject}
import play.api.cache.Cache
import play.api.Play.current
import scala.collection.JavaConversions._
import models.Transactions
import models.persistence.criteria.{AbstractCriteria, CriteriaContainer, RoomCriteria}
import models.persistence.enumerations.EPriority
import models.persistence.participants.Course
import models.persistence.Docent
import models.persistence.location.{RoomEntity, RoomAttributesEntity, HouseEntity}
import scala.collection.mutable

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

      val houses = Cache.getOrElse("houses") {
        val house = findHouses()
        Cache.set("houses", house)
        house
      }

      val rooms = Cache.getOrElse("rooms") {
        val room = findRooms()
        Cache.set("rooms", room)
        room
      }

      Ok(Json.stringify(Json.obj("html" -> subjectfields(subjectType, subject, docents, courses, houses, rooms).toString().trim())))
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
        Ok(Json.stringify(Json.obj("html" -> namefield(findExerciseSubjectsForSemester(semesterPattern, filterDocentId, filterCourseId, filterActive), EXERCISE).toString())))
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

        val expectedParticipants = (jsVal \ "expectedParticipants").as[Int]

        val selectedCourseIds = (jsVal \ "selectCourse").as[JsArray].value.map(_.as[String].toLong).toList

        val selectDocentsIds = (jsVal \ "selectDocents").as[JsArray].value.map(_.as[String].toLong).toList

        val roomAttributesInput = (jsVal \ "roomAttributes").as[JsArray].value.map(_.as[String]).toList

        val houseCriteriaIds = (jsVal \ "houseCriteria").as[JsArray].value.map(_.as[String].toLong).toList

        val roomCriteriaIds = (jsVal \ "roomCriteria").as[JsArray].value.map(_.as[String].toLong).toList

        val selectedCourse = findCourses(selectedCourseIds).toSet

        val selectedDocents = findDocents(selectDocentsIds).toSet

        val roomAttributes = findRoomAttributes(roomAttributesInput)

        val selectedHouses = findSelectedHouses(houseCriteriaIds)

        val selectedRooms = findSelectedRooms(roomCriteriaIds)

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


        initCourseValues(subject, activeCheckbox, nameInput, unitInput, selectedCourse, selectedDocents, expectedParticipants)

        val criteriaContainer = subject.getCriteriaContainer
        val otherCriteria = criteriaContainer.getCriterias.filterNot(_.isInstanceOf[RoomCriteria])
        val existingRoomCriteria = criteriaContainer.getCriterias.filter(_.isInstanceOf[RoomCriteria])

        if (!roomAttributes.isEmpty) {
          setRoomAttributesForCriteria(roomAttributes, criteriaContainer, otherCriteria)
        }

        setHouseCriterias(selectedHouses, criteriaContainer)

        setRoomCriterias(selectedRooms, criteriaContainer)

        Transactions {
          implicit entitymanager =>
            entitymanager.merge(subject)
            existingRoomCriteria.foreach {
              rc =>
                val attachedRc = entitymanager.merge(rc)
                entitymanager.remove(attachedRc)
            }

        }

        Ok(Json.stringify(Json.obj("result" -> "succsess")))
      }
      catch {
        case e: Exception =>
          Logger.error("submit error", e)
          NotAcceptable(Json.stringify(Json.obj("result" -> e.getMessage)))
      }
  }


  private def setRoomCriterias(selectedRooms: List[RoomEntity], criteriaContainer: CriteriaContainer) {
    val roomCriterias = selectedRooms.map {
      room =>
        val rCrit = new RoomCriteria
        rCrit.setTolerance(false)
        rCrit.setRoom(room)
        rCrit.setPriority(EPriority.HIGH)
        rCrit
    }

    criteriaContainer.setCriterias(criteriaContainer.getCriterias.toList ++ roomCriterias)
  }

  private def setHouseCriterias(selectedHouses: List[HouseEntity], criteriaContainer: CriteriaContainer) {
    val houseCriterias = selectedHouses.map {
      house =>
        val rCrit = new RoomCriteria
        rCrit.setHouse(house)
        rCrit.setTolerance(false)
        rCrit.setPriority(EPriority.HIGH)
        rCrit
    }

    criteriaContainer.setCriterias(criteriaContainer.getCriterias.toList ++ houseCriterias)
  }

  private def setRoomAttributesForCriteria(roomAttributes: List[RoomAttributesEntity], criteriaContainer: CriteriaContainer, otherCriterias: mutable.Buffer[AbstractCriteria]) {
    val roomCriteria = new RoomCriteria
    roomCriteria.setRoomAttributes(roomAttributes)
    roomCriteria.setPriority(EPriority.HIGH)
    roomCriteria.setTolerance(false)
    criteriaContainer.setCriterias(otherCriterias :+ roomCriteria)
  }

  private def initCourseValues(subject: AbstractSubject, activeCheckbox: Boolean, nameInput: String, unitInput: Float, selectedCourse: Set[Course], selectedDocents: Set[Docent], expectedParticipants: Int) {
    subject.setActive(activeCheckbox)
    subject.setName(nameInput)
    subject.setUnits(unitInput)
    subject.setCourses(selectedCourse)
    subject.setDocents(selectedDocents)

    if (expectedParticipants != -1) {
      subject.setExpectedParticipants(expectedParticipants)
    } else {
      subject.setExpectedParticipants(null)
    }
  }
}
