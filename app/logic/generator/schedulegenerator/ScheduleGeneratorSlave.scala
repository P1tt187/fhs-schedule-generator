package logic.generator.schedulegenerator

import akka.actor.Actor
import models.persistence.scheduletree.{Timeslot, Weekday, Root}
import models.Transactions
import models.persistence.location.RoomEntity
import org.hibernate.criterion.CriteriaSpecification
import models.fhs.pages.JavaList
import scala.collection.JavaConversions._
import models.persistence.template.{TimeslotTemplate, WeekdayTemplate}
import models.persistence.{Schedule, Docent}
import models.persistence.participants.{Group, Participant}
import models.persistence.lecture.{Lecture, AbstractLecture}
import play.api.Logger
import scala.util.Random
import scala.annotation.tailrec
import scala.collection.mutable
import models.persistence.enumerations.EDuration


/**
 * @author fabian 
 *         on 23.03.14.
 */
class ScheduleGeneratorSlave extends Actor {

  private var placed = 0

  private var notPlaced = 0

  private var noRoom = 0

  override def receive = {

    case SlaveGenerate(lectures) =>

      Logger.debug("number of lectures: " + lectures.size)

      val rooms = Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[RoomEntity]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[RoomEntity]].toList
      }

      val weekdays: List[Weekday] = Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[WeekdayTemplate]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[WeekdayTemplate]].toList.sortBy(_.getSortIndex)
      }

      val root = new Root

      root.setChildren(weekdays)

      lectures.foreach {
        lecture =>

          if(lecture.getDuration == EDuration.WEEKLY){

          }

        //TODO filter weekdays
          val possibleTimeslots = findPossibleTimeslots(root, lecture)
          initTimeslotAndRoom(lecture, Random.shuffle(possibleTimeslots.toList), rooms.filter(_.getCapacity >= lecture.getParticipants.map(_.getSize.toInt).sum))
      }

      val schedule = new Schedule

      schedule.setRoot(root)
      Logger.debug("placed: " + placed + ", not placed: " + notPlaced + ", no room: " + noRoom)

      sender() ! ScheduleAnswer(schedule)


    case _ =>
  }


  private def findPossibleTimeslots(root: Root, lecture: AbstractLecture)= {
     root.getChildren.flatMap {
      weekday =>
        weekday.getChildren.toList.asInstanceOf[List[Timeslot]].filter {
          //TODO filter with timecriterias
          timeslot =>
            !timeslotContainsDocents(timeslot, lecture.getDocents.toSet) && !timeslotContainsParticipants(timeslot, lecture.getParticipants.toSet)
        }
    }
  }

  @tailrec
  private def initTimeslotAndRoom(lecture: Lecture, possibleTimeslots: List[Timeslot], rooms: List[RoomEntity]) {
    //TODO filter with room criterias
    possibleTimeslots.headOption match {
      case None => Logger.warn("cannot place " + lecture + " no timeslots available")
        notPlaced += 1
      case Some(timeslot) =>
        val possibleRooms = rooms.diff(timeslot.getLectures.flatMap(_.getRooms))
        if (possibleRooms.isEmpty) {
          Logger.debug("no room in timeslot " + timeslot + " for lecture " + lecture)
          noRoom += 1
          initTimeslotAndRoom(lecture, possibleTimeslots.tail, rooms)
        } else {
          lecture.setRoom(possibleRooms.head)
          timeslot.setLectures(timeslot.getLectures :+ lecture)
          placed += 1
        }


    }
  }

  private def timeslotContainsParticipants(timeslot: Timeslot, participants: Set[Participant]): Boolean = {
    if (timeslot.getLectures.isEmpty) {
      return false
    }

    @tailrec
    def checkRecursive(existingParticipant: mutable.Buffer[Participant], lectureParticipant: Set[Participant]): Boolean = {
      if (lectureParticipant.isEmpty) {
        return false
      }
      if (existingParticipant.contains(lectureParticipant.head) || existingParticipant.contains(lectureParticipant.head.getCourse)) {
        return true
      }
      lectureParticipant.head match {
        case group: Group =>
          val existingGroups = existingParticipant.filter(_.isInstanceOf[Group])

          if (containsInParentGroup(group, existingGroups) || containsInSubGroups(group, existingGroups)) {
            return true
          }
        case _ =>
      }

      checkRecursive(existingParticipant, lectureParticipant.tail)
    }

    checkRecursive(timeslot.getLectures.flatMap(_.getParticipants), participants)
  }

  @tailrec
  private def containsInParentGroup(group: Group, participants: mutable.Buffer[Participant]): Boolean = {
    if (group == null) {
      return false
    }

    if (participants.contains(group)) {
      return true
    }
    containsInParentGroup(group.getParent, participants)
  }

  private def containsInSubGroups(group: Group, participants: mutable.Buffer[Participant]): Boolean = {
    if (group.getSubGroups == null || group.getSubGroups.isEmpty) {
      return false
    }
    if(participants.contains(group)){
      return true
    }

    group.getSubGroups.filter(subgroup => containsInSubGroups(subgroup,participants)).isEmpty
  }

  private def timeslotContainsDocents(timeslot: Timeslot, docents: Set[Docent]): Boolean = {
    if (timeslot.getLectures.isEmpty) {
      return false
    }

    @tailrec
    def checkRecursive(docents: Set[Docent], existingDocents: mutable.Buffer[Docent]): Boolean = {
      if (existingDocents.isEmpty) {
        return false
      }
      if (docents.contains(existingDocents.head)) {
        return true
      }
      checkRecursive(docents, existingDocents.tail)
    }

    val existingDocents = timeslot.getLectures.flatMap(_.getDocents)

    checkRecursive(docents, existingDocents)

  }

  private implicit def weekdayTemplateList2WeekdayList(wl: List[WeekdayTemplate]): List[Weekday] = {
    wl.map(weekdayTemplate2Weekday)
  }

  private implicit def weekdayTemplate2Weekday(weekdayTemplate: WeekdayTemplate): Weekday = {
    val ret = new Weekday()

    ret.setName(weekdayTemplate.getName)
    ret.setSortIndex(weekdayTemplate.getSortIndex)
    ret.setChildren(weekdayTemplate.getChildren.map(timeslotTemplate2Timeslot(_, ret)).sorted)
    ret
  }

  private def timeslotTemplate2Timeslot(timeslotTemplate: TimeslotTemplate, weekday: Weekday): Timeslot = {
    val ret = new Timeslot
    ret.setStartHour(timeslotTemplate.getStartHour)
    ret.setStartMinute(timeslotTemplate.getStartMinute)
    ret.setStopHour(timeslotTemplate.getStopHour)
    ret.setStopMinute(timeslotTemplate.getStopMinute)
    ret.setParent(weekday)
    ret.setLectures(List[AbstractLecture]())
    ret
  }


}

