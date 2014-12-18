package models.fhs.pages.editcourses

import controllers.CEditCourses
import models.Transactions
import models.persistence.location.{LectureRoom, RoomEntity}
import models.persistence.participants.{Course, Group, Student}
import models.persistence.subject.AbstractSubject
import org.hibernate.FetchMode
import org.hibernate.criterion.{CriteriaSpecification, Order, Projections, Restrictions}

import scala.collection.JavaConversions._


/**
 * @author fabian 
 *         on 14.03.14.
 */
object MEditCourses {

  def findCourses() = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Course]).setFetchMode("students", FetchMode.SELECT).addOrder(Order.asc("shortName")).list().asInstanceOf[java.util.List[Course]].toList
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
        criterion.createCriteria("courses").add(Restrictions.idEq(courseId)).setFetchMode("students", FetchMode.SELECT)
        criterion.list().toList.asInstanceOf[List[AbstractSubject]]
    }
  }

  def removeCourse(courseId: Long) {
    Transactions.hibernateAction {
      implicit s =>
        s.delete(s.createCriteria(classOf[Course]).add(Restrictions.idEq(courseId)).uniqueResult())
    }
  }

  def findRooms() = {
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[RoomEntity]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().toList.asInstanceOf[List[RoomEntity]]
    }
  }

  def findRoom(id: Long) = {
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[RoomEntity]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[RoomEntity]
    }
  }

  implicit def roomEntity2LectureRoom(roomEntity: RoomEntity): LectureRoom = {
    if (roomEntity == null) {
      null
    } else {
      roomEntity.roomEntity2LectureRoom()
    }
  }

  def createStudentsForCourse(course: Course) = {
    val students = (1 to course.getSize).toSet.map {
      _: Int =>
        new Student
    }
    Transactions.hibernateAction {
      implicit s =>
        s.saveOrUpdate(course)
        course.setStudents(students)
        students.foreach(s.saveOrUpdate(_))
        s.saveOrUpdate(course)

        val groupTypes = course.getGroups.map(_.getGroupType).toSet

        groupTypes.foreach {
          gType =>
            val groups = course.getGroups.filter(_.getGroupType.equals(gType))
            val parts = CEditCourses.cut(students.toSeq, groups.size).toList

            (0 to groups.size - 1).foreach {
              i =>
                groups(i).setStudents(new java.util.HashSet[Student](parts(i)))
                groups(i).setSize(groups(i).getStudents.size())
                s.saveOrUpdate(groups(i))
            }
        }
    }
  }

  def deleteStudentsFromCourse(course: Course): Unit = {
    Transactions.hibernateAction {
      implicit s =>
        s.saveOrUpdate(course)

        course.getGroups.foreach {
          group =>
            group.setStudents(Set[Student]())
            s.saveOrUpdate(group)
        }

        val students = course.getStudents
        course.setStudents(Set[Student]())
        students.foreach { student =>
          s.saveOrUpdate(student)
          s.delete(student)
        }

        s.saveOrUpdate(course)
    }
  }

  def findStudentById(studentId: Long): Student = {
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[Student]).add(Restrictions.idEq(studentId)).uniqueResult().asInstanceOf[Student]
    }
  }

  def updateStudent(student: Student) = {
    Transactions.hibernateAction {
      implicit s =>
        s.saveOrUpdate(student)
    }
  }

  def findStudentsForCourse(courseId: Long): List[Student] = {
    Transactions.hibernateAction {
      implicit s =>
        val course = s.createCriteria(classOf[Course]).add(Restrictions.idEq(courseId)).uniqueResult().asInstanceOf[Course]
        course.getStudents.toList
    }
  }

}

case class MCourse(longName: String, shortName: String, size: Int)