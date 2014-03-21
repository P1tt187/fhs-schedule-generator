package models.fhs.pages.generator

import models.Transactions
import org.hibernate.criterion.{Restrictions, CriteriaSpecification, Order}
import scala.collection.JavaConversions._
import models.persistence.Semester
import models.persistence.subject.AbstractSubject
import models.fhs.pages.JavaList


/**
 * @author fabian 
 *         on 20.03.14.
 */
object MGenerator {

  def findActiveSubjectsBySemesterId(id:Long)={
    Transactions.hibernateAction{
      implicit session =>
        val criterion = session.createCriteria(classOf[AbstractSubject]).add(Restrictions.eq("active",true)).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
        criterion.createCriteria("semester").add(Restrictions.idEq(id))
       // criterion.createCriteria("courses").setFetchMode("groups",FetchMode.JOIN)

        criterion.list().asInstanceOf[JavaList[AbstractSubject]].toList

    }
  }

  def findSemesters() = {
    Transactions.hibernateAction {
      implicit session =>
       session.createCriteria(classOf[Semester]).addOrder(Order.desc("name")).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[Semester]].toList


    }
  }

}

case class GeneratorForm(id:Long)