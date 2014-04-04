package logic.blaimport

import akka.actor.Actor
import java.io.File
import java.util.Scanner
import play.api.Logger
import models.persistence.subject.{ExerciseSubject, AbstractSubject, LectureSubject}
import models.Transactions
import models.persistence.participants.{Group, Course}
import org.hibernate.criterion.Restrictions
import java.util
import scala.collection.JavaConversions._
import models.persistence.{Semester, Docent}
import models.persistence.criteria.{AbstractCriteria, CriteriaContainer}

/**
 * @author fabian
 *         on 14.02.14.
 */
class BlaImportActor extends Actor {

  type CourseOfStudies = String
  type ShortCourseName = String
  type SubjectName = String

  private var shortcut = Map[CourseOfStudies, ShortCourseName]()
  private var shortcutReverse = Map[ShortCourseName, CourseOfStudies]()
  private var subjectNames = Map[CourseOfStudies, Set[SubjectName]]()
  private var subjectMetaInformation = Map[SubjectName, SubjectMetaInformation]()

  private var semester: Semester = null

  override def receive: Actor.Receive = {

    case ImportFile(file) =>
      shortcut = shortcut.empty
      shortcutReverse = shortcutReverse.empty
      subjectNames = subjectNames.empty
      subjectMetaInformation = subjectMetaInformation.empty


      parseFile(file)
      sender() ! ImportFinished

    case unkownCommand => throw new IllegalArgumentException("unknown command " + unkownCommand)
  }

  def parseFile(file: File) = {

    def Scanner = new Scanner(file, "ISO-8859-1")

    def findCourse(shortName: String) = {
      var course = Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[Course]).add(Restrictions.eq("shortName", shortName)).uniqueResult().asInstanceOf[Course]
      }

      if (course == null) {
        course = new Course
        course.setShortName(shortName)
        Logger.debug("shortname - " + shortName + " " + shortName.substring(0, shortName.length - 1))
        course.setFullName(shortcutReverse(shortName.substring(0, shortName.length - 1)))
        course.setGroups(new util.LinkedList[Group]())
        course.setSize(0)
        Transactions {
          implicit entityManager =>
            entityManager.persist(course)
        }
      }

