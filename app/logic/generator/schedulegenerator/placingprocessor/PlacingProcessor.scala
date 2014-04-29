package logic.generator.schedulegenerator.placingprocessor

import models.persistence.scheduletree.TimeSlot
import models.persistence.lecture.{AbstractLecture, Lecture}
import models.persistence.location.RoomEntity
import models.persistence.Docent
import scala.annotation.tailrec
import models.persistence.criteria.{RoomCriteria, TimeslotCriteria}
import scala.collection.JavaConversions._
import scala.collection.mutable
import models.persistence.participants.{Course, Group, Participant}
import models.persistence.enumerations.EDuration

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

  protected def timeSlotContainsParticipants(timeslot: TimeSlot, participants: Set[Participant]): Boolean = {
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
        case course: Course => if (!existingParticipant.filter(_.getCourse.equals(course)).isEmpty) {
          return true
        }
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

    if (participantsContainsOtherGroupType(group, participants)) {
      return true
    }

    if (participants.contains(group)) {
      return true
    }
    containsInParentGroup(group.getParent, participants)
  }


  private def participantsContainsOtherGroupType(group: Group, participants: mutable.Buffer[Participant]): Boolean = {
    val parentSubgroups = if (group.getParent != null) {
      group.getParent.getSubGroups
    } else {
      group.getCourse.getGroups
    }


    val otherGroupTypes = parentSubgroups.filter(_.getGroupType != group.getGroupType)
    if (!otherGroupTypes.isEmpty) {
      for (g <- otherGroupTypes) {
        if (participants.contains(g)) {
          return true
        }
      }
    }
    false
  }

  private def containsInSubGroups(group: Group, participants: mutable.Buffer[Participant]): Boolean = {
    if (participants.contains(group) || participantsContainsOtherGroupType(group, participants)) {
      return true
    }

    if (group.getSubGroups == null || group.getSubGroups.isEmpty) {
      return false
    }

    !group.getSubGroups.filter(subgroup => containsInSubGroups(subgroup, participants)).isEmpty
  }

  protected def timeSlotContainsDocents(timeslot: TimeSlot, docents: Set[Docent]): Boolean = {
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

  def findEquivalent(timeSlot: TimeSlot, allTimeslots: List[TimeSlot]): TimeSlot = {
    allTimeslots.find(t => t.getDuration != timeSlot.getDuration && t.compareTo(timeSlot) == 0).get
  }

  protected def filterTimeslotCriterias(docents: Set[Docent]) = {

    @tailrec
    def filterRecursive(docent: Set[Docent], docents: Set[Docent], timeslot: Set[TimeslotCriteria] = Set[TimeslotCriteria]()): Set[TimeslotCriteria] = {
      if (docent.isEmpty) {
        return timeslot
      }

      val timeCriterias = docents.flatMap {
        d =>
          if (docent.head == d) {
            Set[TimeslotCriteria]()
          } else {

            val allTimeslotCriterias = d.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[TimeslotCriteria]).toSet.asInstanceOf[Set[TimeslotCriteria]]
            if (allTimeslotCriterias.isEmpty) {
              docent.head.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[TimeslotCriteria]).toSet.asInstanceOf[Set[TimeslotCriteria]]
            } else {

              docent.head.getCriteriaContainer.getCriterias.filter {
                case _: RoomCriteria => false
                case tcrit: TimeslotCriteria => allTimeslotCriterias.count(_.isInTimeslotCriteria(tcrit)) > 0
              }.toSet.asInstanceOf[Set[TimeslotCriteria]]
            }
          }
      }

      filterRecursive(docent.tail, docents, timeslot ++ timeCriterias)
    }

    if (docents.size == 1) {
      docents.flatMap {
        docent =>
          docent.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[TimeslotCriteria])
      }.toSet.asInstanceOf[Set[TimeslotCriteria]]
    } else {
      filterRecursive(docents, docents)
    }
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


    val roomCriterias = (lecture.getCriteriaContainer.getCriterias ++ lecture.getDocents.flatMap(_.getCriteriaContainer.getCriterias)).filter(_.isInstanceOf[RoomCriteria]).map {
      case rcrit: RoomCriteria => rcrit
    }
    if (roomCriterias.isEmpty) {
      return allRooms
    }

    filterRoomsWithCapacity(filterRooms(roomCriterias.toSet, allRooms).toList,lecture)
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
    val timeCriterias = room.getCriteriaContainer.getCriterias.filter(_.isInstanceOf[TimeslotCriteria]).toList.asInstanceOf[List[TimeslotCriteria]]
    if (timeCriterias.isEmpty) {
      return true
    }

    timeCriterias.count(timeslot.isInTimeslotCriteria) > 0
  }

}
