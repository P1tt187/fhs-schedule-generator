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

  override def receive: Actor.Receive = {

    case ImportFile(file) =>
      parseFile(file)

    case unkownCommand => throw new IllegalArgumentException("unknown command" + unkownCommand)
  }

  def parseFile(file: File) = {
    val scanner = new Scanner(file, "ISO-8859-1")

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
      }
    }
    scanner.close()
  }
}
