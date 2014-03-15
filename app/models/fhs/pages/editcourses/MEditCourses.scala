package models.fhs.pages.editcourses

import models.Transactions
import models.persistence.participants.Course
import org.hibernate.criterion.{Restrictions, Order}
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 14.03.14.
 */
object MEditCourses {

  def findCourses() = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Course]).addOrder(Order.asc("shortName")).list().asInstanceOf[java.util.List[Course]].toList
    }
  }

  def findCourse(courseId:Long)={
    Transactions.hibernateAction{
      implicit session =>
        session.createCriteria(classOf[Course]).add(Restrictions.idEq(courseId)).uniqueResult().asInstanceOf[Course]
    }
  }

  def updateCourse(course:Course){
    Transactions{
      implicit em =>
        em.merge(course)
    }
  }

}
