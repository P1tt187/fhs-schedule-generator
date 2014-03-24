package logic.generator.schedulegenerator

import akka.actor.{Props, Actor}
import models.persistence.Schedule
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import logic.generator.lecturegenerator.{LectureAnswer, GenerateLectures, LectureGeneratorActor}
import scala.concurrent.Await
import com.rits.cloning.Cloner
import play.api.Logger


/**
 * @author fabian 
 *         on 21.03.14.
 */
class ScheduleGeneratorActor extends Actor {

  private val cloner = new Cloner()

  val TIMEOUT_VAL = 10

  implicit val timeout = Timeout(TIMEOUT_VAL seconds)

  override def receive = {

    case GenerateSchedule(subjectList) =>

      val lectureGenerationActor = context.actorOf(Props[LectureGeneratorActor])

      val lectureFuture = ask(lectureGenerationActor, GenerateLectures(subjectList)).mapTo[LectureAnswer]

      val lectures = Await.result(lectureFuture,TIMEOUT_VAL seconds).lectures

      Logger.debug("lectures: \n" + lectures.mkString("\n"))

      cloner.setCloningEnabled(true)

      context.actorOf(Props[ScheduleGeneratorSlave])?SlaveGenerate(cloner.deepClone(lectures))

      sender() ! ScheduleAnswer(new Schedule)
    case _ =>
  }

}