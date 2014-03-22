package logic.generator.schedulegenerator

import akka.actor.{Props, Actor}
import models.persistence.Schedule
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import logic.generator.lecturegenerator.{LectureAnswer, GenerateLectures, LectureGeneratorActor}
import scala.concurrent.Await
import play.api.Logger
import com.rits.cloning.Cloner

/**
 * @author fabian 
 *         on 21.03.14.
 */
class ScheduleGeneratorActor extends Actor {

  val TIMEOUTVAL = 10

  implicit val timeout = Timeout(TIMEOUTVAL seconds)

  override def receive = {

    case GenerateSchedule(subjectList) =>

      val lectureGenerationActor = context.actorOf(Props[LectureGeneratorActor])

      val lectureFuture = ask(lectureGenerationActor, GenerateLectures(subjectList)).mapTo[LectureAnswer]
      val lectures = Await.result(lectureFuture,TIMEOUTVAL seconds).lectures

      val cloner = new Cloner

      val clone = cloner.deepClone(lectures)

      Logger.debug(clone.mkString("\n"))
      sender() ! ScheduleAnswer(new Schedule)
    case _ =>
  }

}
