package logic.generator.schedulegenerator.placingprocessor

import models.persistence.scheduletree.TimeSlot
import models.persistence.location.RoomEntity
import models.persistence.criteria.TimeslotCriteria
import models.persistence.lecture.Lecture
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 29.04.14.
 */
class UnweeklyLecturePlacer(availableTimeSlotCriterias: List[TimeslotCriteria], availableTimeSlots: List[TimeSlot], allTimeslots: List[TimeSlot], availableRooms: List[RoomEntity]) extends PlacingProcessor {
  override def doPlacing(lecture: Lecture): Boolean = {
    // TODO sort TimeSlots better
    place(lecture, availableTimeSlots.sortBy {
      slot =>
        val equivalent = findEquivalent(slot, allTimeslots)
        (equivalent.getLectures.find(_.getDocents.containsAll(lecture.getDocents)).isEmpty, !(timeSlotContainsParticipants(equivalent, lecture.getParticipants.toSet)||equivalent.getLectures.find(_.getParticipants.containsAll(lecture.getParticipants)).isEmpty) )
    })
  }

  private def place(lecture: Lecture, timeSlots: List[TimeSlot]): Boolean = {
    if (timeSlots.isEmpty) {
      return false
    }

    val slot = timeSlots.head

    if (!availableTimeSlotCriterias.isEmpty && availableTimeSlotCriterias.count(slot.isInTimeslotCriteria) == 0) {
      return place(lecture, timeSlots.tail)
    }

    val rooms = availableRooms.diff(slot.getLectures.flatMap(_.getRooms)).sortBy(_.getCapacity)
    if (rooms.isEmpty) {
      return place(lecture, timeSlots.tail)
    }
    lecture.setRoom(rooms.head)
    lecture.setDuration(slot.getDuration)
    slot.setLectures(slot.getLectures :+ lecture)

    true
  }
}
