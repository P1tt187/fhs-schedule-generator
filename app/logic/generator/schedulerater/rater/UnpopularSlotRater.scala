package logic.generator.schedulerater.rater

import models.persistence.scheduletree.TimeSlot
import models.persistence.lecture.Lecture
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 07.05.14.
 */
class UnpopularSlotRater extends Rater {
  override def rate(timeSlots: List[TimeSlot]): (Int, Set[Lecture]) = {
    val lectures = timeSlots.filter(_.isUnpopular).flatMap(_.getLectures.toList).toSet.asInstanceOf[Set[Lecture]]

    (lectures.size, lectures)
  }
}
