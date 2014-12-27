package logic.generator.schedulerater.rater

import models.Transactions
import models.persistence.criteria.DocentTimeWish
import models.persistence.docents.Docent
import models.persistence.enumerations.EDocentTimeKind
import models.persistence.lecture.Lecture
import models.persistence.scheduletree.TimeSlot
import org.hibernate.criterion.CriteriaSpecification
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 27.12.14.
 */
class UnpopularDocentWishTimeAllocation extends Rater {

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
            ts.getLectures.exists(_.getDocents.exists(_.compareTo(d) == 0))

        }

        slots.filter{
          case ts:TimeSlot=>
            wishTimes.exists(ts.isInTimeSlotCriteria)

        }.map{
          ts=>
            ts.getLectures.filter(_.getDocents.exists(_.compareTo(d) == 0)).head.asInstanceOf[Lecture]
        }

    }.toSet

    val unpopularDocents = lectures.flatMap{ l=>
      l.getDocents
    }.toSet
    (-unpopularDocents.size, lectures)
  }

}
