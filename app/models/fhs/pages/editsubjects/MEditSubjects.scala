package models.fhs.pages.editsubjects

import models.Transactions
import scala.collection.JavaConversions._
import models.persistence.subject.{ExerciseSubject, LectureSubject}
import org.hibernate.criterion.{Order, CriteriaSpecification, Restrictions}
import models.persistence.{Semester, Docent}
import models.persistence.participants.Course
import models.persistence.location.{RoomEntity, HouseEntity, RoomAttributesEntity}
import org.hibernate.FetchMode

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

  def findLectureSubjectsForSemester(semester: String, filterDocentId: Long = -1, filterCourseId: Long = -1, filterActive: String) = {

    val semesterDO = findSemester(semester)

    Transactions.hibernateAction {
      implicit session =>
        val criterion = session.createCriteria(classOf[LectureSubject]).add(Restrictions.eq("semester", semesterDO)).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).addOrder(Order.asc("name"))

        criterion.setFetchMode("criteriaContainer",FetchMode.SELECT)
        if (filterDocentId != -1) {
          criterion.createCriteria("docents").setFetchMode("criteriaContainer",FetchMode.SELECT).add(Restrictions.idEq(filterDocentId))
        }

        if (filterCourseId != -1) {
          criterion.createCriteria("courses").add(Restrictions.idEq(filterCourseId))
        }

        if (!filterActive.equals("-1")) {
          criterion.add(Restrictions.eq("active", filterActive.toBoolean))
        }

        criterion.list().asInstanceOf[java.util.List[LectureSubject]].
          map(element => MSubjects(element.getId, element.getName)).toList
    }
  }

  def findExerciseSubjectsForSemester(semester: String, filterDocentId: Long = -1, filterCourseId: Long = -1, filterActive: String) = {
    val semesterDO = findSemester(semester)

    Transactions.hibernateAction {
      implicit session =>
        val criterion = session.createCriteria(classOf[ExerciseSubject]).add(Restrictions.eq("semester", semesterDO)).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).addOrder(Order.asc("name"))

        if (filterDocentId != -1) {
          criterion.createCriteria("docents").add(Restrictions.idEq(filterDocentId))
        }

        if (filterCourseId != -1) {
          criterion.createCriteria("courses").add(Restrictions.idEq(filterCourseId))
        }

        if (!filterActive.equals("-1")) {
          criterion.add(Restrictions.eq("active", filterActive.toBoolean))
        }

        criterion.list().asInstanceOf[java.util.List[ExerciseSubject]].
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

  def findRoomAttributes(attributes: List[String]) = {
    attributes.map(findRoomAttribute)
  }

  def findRoomAttribute(attrName: String) = {
    Transactions.hibernateAction {
      implicit session =>
        val result = session.createCriteria(classOf[RoomAttributesEntity]).add(Restrictions.eq("attribute", attrName)).uniqueResult()

        result match {
          case roomAttr: RoomAttributesEntity => roomAttr
          case null => val roomAttr = new RoomAttributesEntity()
            roomAttr.setAttribute(attrName)
            roomAttr
        }
    }
  }

  def findCourses(ids: List[Long]) = {
    ids.map {
      id =>
        Transactions.hibernateAction {
          implicit session =>
            session.createCriteria(classOf[Course]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[Course]
        }
    }
  }

  def findHouses() = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[HouseEntity]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).setFetchMode("rooms", FetchMode.SELECT).list().toList.asInstanceOf[List[HouseEntity]].sortBy(_.getName)
    }
  }

  def findSelectedHouses(ids: List[Long]) = {

    Transactions.hibernateAction {
      implicit session =>
        ids.map {
          id =>

            session.createCriteria(classOf[HouseEntity]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[HouseEntity]
        }
    }
  }

  def findRooms() = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[RoomEntity]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).setFetchMode("house.rooms", FetchMode.SELECT).setFetchMode("roomAttributes",FetchMode.SELECT).list().toList.asInstanceOf[List[RoomEntity]].sorted
    }
  }

  def findSelectedRooms(ids: List[Long]) = {
    Transactions.hibernateAction {
      implicit session =>
        ids.map {
          id =>
            session.createCriteria(classOf[RoomEntity]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[RoomEntity]
        }
    }
  }

}

case class MSubjects(id: Long, name: String)


