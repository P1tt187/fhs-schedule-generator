package models.fhs.pages.editschedule

import models.Transactions
import models.persistence.Schedule
import scala.collection.JavaConversions._

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

}
