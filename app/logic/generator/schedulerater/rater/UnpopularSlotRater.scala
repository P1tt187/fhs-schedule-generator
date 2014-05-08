package logic.generator.schedulerater.rater

import models.persistence.scheduletree.TimeSlot
import models.persistence.lecture.Lecture

/**
 * @author fabian 
 *         on 07.05.14.
 */
class UnpopularSlotRater extends Rater {
  override def rate(timeSlots: List[TimeSlot]): (Int, Set[Lecture]) = {
    val lectures = timeSlots.filter(_.isUnpopular).flatMap(_.getLectures).toSet.asInstanceOf[Set[Lecture]]

    (lectures.size, lectures)
  }
}
