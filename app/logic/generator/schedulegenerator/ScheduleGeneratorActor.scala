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
import logic.generator.schedulerater.rater.ERaters
import scala.annotation.tailrec


/**
 * @author fabian 
 *         on 21.03.14.
 */
class ScheduleGeneratorActor extends Actor {

  private val cloner = new Cloner(new ObjenesisInstantiationStrategy)

  private var stopPlacing = false

  private var optimalSchedule: Option[Schedule] = None

  private var rate = Map[ERaters, Int]()

  val TIMEOUT_VAL = 60

  implicit val timeout = Timeout(TIMEOUT_VAL minutes)

  @tailrec
  private def isBetter(rater: List[ERaters], newRate: Map[ERaters, Int]): Boolean = {
    if (rater.isEmpty) {
      return false
    }
    val r = rater.head
    rate.get(r) match {
      case None => true
      case Some(value) =>
        if (value > newRate(r)) {
          true
        } else if (value == newRate(r)) {
          isBetter(rater.tail, newRate)
        } else {
          false
        }
    }
  }

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
            case Some(schedule) => theSender ! ScheduleAnswer(schedule, rate(ERaters.WISHTIME_RATER))
            case None => theSender ! InplacebleSchedule(lectures.sortBy(_.getDifficulty.multiply(BigInteger.valueOf(-1))).take(20))
          }
          return
        }

        val scheduleFuture = context.actorOf(Props[ScheduleGeneratorSlave]) ? SlaveGenerate(lectures)

        scheduleFuture.onSuccess {
          case ScheduleSlaveAnswer(answer) =>

            val rateFuture = (context.actorOf(Props[ScheduleRateActor]) ? Rate(answer)).mapTo[RateAnswer]

            val newRate = Await.result(rateFuture, TIMEOUT_VAL seconds).qualitiy



            Logger.debug("************************************")

            if (isBetter(ERaters.values().toList.sortBy(-_.getPriority), newRate)) {
              Logger.debug("new optimum: " + newRate.values.sum + " " + newRate)
              rate = newRate
              optimalSchedule = Some(cloner.deepClone(answer))
            } else {
              Logger.debug("rate: " + newRate + " current: " + rate)
            }

            lectures.par.foreach(l => if (l.getDuration != EDuration.WEEKLY) {
              l.setDuration(EDuration.UNWEEKLY)
            })

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
