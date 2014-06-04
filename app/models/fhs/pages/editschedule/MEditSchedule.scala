package models.fhs.pages.editschedule

import models.Transactions
import models.persistence.Schedule
import scala.collection.JavaConversions._
import models.persistence.location.RoomEntity
import org.hibernate.criterion.CriteriaSpecification

/**
 * @author fabian 
 *         on 03.06.14.
 */
object MEditSchedule {

  def findSemestersWithSchedule = {
    Transactions.hibernateAction{
      implicit s=>
        val schedules = s.createCriteria(classOf[Schedule]).list().toList.asInstanceOf[List[Schedule]]
        schedules.map(_.getSemester).toSet
    }
  }


  def findAllRooms:List[RoomEntity] = {
    Transactions.hibernateAction{
      implicit s=>
        s.createCriteria(classOf[RoomEntity]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().toList.asInstanceOf[List[RoomEntity]]
    }
  }
}
