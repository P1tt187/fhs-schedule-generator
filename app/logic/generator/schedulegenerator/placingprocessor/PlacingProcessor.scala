package logic.generator.schedulegenerator.placingprocessor

import models.persistence.criteria.{DocentTimeWish, RoomCriteria, TimeSlotCriteria}
import models.persistence.docents.LectureDocent
import models.persistence.enumerations.EDuration
import models.persistence.lecture.{AbstractLecture, Lecture}
import models.persistence.location.{LectureRoom, RoomEntity}
import models.persistence.participants.{Course, Group, Participant}
import models.persistence.scheduletree.TimeSlot

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * @author fabian 
 *         on 29.04.14.
 */
trait PlacingProcessor {

  protected def filterRoomsWithCapacity(rooms: List[RoomEntity], lecture: Lecture) = rooms.par.filter(_.getCapacity >= lecture.calculateNumberOfParticipants()).toList

  def doPlacing(lecture: Lecture): Boolean


  protected def findPossibleTimeSlots(timeSlots: List[TimeSlot], lecture: AbstractLecture) = {
    timeSlots.filter {
      timeSlot =>
        !timeSlotContainsDocents(timeSlot, lecture.getDocents.toSet) && !timeSlotContainsParticipants(timeSlot, lecture.getParticipants.toSet)
    }
  }

