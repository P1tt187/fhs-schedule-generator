package models.fhs.pages.editsubjects

import models.{Semester, Transactions}
import scala.collection.JavaConversions._
import models.persistence.subject.{ExerciseSubject, LectureSubject}
import org.hibernate.criterion.{Order, CriteriaSpecification, Restrictions}
import models.persistence.Docent
import models.persistence.participants.Course

/**
 * @author fabian 
 *         on 06.03.14.
 */

object MEditSubjects {
  def findSemesters() = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Semester]).list().asInstanceOf[java.util.List[Semester]].map(_.getName).toList
    }
  }

  def findSemester(semester: String) = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Semester]).add(Restrictions.eq("name", semester)).uniqueResult().asInstanceOf[Semester]
    }
  }

  def findLectureSubjectsForSemester(semester: String) = {

    val semesterDO = findSemester(semester)

    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[LectureSubject]).add(Restrictions.eq("semester", semesterDO)).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).addOrder(Order.asc("name")).list().asInstanceOf[java.util.List[LectureSubject]].
          map(element => MSubjects(element.getId, element.getName)).toList
    }
  }

  def findExersiseSubjectsForSemester(semester: String) = {
    val semesterDO = findSemester(semester)

    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[ExerciseSubject]).add(Restrictions.eq("semester", semesterDO)).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).addOrder(Order.asc("name")).list().asInstanceOf[java.util.List[ExerciseSubject]].
          map(element => MSubjects(element.getId, element.getName)).toList
    }
  }

  def findSubject[T](clazz: Class[T], id: Long) = {
    Transactions.hibernateAction {
      implicit session =>
        clazz.cast(session.createCriteria(clazz).add(Restrictions.idEq(id)).uniqueResult())
    }
  }

  def findDocents() = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Docent]).addOrder(Order.asc("lastName")).list().asInstanceOf[java.util.List[Docent]].toList
    }
  }

  def findDocents(ids: List[Long]) = {
    ids.map {
      id =>
        Transactions.hibernateAction {
          implicit session =>
            session.createCriteria(classOf[Docent]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[Docent]
        }
    }
  }

  def findCourses() = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Course]).addOrder(Order.asc("shortName")).list().asInstanceOf[java.util.List[Course]].toList
    }
  }

  def findCourses(ids:List[Long])={
    ids.map{
      id =>
        Transactions.hibernateAction {
          implicit session =>
            session.createCriteria(classOf[Course]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[Course]
        }
    }
  }

}

case class MSubjects(id: Long, name: String)


