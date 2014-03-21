package logic.generator.schedulegenerator

import akka.actor.{Props, Actor}
import models.persistence.Schedule
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import logic.generator.lecturegenerator.{LectureAnswer, GenerateLectures, LectureGeneratorActor}
import scala.concurrent.Await
import play.api.Logger
import models.persistence.enumerations.EDuration

/**
 * @author fabian 
 *         on 21.03.14.
 */
class ScheduleGeneratorActor extends Actor {

  implicit val timeout = Timeout(60 seconds)

  override def receive = {

    case GenerateSchedule(subjectList) =>

      val lectureGenerationActor = context.actorOf(Props[LectureGeneratorActor])

      val lectureFuture = ask(lectureGenerationActor, GenerateLectures(subjectList)).mapTo[LectureAnswer]
      val lectures = Await.result(lectureFuture, 60 seconds).lectures

      Logger.debug(lectures.filter(_.getDuration ==  EDuration.UNWEEKLY) .mkString("\n"))
      sender() ! ScheduleAnswer(new Schedule)
    case _ =>
  }

}
