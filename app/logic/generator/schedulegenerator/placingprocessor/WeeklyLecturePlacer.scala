package logic.generator.schedulegenerator.placingprocessor

import models.persistence.criteria.{DocentTimeWish, TimeSlotCriteria}
import models.persistence.enumerations.EDocentTimeKind
import models.persistence.lecture.Lecture
import models.persistence.location.RoomEntity
import models.persistence.scheduletree.TimeSlot

import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 29.04.14.
 */
class WeeklyLecturePlacer(availableTimeSlotCriterias: List[TimeSlotCriteria],lectureTimeCriterias:List[TimeSlotCriteria], availableTimeSlots: List[TimeSlot], allTimeslots: List[TimeSlot], availableRooms: List[RoomEntity]) extends PlacingProcessor {
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
        (timeWishes.find(slot.isInTimeSlotCriteria).isEmpty, slot.isUnpopular)
    })
  }

  @tailrec
  private def place(lecture: Lecture, timeSlots: List[TimeSlot]): Boolean = {
    if (timeSlots.isEmpty) {
      return false
    }
    val slot = timeSlots.head

    val notInTimeCriteria = !availableTimeSlotCriterias.isEmpty && availableTimeSlotCriterias.count(slot.isInTimeSlotCriteria) == 0

    val notInLectureTimeCriteria = !lectureTimeCriterias.isEmpty && lectureTimeCriterias.count(slot.isInTimeSlotCriteria) == 0

    val equivalent = findEquivalent(slot, allTimeslots)

    val equivalentNotAvailable = !availableTimeSlots.contains(equivalent)

    val roomCriterias = getRoomCriteriasFromDocents(lecture.getDocents.toList)

    val timeSlotRooms = slot.getLectures.flatMap(_.getRooms) ++ equivalent.getLectures.flatMap(_.getRooms)

    var rooms = availableRooms.diff(timeSlotRooms).filter(r => isRoomAvailableInTimeSlot(r, slot) && isRoomAvailableInTimeSlot(r, equivalent)).sortBy(_.getCapacity)


    var alternativeRoomsNotAvailable = false

    lecture.getAlternativeRooms.foreach {
      room =>
        if (timeSlotRooms.contains(room)) {
          alternativeRoomsNotAvailable = true
        }
    }

    rooms = rooms.diff(lecture.getAlternativeRooms)

    if (!roomCriterias.isEmpty && roomsInCriteria(rooms, roomCriterias)) {
      rooms = sortRoomsByCriteria(rooms, roomCriterias)
    }

    val noRoom = rooms.isEmpty

    if (notInTimeCriteria || notInLectureTimeCriteria || equivalentNotAvailable || noRoom || alternativeRoomsNotAvailable) {
      place(lecture, timeSlots.tail)
    } else {
      lecture.setRoom(rooms.head)
      slot.setLectures(slot.getLectures :+ lecture)
      equivalent.setLectures(equivalent.getLectures :+ lecture)
      true
    }
  }
}
