package logic.blaimport

import java.io.File

/**
 * @author fabian
 *         on 14.02.14.
 */
sealed trait  ImportMessage

case class ImportFile(file:File) extends ImportMessage

case object ImportFinished extends ImportMessage

case class ImportFailure(ex:Exception) extends ImportMessage
