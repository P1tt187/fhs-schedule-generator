package logic.generator.schedulegenerator

import akka.actor.{PoisonPill, Props, Actor}
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import logic.generator.lecturegenerator.{LectureAnswer, GenerateLectures, LectureGeneratorActor}
import scala.concurrent.Await
import com.rits.cloning.{ObjenesisInstantiationStrategy, Cloner}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger


/**
 * @author fabian 
 *         on 21.03.14.
 */
class ScheduleGeneratorActor extends Actor {

  private val cloner = new Cloner(new ObjenesisInstantiationStrategy)

  private var stopPlacing = false

  val TIMEOUT_VAL = 60

  implicit val timeout = Timeout(TIMEOUT_VAL seconds)

  override def receive = {

    case GenerateSchedule(subjectList, semester) =>

      val lectureGenerationActor = context.actorOf(Props[LectureGeneratorActor])

      val lectureFuture = ask(lectureGenerationActor, GenerateLectures(subjectList)).mapTo[LectureAnswer]

      val lectures = Await.result(lectureFuture, TIMEOUT_VAL seconds).lectures

      //Logger.debug("lectures: \n" + lectures.mkString("\n"))

      cloner.setCloningEnabled(true)

      //val scheduleFuture = context.actorOf(Props[ScheduleGeneratorSlave])?SlaveGenerate(cloner.deepClone(lectures))

      Logger.debug("number of lectures: " + lectures.size)
      val theSender = sender()

      def generate() {
        val scheduleFuture = context.actorOf(Props[ScheduleGeneratorSlave]) ? SlaveGenerate(lectures)

        scheduleFuture.onSuccess {
          case  ScheduleAnswer(answer) => theSender ! ScheduleAnswer(answer)

          case PlacingFailure =>
            if (!stopPlacing) {
              generate()
            }
          case _ =>

        }
      }

      generate()

    case PoisonPill =>
      stopPlacing = true
    case _ =>
  }


}
