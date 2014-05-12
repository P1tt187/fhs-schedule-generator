package logic.generator.schedulerater.rater

import models.persistence.scheduletree.TimeSlot
import models.persistence.lecture.Lecture
import scala.collection.JavaConversions._
import models.Transactions
import models.persistence.Docent
import org.hibernate.criterion.CriteriaSpecification
import models.persistence.criteria.DocentTimeWish
import models.persistence.enumerations.EDocentTimeKind


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
            !ts.getLectures.find(_.getDocents.contains(d)).isEmpty

        }

        slots.filter{
          case ts:TimeSlot=>
            !wishTimes.find(ts.isInTimeSlotCriteria).isEmpty

        }.map{
          ts=>
          ts.getLectures.filter(_.getDocents.contains(d)).head.asInstanceOf[Lecture]
        }

    }.toSet

    lectures.foreach{l=>
      l.increaseDifficultLevel()
      l.setNotOptimalPlaced("*")
    }
    (lectures.size, lectures)
  }
}
