package logic.generator.schedulegenerator.placingprocessor

import models.persistence.criteria.{DocentTimeWish, TimeSlotCriteria}
import models.persistence.enumerations.EDocentTimeKind
import models.persistence.lecture.Lecture
import models.persistence.location.{LectureRoom, RoomEntity}
import models.persistence.scheduletree.TimeSlot

import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 29.04.14.
 */
class UnweeklyLecturePlacer(availableTimeSlotCriterias: List[TimeSlotCriteria], lectureTimeCriterias: List[TimeSlotCriteria], availableTimeSlots: List[TimeSlot], allTimeslots: List[TimeSlot], availableRooms: List[RoomEntity], classRooms:List[LectureRoom]) extends PlacingProcessor {
  override def doPlacing(lecture: Lecture): Boolean = {

    val timeWishes = lecture.getDocents.flatMap {
      docent =>
        docent.getCriteriaContainer.getCriterias.filter {
          case timeWish: DocentTimeWish => timeWish.getTimeKind == EDocentTimeKind.WISH
          case _ => false
        }.toList.asInstanceOf[List[DocentTimeWish]]
    }

    /** sorting with boolean values will order false as first element, so we invert the conditions */
    place(lecture, availableTimeSlots.sortBy {
      slot =>
        val equivalent = findEquivalent(slot, allTimeslots)
        (timeWishes.find(slot.isInTimeSlotCriteria(_)).isEmpty, slot.isUnpopular, equivalent.getLectures.find(_.getDocents.containsAll(lecture.getDocents)).isEmpty, !(timeSlotContainsParticipants(equivalent, lecture.getParticipants.toSet) || equivalent.getLectures.find(_.getParticipants.containsAll(lecture.getParticipants)).isEmpty))
    })
  }

  @tailrec
  private def place(lecture: Lecture, timeSlots: List[TimeSlot]): Boolean = {
    if (timeSlots.isEmpty) {
      return false
    }



    val slot = timeSlots.head

    val notInTimeCriteria = availableTimeSlotCriterias.nonEmpty && availableTimeSlotCriterias.count(slot.isInTimeSlotCriteria) == 0

    val notInLectureTimeCriteria = lectureTimeCriterias.nonEmpty && lectureTimeCriterias.count(slot.isInTimeSlotCriteria) == 0

    val roomCriterias = getRoomCriteriasFromDocents(lecture.getDocents.toList)

    val timeSlotRooms = slot.getLectures.flatMap(_.getRoomEntitys)

    var rooms = availableRooms.diff(timeSlotRooms).filter(isRoomAvailableInTimeSlot(_, slot)).sortBy(_.getCapacity)

    var alternativeRoomsNotAvailable = false

    lecture.getAlternativeRooms.foreach {
      room =>
        if (timeSlotRooms.contains(room)) {
          alternativeRoomsNotAvailable = true
        }
    }

    rooms = rooms.diff(lecture.getAlternativeRooms)

    rooms = sortRoomsWithClassRooms(rooms,classRooms)

    if (roomCriterias.nonEmpty && roomsInCriteria(rooms, roomCriterias)) {
      rooms = sortRoomsByCriteria(rooms, roomCriterias)
    }

    val noRoom = rooms.isEmpty
    if (notInTimeCriteria || notInLectureTimeCriteria || noRoom || alternativeRoomsNotAvailable) {
      place(lecture, timeSlots.tail)
    } else {
      val room = rooms.head
      lecture.setRoom(room)
      lecture.setLectureRoom(room)
      lecture.setDuration(slot.getDuration)
      slot.setLectures(slot.getLectures :+ lecture)
      true
    }

  }
}
