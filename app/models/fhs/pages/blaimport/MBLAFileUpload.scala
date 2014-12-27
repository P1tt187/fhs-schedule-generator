package models.fhs.pages.blaimport

import models.Transactions
import models.fhs.pages.JavaList
import models.persistence.participants.{Student, Course, Group}
import models.persistence.subject.AbstractSubject
import org.hibernate.FetchMode
import org.hibernate.criterion.Restrictions

import scala.collection.JavaConversions._
import play.api.Logger

/**
 * @author fabian 
 *         on 27.12.14.
 */
object MBLAFileUpload {

  def renameCourses() = {
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[Course]).list().asInstanceOf[JavaList[Course]].foreach {
          c =>
            val oldName = c.getShortName
            val oldChar = oldName.charAt(oldName.length - 1)
            val newChar = (oldChar.toInt + 1).toChar
            val newName = oldName.replace(oldChar, newChar)
            c.setShortName(newName)
            s.saveOrUpdate(c)
        }
    }
  }

  def findCourses() = {
    Transactions.hibernateAction{
      implicit s=>
        s.createCriteria(classOf[Course]).setFetchMode("students",FetchMode.SELECT).list().asInstanceOf[JavaList[Course]].toList.sortBy(_.getShortName)
    }
  }

  def removeOldCourses(courseIds:List[Long])={
    Transactions.hibernateAction{
      implicit s=>
        val courses = s.createCriteria(classOf[Course]).add(Restrictions.in("id",courseIds)).list().asInstanceOf[JavaList[Course]].toList

        var criterion = s.createCriteria(classOf[AbstractSubject])
        criterion.createCriteria("courses").add(Restrictions.in("id",courseIds))
        val subjects = criterion.list().asInstanceOf[JavaList[AbstractSubject]]

        subjects.foreach {
          sub =>
          sub.setCourses(sub.getCourses -- courses)
            s.saveOrUpdate(sub)
        }

        criterion = s.createCriteria(classOf[Group])
        criterion.createCriteria("course").add(Restrictions.in("id", courseIds))
/*
        val groups = criterion.list().asInstanceOf[JavaList[Group]]
        groups.foreach{
          g=>
            g.setCourse(null)
            s.saveOrUpdate(g)
            s.delete(g)
        }
*/
 //       s.flush()
        courses.foreach{
          c=>
            c.getGroups.foreach{
              g=>
               g.setCourse(null)
                s.saveOrUpdate(g)
                s.delete(g)
            }

            c.getStudents.foreach{
              stud=>
                s.delete(stud)
            }
            c.setStudents(Set[Student]())
            c.setGroups(List[Group]())
            s.saveOrUpdate(c)

            s.delete(c)
            s.flush()
        }

    }
  }

}

case class OldCourses(courseIds:List[Long])
