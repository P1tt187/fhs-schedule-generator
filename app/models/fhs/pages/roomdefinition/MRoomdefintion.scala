package models.fhs.pages.roomdefinition

import models.Transactions
import org.hibernate.criterion.Restrictions
import models.persistence.template.WeekdayTemplate

/**
 * Created by fabian on 04.02.14.
 */
case class MRoomdefintion(capacity: Int, house: String, number: Int, pcpools: Boolean, beamer: Boolean, timeCriterias: List[MTtimeslotCritDefine])

object MRoomdefintion {
  def findWeekdayBySortIndex(sortIndex: Int): WeekdayTemplate = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[WeekdayTemplate]).add(Restrictions.eq("sortIndex", sortIndex)).uniqueResult().asInstanceOf[WeekdayTemplate]
    }
  }
}
