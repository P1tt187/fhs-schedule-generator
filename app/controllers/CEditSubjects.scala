package controllers

import com.rits.cloning.{Cloner, ObjenesisInstantiationStrategy}
import models.Transactions
import models.fhs.pages.editsubjects.MEditSubjects._
import models.fhs.pages.editsubjects.MSemester
import models.persistence.Semester
import models.persistence.criteria.{AbstractCriteria, CriteriaContainer, RoomCriteria, TimeSlotCriteria}
import models.persistence.docents.Docent
import models.persistence.enumerations.EDuration
import models.persistence.location.{HouseEntity, RoomAttributesEntity, RoomEntity}
import models.persistence.participants.Course
import models.persistence.subject.{AbstractSubject, ExerciseSubject, LectureSubject}
import models.persistence.template.WeekdayTemplate
import org.hibernate.criterion.Restrictions
import play.api.Play.current
import play.api._
import play.api.cache.Cache
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json._
import play.api.mvc._
import views.html.editsubjects._

import scala.collection.JavaConversions._
import scala.concurrent.duration._


/**
 * @author fabian 
 *         on 05.03.14.
 */
object CEditSubjects extends Controller {

  val NAV = "EDITSUBJECTS"

  val LECTURE = "lecture"
  val EXERCISE = "exercise"

  val TIME_TO_LIFE = 30 seconds


  val semesterForm: Form[MSemester] = Form(
    mapping(
      "name" -> nonEmptyText
    )(MSemester.apply)(MSemester.unapply)
  )

  def page = Action {
    implicit request =>
      Ok(views.html.editsubjects.editsubjects("Fächer editieren", findSemesters(), findDocents(), findCourses(), semesterForm))
  }

  def addSemester = Action {
    implicit request =>
      val formResult = semesterForm.bindFromRequest
      formResult.fold(
        error => {
          BadRequest(views.html.editsubjects.editsubjects("Fächer editieren", findSemesters(), findDocents(), findCourses(), error))
        },
        result => {
          val newSemester = new Semester
          newSemester.setName(result.name)

          Transactions {
            implicit em =>
              em.persist(newSemester)
          }

          Redirect(routes.CEditSubjects.page())
        }
      )
  }

  def copySubject(subjectType: String, id: Long) = Action {
    val cloner = new Cloner(new ObjenesisInstantiationStrategy)

    val subject = subjectType match {
      case LECTURE =>
        findSubject(classOf[LectureSubject], id)
      case EXERCISE =>
        findSubject(classOf[ExerciseSubject], id)
    }

    val subjectClone = cloner.deepClone(subject)
    subjectClone.setId(-1l)
    subjectClone.setName("*" + subjectClone.getName)

    val (docents: List[Docent], courses: List[Course], houses: List[HouseEntity], rooms: List[RoomEntity]) = loadCachedData()

    Ok(Json.stringify(Json.obj("htmlresult" -> subjectfields(subjectType, subjectClone, docents, courses, houses, rooms).toString().trim())))


  }

  def deleteSubject(subjectType: String, id: Long) = Action {
    val subject = subjectType match {
      case LECTURE =>
        findSubject(classOf[LectureSubject], id)
      case EXERCISE =>
        findSubject(classOf[ExerciseSubject], id)
    }

    removeSubject(subject)
    Redirect(routes.CEditSubjects.page())
  }

  def getSubjectFields(semester: Long, subjectType: String, idString: String) = Action {
    implicit request =>
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
        subject.setSemester(findSemesterById(semester))
        initNewSubject(subject)
      }

      subject match {
        case exerciseSubject: ExerciseSubject =>
          Logger.debug(exerciseSubject.getGroupType)
        case _ =>
      }


      val (docents: List[Docent], courses: List[Course], houses: List[HouseEntity], rooms: List[RoomEntity]) = loadCachedData()

      val selectedSubject = if (idString == "null") {
        "-1"
      } else {
        idString
      }

