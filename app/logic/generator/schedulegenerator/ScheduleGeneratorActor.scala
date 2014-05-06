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
import models.persistence.Schedule
import java.util.Calendar
import logic.generator.schedulerater.{RateAnswer, ScheduleRateActor, Rate}
import java.math.BigInteger
import models.persistence.enumerations.EDuration


/**
 * @author fabian 
 *         on 21.03.14.
 */
class ScheduleGeneratorActor extends Actor {

  private val cloner = new Cloner(new ObjenesisInstantiationStrategy)

  private var stopPlacing = false

  private var optimalSchedule: Option[Schedule] = None

  private var rate = Int.MaxValue

  val TIMEOUT_VAL = 60

  implicit val timeout = Timeout(TIMEOUT_VAL seconds)

  override def receive = {

    case GenerateSchedule(subjectList, semester, endTime) =>

      val lectureGenerationActor = context.actorOf(Props[LectureGeneratorActor])

      val lectureFuture = ask(lectureGenerationActor, GenerateLectures(subjectList)).mapTo[LectureAnswer]

      val lectures = Await.result(lectureFuture, TIMEOUT_VAL seconds).lectures

      //Logger.debug("lectures: \n" + lectures.mkString("\n"))

      cloner.setCloningEnabled(true)

      //val scheduleFuture = context.actorOf(Props[ScheduleGeneratorSlave])?SlaveGenerate(cloner.deepClone(lectures))

      Logger.debug("number of lectures: " + lectures.size)
      val theSender = sender()

      def generate() {

        if (Calendar.getInstance.after(endTime)) {
          optimalSchedule match {
            case Some(schedule) => theSender ! ScheduleAnswer(schedule)
            case None => theSender ! InplacebleSchedule(lectures.sortBy(_.getDifficulty.multiply(BigInteger.valueOf(-1))).take(20))
          }
          return
        }

        val scheduleFuture = context.actorOf(Props[ScheduleGeneratorSlave]) ? SlaveGenerate(lectures)

        scheduleFuture.onSuccess {
          case ScheduleAnswer(answer) =>

            val rateFuture = (context.actorOf(Props[ScheduleRateActor]) ? Rate(answer)).mapTo[RateAnswer]

            val newRate = Await.result(rateFuture, TIMEOUT_VAL seconds).qualitiy
            if (newRate < rate) {
              Logger.debug("new optimum: " + newRate)
              rate = newRate
              optimalSchedule = Some(cloner.deepClone(answer))
            } else {
              Logger.debug("rate: " + rate + " current: " + newRate)
            }

            lectures.par.foreach(l=> if(l.getDuration != EDuration.WEEKLY){ l.setDuration(EDuration.UNWEEKLY) })

            generate()

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
