package logic.generator.schedulegenerator

import java.math.BigInteger
import java.util.Calendar

import akka.actor.{Actor, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.rits.cloning.{Cloner, ObjenesisInstantiationStrategy}
import logic.generator.schedulerater.rater.ERaters
import logic.generator.schedulerater.{Rate, RateAnswer, ScheduleRateActor}
import models.Transactions
import models.persistence.Schedule
import models.persistence.criteria.DocentTimeWish
import models.persistence.docents.Docent
import models.persistence.enumerations.EDuration
import models.persistence.lecture.Lecture
import models.persistence.template.TimeSlotTemplate
import play.api.Logger

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random


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

    case GenerateSchedule(lectures, semester, endTime, randomRatio, maxIterationDeep) =>


      //val scheduleFuture = context.actorOf(Props[ScheduleGeneratorSlave])?SlaveGenerate(cloner.deepClone(lectures))

      Logger.debug("number of lectures: " + lectures.size)

      val outOfTimeDocents = findOutOfTimeDocents(lectures)

      if (!outOfTimeDocents.isEmpty) {
        sender() ! TimeWishNotMatch(outOfTimeDocents)
        stopPlacing = true
      }

      val theSender = sender()

      def generate(iterationDeep: Int) {

        /** proof if we are finished */
        if (Calendar.getInstance.after(endTime)) {
          optimalSchedule match {
            case Some(schedule) =>
              schedule.setSemester(semester)
              schedule.setRate(rate(ERaters.WISHTIME_RATER))

              schedule.setRateSum(rate.values.sum)

              theSender ! ScheduleAnswer(schedule)
            case None => theSender ! InplacebleSchedule(lectures.sortBy(_.getDifficulty.multiply(BigInteger.valueOf(-1))).take(20))
          }
          return
        }
        /** generate new schedule */
        val scheduleFuture = context.actorOf(Props[ScheduleGeneratorSlave]) ? SlaveGenerate(lectures)

        scheduleFuture.onSuccess {
          case ScheduleSlaveAnswer(answer) =>

            val rateFuture = (context.actorOf(Props[ScheduleRateActor]) ? Rate(answer)).mapTo[RateAnswer]

            val newRate = Await.result(rateFuture, TIMEOUT_VAL seconds).qualitiy



            Logger.debug("************************************")

            val nextIteration = if (isBetter(ERaters.values().toList.sortBy(-_.getPriority), newRate)) {
              Logger.debug("new optimum: " + newRate.values.sum + " " + newRate)
              rate = newRate
              optimalSchedule = Some(cloner.deepClone(answer))
              Random.shuffle(lectures).take(randomRatio).foreach(_.increaseDifficultLevel())
              0

            } else {
              Logger.debug("deep: " + iterationDeep + " rate: " + newRate + " current: " + rate)

              /** if the algorithm reaches the maxIterationDeep and the current schedule is not near enough to a new optimum then we will reset the difficultlevel */
              if (iterationDeep == maxIterationDeep) {
                lectures.par.foreach(l => l.setDifficultLevel(BigInteger.valueOf(l.calculateNumberOfParticipants().toLong)))
                Random.shuffle(lectures).take(randomRatio).foreach(_.increaseDifficultLevel())
                0
              } else {

                if (newRate(ERaters.WISHTIME_RATER) - rate(ERaters.WISHTIME_RATER) <= 3) {
                  Random.shuffle(lectures).take(randomRatio).foreach(_.increaseDifficultLevel())
                  0
                } else {
                  iterationDeep + 1
                }
                //iterationDeep + 1
              }
            }

            lectures.par.foreach { l => if (l.getDuration != EDuration.WEEKLY) {
              l.setDuration(EDuration.UNWEEKLY)
            }
              l.setNotOptimalPlaced("")
            }
            generate(nextIteration)

          case PlacingFailure =>
            if (!stopPlacing) {
              generate(iterationDeep)
            }
          case _ =>

        }
        scheduleFuture.onFailure {
          case e: Exception =>
            theSender ! akka.actor.Status.Failure(e)
            stopPlacing=true
        }
      }

      generate(0)

    case PoisonPill =>
      stopPlacing = true
    case _ =>
  }


  private def findOutOfTimeDocents(lectures: List[Lecture]) = {
    def compareTimeSlotTemplate(template: TimeSlotTemplate, timeWish: DocentTimeWish) = {
      List(template.getParent.getSortIndex == timeWish.getWeekday.getSortIndex,
        template.getStartHour == timeWish.getStartHour,
        template.getStartMinute == timeWish.getStartMinute,
        template.getStopHour == timeWish.getStopHour,
        template.getStopMinute == timeWish.getStopMinute
      ).sorted.head

    }

    val outOfTimeDocents = Transactions.hibernateAction {
      implicit s =>
        val docents = s.createCriteria(classOf[Docent]).list().toList.asInstanceOf[List[Docent]]
        val templates = s.createCriteria(classOf[TimeSlotTemplate]).list().toList.asInstanceOf[List[TimeSlotTemplate]]

        val filteredDocents = docents.filter {
          d =>
            val timeWishes = d.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[DocentTimeWish]).toList.asInstanceOf[List[DocentTimeWish]]
            if (timeWishes.isEmpty) {
              false
            } else {
              val numberOfLectures = lectures.map {
                l =>
                  if (!l.getDocents.find(_.compareTo(d) == 0).isEmpty) {
                    l.getDuration match {
                      case EDuration.WEEKLY => 2
                      case EDuration.UNWEEKLY => 1
                      case _ => 0
                    }
                  } else {
                    0
                  }
              }.sum
              val numberOfAvailableTimes = timeWishes.map {
                tw =>
                  if (templates.find(compareTimeSlotTemplate(_, tw)).isEmpty) {
                    0
                  } else {
                    tw.getDuration match {
                      case EDuration.WEEKLY => 2
                      case EDuration.EVEN => 1
                      case EDuration.UNEVEN => 1
                      case _ => 0
                    }
                  }
              }.sum
              //Logger.debug("docent: " + d.getLastName + " nol " + numberOfLectures + " nat " + numberOfAvailableTimes)
              numberOfAvailableTimes < numberOfLectures
            }
        }
        filteredDocents
    }

    outOfTimeDocents
  }
}
