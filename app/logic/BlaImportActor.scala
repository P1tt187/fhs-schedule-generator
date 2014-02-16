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

  private var shortcut = Map[CourseOfStudies, ShortCourseName]()

  override def receive: Actor.Receive = {

    case ImportFile(file) =>
      shortcut = shortcut.empty
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

        subjects.foreach(s => Logger.debug("subject: " + s))
      } else if (line.startsWith("kstudiengang_abk(")) {
        val parts = line.substring(line.indexOf("(\"") + 1).replace("\"", "").split(",")

        shortcut += parts(0) -> parts(1)
      }
    }
    scanner.close()

    scanner= Scanner

    while(scanner.hasNextLine){
      val line = scanner.nextLine()

    }

    Logger.debug(shortcut.toString())
  }
}
