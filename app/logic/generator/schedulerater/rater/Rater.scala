package logic.generator.schedulerater.rater

import models.persistence.lecture.Lecture
import models.persistence.scheduletree.TimeSlot

/**
 * @author fabian 
 *         on 05.05.14.
 */
trait Rater {

  def rate(timeSlots:List[TimeSlot]):Set[Lecture]

}
