package logic.generator.schedulerater

import models.persistence.Schedule
import logic.generator.schedulerater.rater.ERaters

/**
 * @author fabian 
 *         on 05.05.14.
 */
sealed trait RatingMessage

case class Rate(schedule:Schedule) extends RatingMessage

case class RateAnswer(qualitiy:Map[ERaters, Int]) extends RatingMessage
