package logic

import akka.actor.Actor
import java.io.File
import java.util.Scanner
import play.api.Logger
import models.persistence.subject.{ExersiseSubject, AbstractSubject, LectureSubject}
import models.{Semester, Transactions}
import models.persistence.participants.{Group, Course}
import org.hibernate.criterion.Restrictions
import java.util
import scala.collection.JavaConversions._
import models.persistence.Docent
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

    def createLecture(metaInfo: SubjectMetaInformation) = {

      val lectureSubject = new LectureSubject
      lectureSubject.setName(metaInfo.subjektName)
      val course = findCourse(metaInfo.courseShortName)

      lectureSubject.setCourses(Set(course))

      lectureSubject.setDocents(Set(findDocent(metaInfo.docent)))
      lectureSubject.setUnits(metaInfo.lectureCount)
      lectureSubject.setActive(true)
      lectureSubject.setSubjectSynonyms(Map(course.getShortName -> metaInfo.subjektName))
      lectureSubject

    }

    def createExersise(metaInfo: SubjectMetaInformation) = {

      val exersizeSubject = new ExersiseSubject
      exersizeSubject.setName(metaInfo.subjektName)
      val course = findCourse(metaInfo.courseShortName)

      exersizeSubject.setCourses(Set(course))

      exersizeSubject.setDocents(Set(findDocent(metaInfo.docent)))
      exersizeSubject.setUnits(metaInfo.exersizeCount)
      exersizeSubject.setActive(true)
      exersizeSubject.setSubjectSynonyms(Map(course.getShortName -> metaInfo.subjektName))
      exersizeSubject.setGroupType("")
      exersizeSubject

    }

    def createExersizeWith(metaInfo: SubjectMetaInformation, part: Array[String], subjectName: String) = {
      var exersizeSubject: ExersiseSubject = null
      exersizeSubject = findExersiseSubject(metaInfo.subjektName)
      if (exersizeSubject == null) {
        exersizeSubject = createExersise(metaInfo)
      }
      exersizeSubject.setCourses(exersizeSubject.getCourses + findCourse(metaInfo.courseShortName))

      var connectedParticipants = part(4).substring("gemeinsam mit ".length).toUpperCase.trim
      if (!connectedParticipants.startsWith("BA") && !connectedParticipants.startsWith("MA")) {
        connectedParticipants = "BA" + connectedParticipants
      }
      if (connectedParticipants.contains("(")) {
        val synonym = connectedParticipants.substring(connectedParticipants.indexOf("(") + 1, connectedParticipants.indexOf(")")).trim
        connectedParticipants = connectedParticipants.substring(0, connectedParticipants.indexOf("(") - 1).trim

        exersizeSubject.setSubjectSynonyms(exersizeSubject.getSubjectSynonyms + (connectedParticipants -> synonym))
      }
      // Logger.debug("" + lectureSubject)
      Logger.debug("subject: " + subjectName + " with: " + connectedParticipants)
      exersizeSubject.setCourses(exersizeSubject.getCourses + findCourse(connectedParticipants.substring(0, connectedParticipants.length)))
      exersizeSubject

    }

    def createLectureWith(metaInfo: SubjectMetaInformation, part: Array[String], subjectName: String) = {
      var lectureSubject: LectureSubject = null
      lectureSubject = findLectureSubject(metaInfo.subjektName)
      if (lectureSubject == null) {
        lectureSubject = createLecture(metaInfo)
      }
      lectureSubject.setCourses(lectureSubject.getCourses + findCourse(metaInfo.courseShortName))

      var connectedParticipants = part(4).substring("gemeinsam mit ".length).toUpperCase.trim
      if (!connectedParticipants.startsWith("BA") && !connectedParticipants.startsWith("MA")) {
        connectedParticipants = "BA" + connectedParticipants
      }
      if (connectedParticipants.contains("(")) {
        val synonym = connectedParticipants.substring(connectedParticipants.indexOf("(") + 1, connectedParticipants.indexOf(")")).trim
        connectedParticipants = connectedParticipants.substring(0, connectedParticipants.indexOf("(") - 1).trim

        lectureSubject.setSubjectSynonyms(lectureSubject.getSubjectSynonyms + (connectedParticipants -> synonym))
      }
      // Logger.debug("" + lectureSubject)
      Logger.debug("subject: " + subjectName + " with: " + connectedParticipants)
      lectureSubject.setCourses(lectureSubject.getCourses + findCourse(connectedParticipants.substring(0, connectedParticipants.length)))
      lectureSubject

    }

    def findLectureSubject(name: String) = {
      Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[LectureSubject]).add(Restrictions.eq("name", name)).add(Restrictions.eq("semester",semester)).uniqueResult().asInstanceOf[LectureSubject]
      }
    }

    def findExersiseSubject(name: String) = {
      Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[ExersiseSubject]).add(Restrictions.eq("name", name)).add(Restrictions.eq("semester",semester)).uniqueResult().asInstanceOf[ExersiseSubject]
      }
    }

    def saveSubject(abstractSubject: AbstractSubject) {
      abstractSubject.setSemester(semester)
      Transactions {
        implicit entityManager =>
          if (abstractSubject.getId == null) {
            entityManager.persist(abstractSubject)
          } else {
            entityManager.merge(abstractSubject)
          }
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

          subjectMetaInformation += subjectName + shortcut(course) -> SubjectMetaInformation(shortcut(course) + semester, subjectName, semester.toInt, docent, lectureCount.toFloat / 2f, exersizeCount.toFloat / 2f)
        } else if (line.startsWith("klv_teilnehmer(")) {
          /** connect participants */
          val part = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")).split(",").map(_.replace("\"", ""))

          val subjectName = part(1)
          if (part(4).startsWith("gemeinsam mit")) {

            val metaInfo = subjectMetaInformation(subjectName + shortcut(part(0)))

            var abstractSubject: AbstractSubject = null
            if (metaInfo.lectureCount > 0f) {
              abstractSubject = createLectureWith(metaInfo, part, subjectName)
              saveSubject(abstractSubject)
            }
            if (metaInfo.exersizeCount > 0f) {
              abstractSubject = createExersizeWith(metaInfo, part, subjectName)
              saveSubject(abstractSubject)
            }

            if (abstractSubject == null) {
              Logger.warn("abstractSubject=null " + metaInfo)

            }

          } else {
            val metaInfo = subjectMetaInformation(part(1) + shortcut(part(0)))
            if (metaInfo.lectureCount > 0f) {
              var lectureSubject = findLectureSubject(metaInfo.subjektName)
              if (lectureSubject == null) {
                lectureSubject = createLecture(metaInfo)
              }
              lectureSubject.setCourses(lectureSubject.getCourses + findCourse(metaInfo.courseShortName))
              lectureSubject.setSemester(semester)

              Transactions {
                implicit entityManager =>
                  if (lectureSubject.getId == null) {
                    entityManager.persist(lectureSubject)
                  } else {
                    entityManager.merge(lectureSubject)
                  }
              }
            }

            if (metaInfo.exersizeCount > 0f) {
              var exersizeSubject = findExersiseSubject(metaInfo.subjektName)
              if (exersizeSubject == null) {
                exersizeSubject = createExersise(metaInfo)
              }
              exersizeSubject.setCourses(exersizeSubject.getCourses + findCourse(metaInfo.courseShortName))
              exersizeSubject.setSemester(semester)
              Transactions {
                implicit entityManager =>
                  if (exersizeSubject.getId == null) {
                    entityManager.persist(exersizeSubject)
                  } else {
                    entityManager.merge(exersizeSubject)
                  }
              }
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

    val result = Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[AbstractSubject]).add(Restrictions.eq("semester", semester)).list().asInstanceOf[util.List[AbstractSubject]]
    }
    Logger.debug(result.mkString("\n"))


    /* Logger.debug(subjectNames.mkString("\n"))
     Logger.debug(subjectMetaInformation.mkString("\n"))
     */
  }

  private case class SubjectMetaInformation(courseShortName: String, subjektName: String, semester: Int, docent: String, lectureCount: Float, exersizeCount: Float)

}

