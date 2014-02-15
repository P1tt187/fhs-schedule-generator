package logic

import java.io.File

/**
 * @author fabian
 *         on 14.02.14.
 */
sealed trait  ImportMessage

case class ImportFile(file:File) extends ImportMessage

