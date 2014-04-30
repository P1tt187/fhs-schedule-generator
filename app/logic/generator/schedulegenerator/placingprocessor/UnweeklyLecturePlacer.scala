package logic.generator.schedulegenerator.placingprocessor

import models.persistence.scheduletree.TimeSlot
import models.persistence.location.RoomEntity
import models.persistence.criteria.{DocentTimeWish, TimeSlotCriteria}
import models.persistence.lecture.Lecture
import scala.collection.JavaConversions._
import models.persistence.enumerations.EDocentTimeKind
import scala.annotation.tailrec

/**
 * @author fabian 
 *         on 29.04.14.
 */
class UnweeklyLecturePlacer(availableTimeSlotCriterias: List[TimeSlotCriteria], availableTimeSlots: List[TimeSlot], allTimeslots: List[TimeSlot], availableRooms: List[RoomEntity]) extends PlacingProcessor {
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
        (timeWishes.find(slot.isInTimeSlotCriteria(_)).isEmpty, equivalent.getLectures.find(_.getDocents.containsAll(lecture.getDocents)).isEmpty, !(timeSlotContainsParticipants(equivalent, lecture.getParticipants.toSet) || equivalent.getLectures.find(_.getParticipants.containsAll(lecture.getParticipants)).isEmpty))
    })
  }

  @tailrec
  private def place(lecture: Lecture, timeSlots: List[TimeSlot]): Boolean = {
    if (timeSlots.isEmpty) {
      return false
    }

    val slot = timeSlots.head

    val notInTimeCriteria = !availableTimeSlotCriterias.isEmpty && availableTimeSlotCriterias.count(slot.isInTimeSlotCriteria) == 0

    val rooms = availableRooms.diff(slot.getLectures.flatMap(_.getRooms)).filter(isRoomAvailableInTimeSlot(_, slot)).sortBy(_.getCapacity)

    val noRoom = rooms.isEmpty
    if (notInTimeCriteria || noRoom) {
      place(lecture, timeSlots.tail)
    } else {
      lecture.setRoom(rooms.head)
      lecture.setDuration(slot.getDuration)
      slot.setLectures(slot.getLectures :+ lecture)
      true
    }

  }
}
