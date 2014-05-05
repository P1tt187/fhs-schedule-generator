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
import models.Transactions
import models.persistence.criteria.{AbstractCriteria, CriteriaContainer, RoomCriteria}
import models.persistence.enumerations.{EDuration, EPriority}
import models.persistence.participants.Course
import models.persistence.Docent
import models.persistence.location.{RoomEntity, RoomAttributesEntity, HouseEntity}
import scala.collection.mutable
import scala.concurrent.duration._
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 05.03.14.
 */
object CEditSubjects extends Controller {

  val NAV = "EDITSUBJECTS"

  val LECTURE = "lecture"
  val EXERCISE = "exercise"

  val TIME_TO_LIFE = 30 seconds

  def page = Action {
    Ok(views.html.editsubjects.editsubjects("Fächer editieren", findSemesters(), findDocents(), findCourses()))
  }

  def getSubjectFields(semester: String, subjectType: String, idString: String) = Action {


    val id = if (idString.equals("null")) {
      -1l
    } else {
      idString.toLong
    }

    val subject = subjectType match {
      case LECTURE =>
        findSubject(classOf[LectureSubject], id)
      case EXERCISE =>
        findSubject(classOf[ExerciseSubject], id)
    }

    if (subject.getId == null) {
      subject.setId(-1l)
      subject.setSemester(findSemester(extractSemesterPattern(semester)))
      initNewSubject(subject)
    }

    subject match {
      case exerciseSubject: ExerciseSubject =>
        Logger.debug(exerciseSubject.getGroupType)
      case _ =>
    }


    val docents = Cache.getOrElse("docents") {
      val docent = findDocents()
      Cache.set("docents", docent, expiration = TIME_TO_LIFE)
      docent
    }


    val courses = Cache.getOrElse("courses") {
      val course = findCourses()
      Cache.set("courses", course, expiration = TIME_TO_LIFE)
      course
    }

    val houses = Cache.getOrElse("houses") {
      val house = findHouses()
      Cache.set("houses", house, expiration = TIME_TO_LIFE)
      house
    }

    val rooms = Cache.getOrElse("rooms") {
      val room = findRooms()
      Cache.set("rooms", room, expiration = TIME_TO_LIFE)
      room
    }

    Ok(Json.stringify(Json.obj("htmlresult" -> subjectfields(subjectType, subject, docents, courses, houses, rooms).toString().trim())))

  }

  private def extractSemesterPattern(semester: String) = {
    semester.replaceAll(Pattern.quote("+"), "/").trim
  }

  def getNamesField(semester: String, subjectType: String, filterDocentId: Long, filterCourseId: Long, filterActive: String) = Action {
    val semesterPattern = extractSemesterPattern(semester)
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

        val semester = (jsVal \ "semester").as[String]

        val duration = EDuration.valueOf((jsVal \ "duration").as[String])

        val selectedCourse = findCourses(selectedCourseIds).toSet

        val selectedDocents = findDocents(selectDocentsIds).toSet

        val roomAttributes = findRoomAttributes(roomAttributesInput)

        val selectedHouses = findSelectedHouses(houseCriteriaIds)

        val selectedRooms = findSelectedRooms(roomCriteriaIds)

        val selectedSemester = findSemester(extractSemesterPattern(semester))



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

        if (subject.getId == null) {
          initNewSubject(subject)
          subject.setSemester(selectedSemester)
          subject match {
            case exercise: ExerciseSubject =>
              val groupTypeInput = (jsVal \ "groupTypeInput").as[String]
              exercise.setGroupType(groupTypeInput)
          }
        }
        subject.setDuration(duration)

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
          implicit entityManager =>
            if (subject.getId == null) {
              entityManager.merge(subject)
            } else {
              entityManager.merge(subject)
              existingRoomCriteria.foreach {
                rc =>
                  val attachedRc = entityManager.merge(rc)
                  entityManager.remove(attachedRc)
              }
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
