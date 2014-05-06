package logic.generator.schedulegenerator.placingprocessor

import models.persistence.criteria.{DocentTimeWish, TimeSlotCriteria}
import models.persistence.scheduletree.TimeSlot
import models.persistence.location.RoomEntity
import models.persistence.lecture.Lecture
import scala.collection.JavaConversions._
import models.persistence.enumerations.EDocentTimeKind
import scala.annotation.tailrec

/**
 * @author fabian 
 *         on 29.04.14.
 */
class WeeklyLecturePlacer(availableTimeSlotCriterias: List[TimeSlotCriteria], availableTimeSlots: List[TimeSlot], allTimeslots: List[TimeSlot], availableRooms: List[RoomEntity]) extends PlacingProcessor {
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
        timeWishes.find(slot.isInTimeSlotCriteria).isEmpty
    })
  }

  @tailrec
  private def place(lecture: Lecture, timeSlots: List[TimeSlot]): Boolean = {
    if (timeSlots.isEmpty) {
      return false
    }
    val slot = timeSlots.head

    val notInTimeCriteria = !availableTimeSlotCriterias.isEmpty && availableTimeSlotCriterias.count(slot.isInTimeSlotCriteria) == 0

    val equivalent = findEquivalent(slot, allTimeslots)

    val equivalentNotAvailable = !availableTimeSlots.contains(equivalent)

    val roomCriterias = getRoomCriteriasFromDocents(lecture.getDocents.toList)

    var rooms = availableRooms.diff(slot.getLectures.flatMap(_.getRooms) ++ equivalent.getLectures.flatMap(_.getRooms)).filter(r => isRoomAvailableInTimeSlot(r, slot) && isRoomAvailableInTimeSlot(r, equivalent)).sortBy(_.getCapacity)
    if (!roomCriterias.isEmpty && roomsInCriteria(rooms,roomCriterias)) {
      rooms = sortRoomsByCriteria(rooms, roomCriterias)
    }

    val noRoom = rooms.isEmpty

    if (notInTimeCriteria || equivalentNotAvailable || noRoom) {
      place(lecture, timeSlots.tail)
    } else {
      lecture.setRoom(rooms.head)
      slot.setLectures(slot.getLectures :+ lecture)
      equivalent.setLectures(equivalent.getLectures :+ lecture)
      true
    }
  }
}
