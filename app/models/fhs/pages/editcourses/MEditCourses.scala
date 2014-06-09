package models.fhs.pages.editcourses

import models.Transactions
import models.persistence.participants.{Group, Course}
import org.hibernate.criterion.{CriteriaSpecification, Projections, Restrictions, Order}
import scala.collection.JavaConversions._
import org.hibernate.FetchMode
import models.persistence.subject.AbstractSubject


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

  def findCourse(courseId: Long) = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Course]).add(Restrictions.idEq(courseId)).setFetchMode("groups", FetchMode.JOIN).uniqueResult().asInstanceOf[Course]
    }
  }

  def updateCourse(course: Course) {
    Transactions {
      implicit em =>
        em.merge(course)
    }
  }

  def findGroup(groupId: Long) = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Group]).add(Restrictions.idEq(groupId)).uniqueResult().asInstanceOf[Group]
    }
  }

  def removeGroup(groupId: Long) {
    Transactions.hibernateAction {
      implicit session =>
        val group = session.createCriteria(classOf[Group]).add(Restrictions.idEq(groupId)).uniqueResult().asInstanceOf[Group]

        val course = group.getCourse
        if (course != null) {
          course.setGroups(course.getGroups - group)
        }
        group.setCourse(null)
        if (group.getParent != null) {
          val parent = group.getParent
          parent.setSubGroups(parent.getSubGroups - group)
          group.setParent(null)

          session.saveOrUpdate(parent)
        }

        session.delete(group)
        if (course != null) {
          session.saveOrUpdate(course)
        }
    }
  }

  def getGroupCount(groupType: String, course: Course) = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Group]).
          add(Restrictions.eq("course", course)).
          add(Restrictions.eq("groupType", groupType)).
          setProjection(Projections.rowCount()).uniqueResult().asInstanceOf[Long].toInt
    }
  }

  def findSubjectsWithCourse(courseId: Long): List[AbstractSubject] = {
    Transactions.hibernateAction {
      implicit s =>
        val criterion = s.createCriteria(classOf[AbstractSubject]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
        criterion.createCriteria("courses").add(Restrictions.idEq(courseId))
        criterion.list().toList.asInstanceOf[List[AbstractSubject]]
    }
  }

  def removeCourse(courseId: Long) {
    Transactions.hibernateAction {
      implicit s =>
        s.delete(s.createCriteria(classOf[Course]).add(Restrictions.idEq(courseId)).uniqueResult())
    }
  }

}

case class MCourse(longName: String, shortName: String, size: Int)