package models.fhs.pages.editsubjects

import models.Transactions
import models.fhs.pages.JavaList
import models.persistence.criteria.{AbstractCriteria, CriteriaContainer}
import models.persistence.docents.Docent
import models.persistence.location.{HouseEntity, RoomAttributesEntity, RoomEntity}
import models.persistence.participants.Course
import models.persistence.subject.{AbstractSubject, ExerciseSubject, LectureSubject}
import models.persistence.{Schedule, Semester}
import org.hibernate.FetchMode
import org.hibernate.criterion.{CriteriaSpecification, Order, Restrictions}
import play.api.Logger

import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 06.03.14.
 */

object MEditSubjects {
  def findSemesters() = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Semester]).list().asInstanceOf[java.util.List[Semester]].toList
    }
  }

  def findSemester(semester: String) = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Semester]).add(Restrictions.eq("name", semester)).uniqueResult().asInstanceOf[Semester]
    }
  }

  def findSemesterById(id: Long) = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Semester]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[Semester]
    }
  }


  def deleteLecturesAndSchedules(semester: Semester) {
    Transactions.hibernateAction {
      implicit session =>
        session.saveOrUpdate(semester)
        val schedules = session.createCriteria(classOf[Schedule]).add(Restrictions.eq("semester", semester)).list().asInstanceOf[JavaList[Schedule]]
        schedules.foreach(session.delete)
        val subjects = session.createCriteria(classOf[AbstractSubject]).add(Restrictions.eq("semester", semester)).list().asInstanceOf[JavaList[AbstractSubject]]
        subjects.foreach(session.delete)

        session.delete(semester)
    }
  }


  def removeSubject(subject:AbstractSubject){
    Transactions.hibernateAction{
      implicit s=>
        s.saveOrUpdate(subject)
        s.delete(subject)
    }
  }

  def findLectureSubjectsForSemester(semester: Long, filterDocentId: Long = -1, filterCourseId: Long = -1, filterActive: String) = {

    val semesterDO = findSemesterById(semester)

    Transactions.hibernateAction {
      implicit session =>
        val criterion = session.createCriteria(classOf[LectureSubject]).add(Restrictions.eq("semester", semesterDO)).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).addOrder(Order.asc("name"))

        criterion.setFetchMode("criteriaContainer", FetchMode.SELECT)
          .setFetchMode("subjectSynonyms",FetchMode.SELECT)
          .setFetchMode("shortCuts", FetchMode.SELECT)
        .setFetchMode("alternativRooms",FetchMode.SELECT)
        if (filterDocentId != -1) {
          criterion.createCriteria("docents").setFetchMode("criteriaContainer", FetchMode.SELECT).add(Restrictions.idEq(filterDocentId))
        }

        if (filterCourseId != -1) {
          criterion.createCriteria("courses").add(Restrictions.idEq(filterCourseId))
        }

        if (!filterActive.equals("-1")) {
          criterion.add(Restrictions.eq("active", filterActive.toBoolean))
        }

        criterion.list().asInstanceOf[java.util.List[LectureSubject]].par.
          map(element => MSubjects(element.getId, element.getName, element.getCourses.map(c => c.getShortName).mkString(" "))).toList
    }
  }

  def findExerciseSubjectsForSemester(semester: Long, filterDocentId: Long = -1, filterCourseId: Long = -1, filterActive: String) = {
    val semesterDO = findSemesterById(semester)

    Transactions.hibernateAction {
      implicit session =>
        val criterion = session.createCriteria(classOf[ExerciseSubject]).add(Restrictions.eq("semester", semesterDO)).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).addOrder(Order.asc("name"))

        criterion.setFetchMode("criteriaContainer", FetchMode.SELECT)
          .setFetchMode("subjectSynonyms",FetchMode.SELECT)
          .setFetchMode("shortCuts", FetchMode.SELECT)
          .setFetchMode("alternativRooms",FetchMode.SELECT)

        if (filterDocentId != -1) {
          criterion.createCriteria("docents").setFetchMode("criteriaContainer", FetchMode.SELECT).add(Restrictions.idEq(filterDocentId))
        }

        if (filterCourseId != -1) {
          criterion.createCriteria("courses").add(Restrictions.idEq(filterCourseId))
        }

        if (!filterActive.equals("-1")) {
          criterion.add(Restrictions.eq("active", filterActive.toBoolean))
        }

        criterion.list().asInstanceOf[java.util.List[ExerciseSubject]].par.
          map(element => MSubjects(element.getId, element.getName, element.getCourses.map(c => c.getShortName).mkString(" "))).toList
    }
  }

  def findSubject[T](clazz: Class[T], id: Long): T = {
    if (id == -1l) {
      Logger.debug("new instance")
      return clazz.newInstance()
    }

    Transactions.hibernateAction {
      implicit session =>
        clazz.cast(session.createCriteria(clazz).add(Restrictions.idEq(id)).uniqueResult())
    }
  }

  def initNewSubject(subject: AbstractSubject) {
    subject.setActive(true)
    subject.setName("")
    subject.setCourses(Set[Course]())
    val criteriaContainer = new CriteriaContainer
    criteriaContainer.setCriterias(List[AbstractCriteria]())
    subject.setCriteriaContainer(criteriaContainer)
    subject.setDocents(Set[Docent]())
    subject.setUnits(0.0f)

    subject match {
      case _: LectureSubject =>
      case exerciseSubject: ExerciseSubject => exerciseSubject.setGroupType("")
    }
  }

  def findDocents() = {
    Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[Docent]).setFetchMode("criteriaContainer", FetchMode.SELECT).addOrder(Order.asc("lastName")).list().asInstanceOf[java.util.List[Docent]].toList
    }
  }

  def findDocents(ids: List[Long]) = {
    ids.map {
      id =>
        Transactions.hibernateAction {
          implicit session =>
            session.createCriteria(classOf[Docent]).add(Restrictions.idEq(id)).setFetchMode("criteriaContainer", FetchMode.SELECT).uniqueResult().asInstanceOf[Docent]
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
        session.createCriteria(classOf[RoomEntity]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).setFetchMode("house.rooms", FetchMode.SELECT).setFetchMode("roomAttributes", FetchMode.SELECT).list().toList.asInstanceOf[List[RoomEntity]].sorted
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

case class MSubjects(id: Long, name: String, participants: String)

case class MSemester (name:String)


