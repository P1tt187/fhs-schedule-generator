package models.fhs.pages.editsubjects

import models.{Semester, Transactions}
import scala.collection.JavaConversions._
import models.persistence.subject.{ExersiseSubject, LectureSubject}
import org.hibernate.criterion.{CriteriaSpecification, Restrictions}

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
        session.createCriteria(classOf[LectureSubject]).add(Restrictions.eq("semester", semesterDO)).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[java.util.List[LectureSubject]].
          map(element => MSubjects(element.getId, element.getName)).toList.sortWith(_ < _)
    }
  }

  def findExersiseSubjectsForSemester(semester: String) = {
    val semesterDO = findSemester(semester)

    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[ExersiseSubject]).add(Restrictions.eq("semester", semesterDO)).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[java.util.List[ExersiseSubject]].
          map(element => MSubjects(element.getId, element.getName)).toList.sortWith(_ < _)
    }
  }

  def findSubject[T](clazz:Class[T], id:Long)={
    Transactions.hibernateAction{
      implicit session=>
       clazz.cast(session.createCriteria(clazz).add(Restrictions.idEq(id)).uniqueResult())
    }
  }


}

case class MSubjects(id: Long, name: String) extends Ordered[MSubjects] {
  override def compare(that: MSubjects): Int = this.name.compareTo(that.name)
}

case class MDocent(id: Long, name: String) extends Ordered[MDocent] {
  override def compare(that: MDocent): Int = this.name.compareTo(that.name)
}
