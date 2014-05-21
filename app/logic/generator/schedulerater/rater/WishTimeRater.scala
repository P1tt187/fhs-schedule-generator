package logic.generator.schedulerater.rater

import models.persistence.scheduletree.TimeSlot
import models.persistence.lecture.Lecture
import scala.collection.JavaConversions._
import models.Transactions
import org.hibernate.criterion.CriteriaSpecification
import models.persistence.criteria.DocentTimeWish
import models.persistence.enumerations.EDocentTimeKind
import models.persistence.docents.Docent


/**
 * @author fabian 
 *         on 05.05.14.
 */
class WishTimeRater extends Rater {
  override def rate(timeSlots: List[TimeSlot]): (Int, Set[Lecture]) = {
    val docents = Transactions.hibernateAction{
      implicit s =>
        s.createCriteria(classOf[Docent]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().toList.asInstanceOf[List[Docent]]
    }

    val lectures = docents.flatMap{
      d=>

        val wishTimes = d.getCriteriaContainer.getCriterias.filter{
          case dtw:DocentTimeWish => dtw.getTimeKind == EDocentTimeKind.AVAILABLE
          case _=> false
        }.toList.asInstanceOf[List[DocentTimeWish]]

        val slots = timeSlots.filter{
          ts=>
            /** find all timeslots which contains docent */
            !ts.getLectures.find(!_.getDocents.find(_.compareTo(d)==0).isEmpty).isEmpty

        }

        slots.filter{
          case ts:TimeSlot=>
            !wishTimes.find(ts.isInTimeSlotCriteria).isEmpty

        }.map{
          ts=>
          ts.getLectures.filter(!_.getDocents.find(_.compareTo(d)==0).isEmpty).head.asInstanceOf[Lecture]
        }

    }.toSet

    lectures.foreach{l=>
      l.increaseDifficultLevel()
      l.setNotOptimalPlaced("*")
    }
    (lectures.size, lectures)
  }
}