      Ok(Json.stringify(Json.obj("htmlresult" -> subjectfields(subjectType, subject, docents, courses, houses, rooms).toString().trim()))).withSession(session + ("subjectFields" -> selectedSubject))

  }

  private def loadCachedData(): (List[Docent], List[Course], List[HouseEntity], List[RoomEntity]) = {
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
    (docents, courses, houses, rooms)
  }

  def getNamesField(semester: Long, subjectType: String, filterDocentId: Long, filterCourseId: Long, filterActive: String) = Action {
    implicit request =>
      //Logger.debug("" +MEditSubjects.findLectureSubjectsForSemester(semester.replaceAll(Pattern.quote("+"),"/").trim))
      subjectType match {
        case LECTURE =>
          Ok(Json.stringify(Json.obj("html" -> namefield(findLectureSubjectsForSemester(semester, filterDocentId, filterCourseId, filterActive), LECTURE).toString())))
        case EXERCISE =>
          Ok(Json.stringify(Json.obj("html" -> namefield(findExerciseSubjectsForSemester(semester, filterDocentId, filterCourseId, filterActive), EXERCISE).toString())))
      }
  }

  def deleteSemester(semesterID: Long) = Action {

    val semester = findSemesterById(semesterID)
    deleteLecturesAndSchedules(semester)

    Redirect(routes.CEditSubjects.page())
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

        val timeCriterias = (jsVal \ "timecriterias").as[JsArray].value.map { js =>
          Logger.debug("" + js)

          val startHour = (js \ "startHour").as[Int]
          val startMinute = (js \ "startMinute").as[Int]
          val stopHour = (js \ "stopHour").as[Int]
          val stopMinute = (js \ "stopMinute").as[Int]
          val duration = EDuration.valueOf((js \ "duration").as[String])
          val sortIndex = (js \ "weekday").as[Int]

          val dbResult = Transactions.hibernateAction {
            implicit session =>
              session.createCriteria(classOf[WeekdayTemplate]).add(Restrictions.eq("sortIndex", sortIndex)).uniqueResult().asInstanceOf[WeekdayTemplate]
          }

          val day = if (dbResult == null) {
            val theDay = WeekdayTemplate.createWeekdayFromSortIndex(sortIndex)
            Transactions {
              implicit emf =>
                emf.persist(theDay)
            }
            theDay
          } else {
            dbResult
          }


          val timeCrit = new TimeSlotCriteria(startHour, startMinute, stopHour, stopMinute, day, duration)
          timeCrit
        }

        val alternativRooms = findSelectedRooms((jsVal \ "alternativeRooms").as[JsArray].value.map(_.as[String].toLong).toList)

        val semester = (jsVal \ "semester").as[Long]

        val duration = EDuration.valueOf((jsVal \ "duration").as[String])

        val synonyms = (jsVal \ "synonyms").as[JsArray].value.map {
          js =>
            ((js \ "courseShortName").as[String], (js \ "synonym").as[String])
        }.toMap

        val shortCuts = (jsVal \ "synonyms").as[JsArray].value.map {
          js =>
            ((js \ "courseShortName").as[String], (js \ "shortCut").as[String])
        }.toMap

        val selectedCourse = findCourses(selectedCourseIds).toSet

        val selectedDocents = findDocents(selectDocentsIds).toSet

        val roomAttributes = findRoomAttributes(roomAttributesInput)

        val selectedHouses = findSelectedHouses(houseCriteriaIds)

        val selectedRooms = findSelectedRooms(roomCriteriaIds)

        val selectedSemester = findSemesterById(semester)



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

        if (null == subject.getId) {
          Logger.debug("init new subject")
          initNewSubject(subject)
          subject.setSemester(selectedSemester)

          Logger.debug("semester " + selectedSemester)
          subject match {
            case exercise: ExerciseSubject =>
              val groupTypeInput = (jsVal \ "groupTypeInput").as[String]
              exercise.setGroupType(groupTypeInput)
            case _ =>
          }
        }
        subject.setDuration(duration)

        initCourseValues(subject, activeCheckbox, nameInput, unitInput, selectedCourse, selectedDocents, expectedParticipants)
        subject.setAlternativRooms(alternativRooms)
        subject.setSubjectSynonyms(synonyms)
        subject.setShortCuts(shortCuts)

        Logger.debug("alternativeRooms: " + alternativRooms)

        val criteriaContainer = new CriteriaContainer
        criteriaContainer.setCriterias(List[AbstractCriteria]())

        val oldCriteriaContainer = subject.getCriteriaContainer

        if (!roomAttributes.isEmpty) {
          setRoomAttributesForCriteria(roomAttributes, criteriaContainer)
        }

        setHouseCriterias(selectedHouses, criteriaContainer)

        setRoomCriterias(selectedRooms, criteriaContainer)

        criteriaContainer.setCriterias(criteriaContainer.getCriterias ++ timeCriterias)

        subject.setCriteriaContainer(criteriaContainer)

        Logger.debug("save subject " + subject.getId)

        Transactions.hibernateAction{
          implicit s=>
            if(subject.getId!=null){
              s.saveOrUpdate(oldCriteriaContainer)
              s.delete(oldCriteriaContainer)
            }
            s.saveOrUpdate(subject.getSemester)
            s.saveOrUpdate(subject)
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
        rCrit.setRoom(room)

        rCrit
    }

    criteriaContainer.setCriterias(criteriaContainer.getCriterias.toList ++ roomCriterias)
  }

  private def setHouseCriterias(selectedHouses: List[HouseEntity], criteriaContainer: CriteriaContainer) {
    val houseCriterias = selectedHouses.map {
      house =>
        val rCrit = new RoomCriteria
        rCrit.setHouse(house)

        rCrit
    }

    criteriaContainer.setCriterias(criteriaContainer.getCriterias.toList ++ houseCriterias)
  }

  private def setRoomAttributesForCriteria(roomAttributes: List[RoomAttributesEntity], criteriaContainer: CriteriaContainer) {
    val roomCriteria = new RoomCriteria
    roomCriteria.setRoomAttributes(roomAttributes)
    criteriaContainer.setCriterias(criteriaContainer.getCriterias :+ roomCriteria)
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
