package logic.generator.schedulerater

import models.persistence.Schedule

/**
 * @author fabian 
 *         on 05.05.14.
 */
sealed trait RatingMessage

case class Rate(schedule:Schedule) extends RatingMessage

case class RateAnswer(qualitiy:Int) extends RatingMessage
