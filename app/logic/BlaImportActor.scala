package logic

import akka.actor.Actor
import java.io.File
import java.util.Scanner
import play.api.Logger

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

  override def receive: Actor.Receive = {

    case ImportFile(file) =>
      shortcut = shortcut.empty
      shortcutReverse = shortcutReverse.empty
      subjectNames = subjectNames.empty
      subjectMetaInformation = subjectMetaInformation.empty

      parseFile(file)

    case unkownCommand => throw new IllegalArgumentException("unknown command" + unkownCommand)
  }

  def parseFile(file: File) = {

    def Scanner = new Scanner(file, "ISO-8859-1")

    var scanner = Scanner

    while (scanner.hasNextLine) {
      val line = scanner.nextLine()
      if (line.startsWith("kinfo(")) {
        val semester = line.substring(7, line.lastIndexOf('"'))
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
        /*
                    Logger.debug("Studiengang: " + studiengang)
                    Logger.debug("Name: " + name)
                    Logger.debug("LehrveranstaltungSchluessel: " + lehrveranstaltungSchluessel)
                    Logger.debug("Semester: " + semester)
                    Logger.debug("AnzahlVorlesung: " + anzahlVorlesungstunden)
                    Logger.debug("anzahlUebungsgruppenStunden: " + anzahlUebungsgruppenStunden)*/
        subjectMetaInformation += subjectName + shortcut(course) -> SubjectMetaInformation(shortcut(course) + semester, subjectName, semester.toInt, docent, lectureCount.toInt, exersizeCount.toInt)
      } else if (line.startsWith("klv_teilnehmer(")) {
        val part = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")).split(",").map(_.replace("\"", ""))

        val subjectName = part(1)
        if (part(4).startsWith("gemeinsam mit")) {
          var connectedParticipants = part(4).substring("gemeinsam mit ".length).toUpperCase
          if (connectedParticipants.contains("(")) {
            connectedParticipants = connectedParticipants.substring(0, connectedParticipants.indexOf("(") - 1).trim
          }

          Logger.debug("subject: " + subjectName + " with: " + shortcutReverse(connectedParticipants.substring(0,connectedParticipants.length-1)))
        } else {
          subjectMetaInformation(part(1) + shortcut(part(0)))
        }
      }
    }

    scanner.close()

     Logger.debug(shortcut.toString())
    /* Logger.debug(subjectNames.mkString("\n"))
     Logger.debug(subjectMetaInformation.mkString("\n"))
     */
  }

  private case class SubjectMetaInformation(courseShortName: String, subjektName: String, semester: Int, docent: String, lectureCount: Int, exersizeCount: Int)

}

