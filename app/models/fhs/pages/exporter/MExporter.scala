package models.fhs.pages.exporter

import scala.collection.JavaConversions._
import models.Transactions
import models.persistence.{Schedule, Semester}
import models.fhs.pages.JavaList
import org.hibernate.criterion.Restrictions

/**
 * @author fabian 
 *         on 22.05.14.
 */
object MExporter {

  def findSemesters:List[Semester] = {
    Transactions.hibernateAction{
      implicit s=>
        s.createCriteria(classOf[Schedule]).list().asInstanceOf[JavaList[Schedule]].toList.map(_.getSemester).toSet.toList
    }
  }

  def findSemester(id:Long):Semester = {
    Transactions.hibernateAction{
      implicit s=>
        s.createCriteria(classOf[Semester]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[Semester]
    }
  }

}
