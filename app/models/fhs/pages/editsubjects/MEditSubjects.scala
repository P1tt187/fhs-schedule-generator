package models.fhs.pages.editsubjects

import models.{Semester, Transactions}
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 06.03.14.
 */

object MEditSubjects{
  def findSemesters()={
    Transactions.hibernateAction{
      implicit session =>
        session.createCriteria(classOf[Semester]).list().asInstanceOf[java.util.List[Semester]].map(_.getName).toList
    }
  }
}


