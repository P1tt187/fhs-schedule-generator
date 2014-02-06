package models.fhs.pages.roomdefinition

import models.persistence.scheduletree.Weekday
import models.Transactions
import org.hibernate.criterion.Restrictions

/**
 * Created by fabian on 04.02.14.
 */
case class MRoomdefintion(capacity: Int, house: String, number: Int, pcpools: Boolean, beamer: Boolean, timeCriterias: List[MTtimeslotCritDefine])

object MRoomdefintion {
  def findWeekdayBySortIndex(sortIndex: Int): Weekday = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Weekday]).add(Restrictions.eq("sortIndex", sortIndex)).uniqueResult().asInstanceOf[Weekday]
    }
  }
}