      course
    }

    def findDocent(lastname: String): Docent = {
      var docent: Docent = Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[Docent]).add(Restrictions.eq("lastName", lastname)).uniqueResult().asInstanceOf[Docent]
      }
      if (docent == null) {
        docent = new Docent
        docent.setLastName(lastname)
        val criteriaContainer = new CriteriaContainer
        criteriaContainer.setCriterias(List[AbstractCriteria]())
        docent.setCriteriaContainer(criteriaContainer)
        Transactions {
          implicit entitiymanager =>
            entitiymanager.persist(docent)
        }
      }

      docent
    }

    def areSemesterValuesConsistent(semesterValues: List[Int]): Boolean = {
      val winter = semester.getName.startsWith("Winter")
      val summer = semester.getName.startsWith("Sommer")
      if (semesterValues.toSet.size > 1) {

        /** data is consistent if all are true or false */
        val checkResults = semesterValues.map {
          element =>
            if (winter) {
              element % 2 != 0
            } else
            if (summer) {
              element % 2 == 0
            } else {
              Logger.warn("this shouldn happen " + semester.getName)
              false
            }
        }


        checkResults.toSet.size < 2

      } else {
        true
      }

    }

    def setActiveValue(subject: AbstractSubject, semesterValue: Int) {

      val winter = semester.getName.startsWith("Winter")

      val winterValue = semesterValue % 2 != 0

      val summer = semester.getName.startsWith("Sommer")

      val summerValue = semesterValue % 2 == 0

      if (winter) {
        subject.setActive(winterValue)
      }

      if (summer) {
        subject.setActive(summerValue)
      }
    }

    def findConsistentSemester(semesterValues: List[Int]) = {
      val winter = semester.getName.startsWith("Winter")
      val summer = semester.getName.startsWith("Sommer")

      val correctSemester =
        semesterValues.filter {
          element =>
            if (winter) {
              element % 2 != 0
            } else
            if (summer) {
              element % 2 == 0
            } else {

              false
            }
        }

      correctSemester(0)
    }

    def findLectureSubject(name: String) = {
      Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[LectureSubject]).add(Restrictions.eq("name", name)).add(Restrictions.eq("semester", semester)).uniqueResult().asInstanceOf[LectureSubject]
      }
    }

    def findExersiseSubject(name: String) = {
      Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[ExerciseSubject]).add(Restrictions.eq("name", name)).add(Restrictions.eq("semester", semester)).uniqueResult().asInstanceOf[ExerciseSubject]
      }
    }

    def saveSubject(abstractSubject: AbstractSubject) {
      abstractSubject.setSemester(semester)
      Transactions {
        implicit entityManager =>
            entityManager.merge(abstractSubject)
      }
    }

    var scanner = Scanner

    /** collect shortcuts */
    while (scanner.hasNextLine) {
      val line = scanner.nextLine()
      if (line.startsWith("kinfo(")) {


        semester = Transactions.hibernateAction {
          implicit session =>
            session.createCriteria(classOf[Semester]).add(Restrictions.eq("name", line.substring(7, line.lastIndexOf('"')))).uniqueResult().asInstanceOf[Semester]
        }
        if (semester == null) {
          semester = new Semester
          semester.setName(line.substring(7, line.lastIndexOf('"')))

          Transactions {
            implicit entityManager =>
              entityManager.persist(semester)
          }
        }
        Logger.debug("semester: " + semester)
      } else if (line.startsWith("kstudiengang_fächer_planen(")) {
        val course = line.substring(line.indexOf("(\"") + 2, line.indexOf("\",["))
        Logger.debug("course: " + course)

        val subjects = line.substring(line.indexOf(",[") + 2, line.indexOf("])")).replace("\"", "").split(",")

        //subjects.foreach(s => Logger.debug("subject: " + s))

        val existingSubjects = subjectNames.getOrElse(course, Set[SubjectName]())
        subjectNames += course -> (existingSubjects ++ subjects)

      } else if (line.startsWith("kstudiengang_abk(")) {
        val parts = line.substring(line.indexOf("(\"") + 1).replace("\"", "").split(",")

        shortcut += parts(0) -> parts(1).toUpperCase
        shortcutReverse += parts(1).toUpperCase -> parts(0)
      }
    }
    scanner.close()

    Logger.debug(shortcutReverse.toString())

    scanner = Scanner

    try {
      /** collect subjects and participants */
      while (scanner.hasNextLine) {
        val line = scanner.nextLine()
        if (line.startsWith("klv(")) {
          val part = line.substring(4).replace("\"", "").split(",")
          val course = part(0)
          val subjectName = part(1)
          //val lehrveranstaltungSchluessel = part(2)
          val semester = part(3)
          val docent = part(4)
          val lectureCount = part(5)
          val exersizeCount = part(6)

          subjectMetaInformation.get(subjectName) match {
            case None => subjectMetaInformation += subjectName -> SubjectMetaInformation(List(shortcut(course)), List(shortcut(course) + semester), subjectName, List(semester.toInt), docent, lectureCount.toFloat / 2f, exersizeCount.toFloat / 2f, Map((shortcut(course) + semester) -> subjectName))

            case Some(metaInfo) =>
              var theSynonyms = metaInfo.synonyms
              theSynonyms += (shortcut(course) + semester) -> subjectName
              subjectMetaInformation += subjectName -> metaInfo.copy(synonyms = theSynonyms, courseShortName = metaInfo.courseShortName :+ (shortcut(course) + semester), semesterValue = metaInfo.semesterValue :+ semester.toInt, courseShortCut = metaInfo.courseShortCut :+ shortcut(course))
          }


        } else if (line.startsWith("klv_teilnehmer(")) {
          /** connect participants */
          val part = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")).split(",").map(_.replace("\"", ""))

          val subjectName = part(1)
          if (part(4).startsWith("gemeinsam mit")) {

            val metaInfo = subjectMetaInformation(subjectName)

            var connectedParticipants = part(4).substring("gemeinsam mit ".length).toUpperCase.trim
            if (!connectedParticipants.startsWith("BA") && !connectedParticipants.startsWith("MA")) {
              connectedParticipants = "BA" + connectedParticipants
            }
            if(!connectedParticipants.matches("^[a-zA-Z]+\\d$")){
              connectedParticipants+=metaInfo.semesterValue.head
            }


            if (connectedParticipants.contains("(")) {
              val synonym = connectedParticipants.substring(connectedParticipants.indexOf("(") + 1, connectedParticipants.indexOf(")")).trim
              connectedParticipants = connectedParticipants.substring(0, connectedParticipants.indexOf("(") - 1).trim
              subjectMetaInformation += part(1) -> metaInfo.copy(courseShortName = metaInfo.courseShortName :+ connectedParticipants, synonyms = metaInfo.synonyms + (connectedParticipants -> synonym))
            } else {
              subjectMetaInformation += part(1) -> metaInfo.copy(courseShortName = metaInfo.courseShortName :+ connectedParticipants)
            }
          }
        }
      }
    }
    catch {
      case e: Exception => Logger.error("error", e)
    }
    scanner.close()

    Logger.debug(shortcut.toString())

    subjectMetaInformation.foreach {
      case (k, v) =>
        v match {
          case SubjectMetaInformation(courseShortCut, courseShortName, subjectName, semesterValues, docent, lectureCount, exersizeCount, synonyms) =>



            def initSubject(subject: AbstractSubject, courses: Set[Course], subjectName: String, semesterValues: List[Int], docent: String, unitCount: Float, synonyms: Map[String, String]) {
              subject.setDocents(Set(findDocent(docent)))
              subject.setName(subjectName)
              subject.setSemester(semester)
              subject.setUnits(unitCount)

              subject.setSubjectSynonyms(synonyms)

              subject.setCriteriaContainer(new CriteriaContainer)
              subject.getCriteriaContainer.setCriterias(List[AbstractCriteria]())

              if (areSemesterValuesConsistent(semesterValues)) {
                setActiveValue(subject, semesterValues(0))
              } else {
                subject.setActive(true)
              }


              subject.setCourses(courses)
              if (subject.isActive) {
                saveSubject(subject)
              }
            }

            val courses = if (areSemesterValuesConsistent(semesterValues)) {
            //  Logger.debug("subjectname - " + subjectName)
              courseShortName.map(e => findCourse(e)).toSet

            } else {
              val correctSemester = findConsistentSemester(semesterValues)

              var correctCourse = Set[Course]()

              for (i <- 0 to courseShortCut.length - 1) {

                correctCourse += findCourse(courseShortCut(i) + correctSemester)

              }

              correctCourse

            }

            if (exersizeCount > 0f) {
              val exerciseSubject = new ExerciseSubject

              exerciseSubject.setGroupType("PCPOOL")

              initSubject(exerciseSubject, courses, subjectName, semesterValues, docent, exersizeCount, synonyms)
            }
            if (lectureCount > 0f) {
              val lectureSubject = new LectureSubject
              initSubject(lectureSubject, courses, subjectName, semesterValues, docent, lectureCount, synonyms)
            }
        }

    }

    // Logger.debug("" + subjectMetaInformation.mkString("\n"))

    val result = Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[AbstractSubject]).add(Restrictions.eq("semester", semester)).list().asInstanceOf[util.List[AbstractSubject]]
    }
    Logger.debug(result.mkString("\n"))


    /* Logger.debug(subjectNames.mkString("\n"))
     Logger.debug(subjectMetaInformation.mkString("\n"))
     */
  }

  private case class SubjectMetaInformation(courseShortCut: List[String], courseShortName: List[String], subjektName: String, semesterValue: List[Int], docent: String, lectureCount: Float, exersizeCount: Float, synonyms: Map[String, String])

}