  protected def getTimeCritsForLecture(lecture: Lecture) = {
    lecture.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[TimeSlotCriteria]).map { case tsc: TimeSlotCriteria => tsc}.toList
  }

  protected def timeSlotContainsParticipants(timeslot: TimeSlot, participants: Set[Participant]): Boolean = {

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

          if (containsInParentGroup(group, existingGroups) || containsInSubGroups(group, existingGroups) || participantsContainsOtherGroupType(group, existingGroups)) {
            return true
          }
        case course: Course => if (existingParticipant.filter(_.getCourse.equals(course)).nonEmpty) {
          return true
        }
      }

      checkRecursive(existingParticipant, lectureParticipant.tail)
    }

    if (timeslot.getLectures.isEmpty) {
      return false
    }

    checkRecursive(timeslot.getLectures.flatMap(_.getParticipants), participants)
  }

  @tailrec
  private def containsInParentGroup(group: Group, existingParticipants: mutable.Buffer[Participant]): Boolean = {

    if (group == null) {
      return false
    }

    if (existingParticipants.contains(group)) {
      return true
    }
    containsInParentGroup(group.getParent, existingParticipants)
  }


  private def participantsContainsOtherGroupType(group: Group, existingParticipants: mutable.Buffer[Participant]): Boolean = {

    val relevantParticipants = existingParticipants.filter(_.getCourse.equals(group.getCourse)).toList.asInstanceOf[List[Group]]
    if (relevantParticipants.isEmpty) {
      return false
    }

    relevantParticipants.filterNot(g => g.isSubGroupOf(group) || group.isSubGroupOf(g)).find(g => !g.getGroupType.equals(group.getGroupType)) match {
      case Some(_) => true
      case None => false
    }

  }

  private def containsInSubGroups(group: Group, participants: mutable.Buffer[Participant]): Boolean = {

    if (participants.contains(group)) {
      return true
    }

    if (group.getSubGroups == null || group.getSubGroups.isEmpty) {
      return false
    }

    group.getSubGroups.find(subgroup => containsInSubGroups(subgroup, participants)).nonEmpty
  }

  protected def timeSlotContainsDocents(timeslot: TimeSlot, docents: Set[LectureDocent]): Boolean = {
    if (timeslot.getLectures.isEmpty) {
      return false
    }

    @tailrec
    def checkRecursive(docents: Set[LectureDocent], existingDocents: mutable.Buffer[LectureDocent]): Boolean = {
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

  def findEquivalent(timeSlot: TimeSlot, allTimeslots: List[TimeSlot]): TimeSlot = {
    allTimeslots.find(t => t.getDuration != timeSlot.getDuration && t.compareTo(timeSlot) == 0).get
  }

  protected def filterTimeWishes(docents: Set[LectureDocent]): Set[TimeSlotCriteria] = {

    if (docents.size == 1) {
      return docents.head.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[TimeSlotCriteria]).toSet.asInstanceOf[Set[TimeSlotCriteria]]
    }

    @tailrec
    def checkTimeWishes(timeWish: DocentTimeWish, docents: Set[LectureDocent]): Boolean = {
      if (docents.isEmpty) {
        return true
      }
      if (docents.head.getCriteriaContainer.getCriterias.find {
        case timeCrit: DocentTimeWish =>
          timeWish.isInTimeSlotCriteria(timeCrit)
        case _ => false
      }.isEmpty) {
        false
      } else {
        checkTimeWishes(timeWish, docents.tail)
      }
    }

    docents.flatMap {
      d =>
        d.getCriteriaContainer.getCriterias.filter {
          case timeCrit: DocentTimeWish =>
            checkTimeWishes(timeCrit, docents - d)
          case _ => false
        }
    }.asInstanceOf[Set[TimeSlotCriteria]]

  }

  @tailrec
  private def filterRooms(criterias: Set[RoomCriteria], allRooms: List[RoomEntity], roomBuffer: Set[RoomEntity] = Set[RoomEntity]()): Set[RoomEntity] = {

    if (criterias.isEmpty) {
      return roomBuffer
    }

    val rCrit = criterias.head

    var rooms: List[RoomEntity] = null

    if (rCrit.getHouse != null) {
      rooms = allRooms.filter(_.getHouse.equals(rCrit.getHouse))
    }
    if (rCrit.getRoomAttributes != null && !rCrit.getRoomAttributes.isEmpty) {
      rooms = allRooms.filter(_.getRoomAttributes.containsAll(rCrit.getRoomAttributes))
    }
    if (rCrit.getRoom != null) {
      rooms = allRooms.filter(_.equals(rCrit.getRoom))
    }

    filterRooms(criterias.tail, allRooms, roomBuffer ++ rooms)
  }

  protected def filterRoomsForLecture(lecture: Lecture, allRooms: List[RoomEntity]): List[RoomEntity] = {

    val roomCriterias = lecture.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[RoomCriteria]).map {
      case rcrit: RoomCriteria => rcrit
    }
    if (roomCriterias.isEmpty) {
      return allRooms
    }

    filterRoomsWithCapacity(filterRooms(roomCriterias.toSet, allRooms).toList, lecture)
  }

  protected def prepareNextDuration(lectures: List[Lecture], lecture: Lecture) {

    lecture.increaseDifficultLevel()
    lectures.par.foreach {
      l =>
        if (l.getDuration != EDuration.WEEKLY) {
          l.setDuration(EDuration.UNWEEKLY)
        }
    }
  }

  protected def isRoomAvailableInTimeSlot(room: RoomEntity, timeslot: TimeSlot): Boolean = {
    val timeCriterias = room.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[TimeSlotCriteria]).toList.asInstanceOf[List[TimeSlotCriteria]]
    if (timeCriterias.isEmpty) {
      return true
    }

    !timeCriterias.find(timeslot.isInTimeSlotCriteria).isEmpty
  }

  protected def getRoomCriteriasFromDocents(docents: List[LectureDocent]) = {
    docents.flatMap {
      d => d.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[RoomCriteria]).toList.asInstanceOf[List[RoomCriteria]]
    }
  }

  /** to respect the favorite rooms of a Docent the rooms will be sorted by his given criterias */
  protected def sortRoomsByCriteria(rooms: List[RoomEntity], roomCriterias: List[RoomCriteria]): List[RoomEntity] = {
    rooms.sortBy {
      room =>
        (!isRoomInCriteria(room, roomCriterias), room.getCapacity)
    }
  }

  private def isRoomInCriteria(room: RoomEntity, roomCriterias: List[RoomCriteria]): Boolean = {
    roomCriterias.par.find {
      rc =>
        if (rc.getHouse != null && rc.getHouse.equals(room.getHouse)) {
          true
        } else if (rc.getRoom != null && rc.getRoom.equals(room)) {
          true
        } else if (rc.getRoomAttributes != null && !rc.getRoomAttributes.isEmpty && room.getRoomAttributes.containsAll(rc.getRoomAttributes)) {
          true
        } else {
          false
        }
    }.nonEmpty
  }

  protected def roomsInCriteria(rooms: List[RoomEntity], roomCriterias: List[RoomCriteria]): Boolean = {
    rooms.par.find {
      room =>
        isRoomInCriteria(room, roomCriterias)
    }.nonEmpty
  }

  protected def isAlternativeRoomsNotAvailable( rooms:List[RoomEntity], timeSlotRooms:List[RoomEntity] )={
    val availabilities = rooms.map {
      room =>
        timeSlotRooms.contains(room)
    }.toSet.toList.sorted
    
    if (availabilities.isEmpty) {
      false
    } else {
      availabilities.head
    }
  }
  
  protected def getClassRooms(lecture: Lecture) = {
    lecture.getParticipants.map { participant =>
      if (participant.getCourse.getClassRoom != null) {
        Some(participant.getCourse.getClassRoom)
      } else {
        None
      }

    }.filter(_.nonEmpty).map {
      case Some(room) => room
      case None => null
    }.toList
  }

  protected def sortRoomsWithClassRooms(rooms: List[RoomEntity], classRooms: List[LectureRoom]) = {
    /**
     * if classroom is available make it the first element
     */
    rooms.sortBy(r => (classRooms.find(_.compareTo(r) == 0).isEmpty, r.getCapacity))
  }

  implicit def convertRoomEntityToLecturRoom(roomEntity: RoomEntity): LectureRoom = {
    roomEntity.roomEntity2LectureRoom()
  }
}
