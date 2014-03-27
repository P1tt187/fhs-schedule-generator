package logic.generator.schedulegenerator

import akka.actor.{Props, Actor}
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import logic.generator.lecturegenerator.{LectureAnswer, GenerateLectures, LectureGeneratorActor}
import scala.concurrent.Await
import com.rits.cloning.{ObjenesisInstantiationStrategy, Cloner}
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * @author fabian 
 *         on 21.03.14.
 */
class ScheduleGeneratorActor extends Actor {

  private val cloner = new Cloner(new ObjenesisInstantiationStrategy)

  val TIMEOUT_VAL = 10

  implicit val timeout = Timeout(TIMEOUT_VAL seconds)

  override def receive = {

    case GenerateSchedule(subjectList) =>

      val lectureGenerationActor = context.actorOf(Props[LectureGeneratorActor])

      val lectureFuture = ask(lectureGenerationActor, GenerateLectures(subjectList)).mapTo[LectureAnswer]

      val lectures = Await.result(lectureFuture,TIMEOUT_VAL seconds).lectures

      Logger.debug("lectures: \n" + lectures.mkString("\n"))

      cloner.setCloningEnabled(true)

      val scheduleFuture = context.actorOf(Props[ScheduleGeneratorSlave])?SlaveGenerate(cloner.deepClone(lectures))


      scheduleFuture.onSuccess {
        case ScheduleAnswer(schedule) => sender() ! ScheduleAnswer(schedule)
        case _=>

      }


    case _ =>
  }

}
