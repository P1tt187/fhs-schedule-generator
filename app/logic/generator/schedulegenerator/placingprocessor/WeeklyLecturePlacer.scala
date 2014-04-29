package logic.generator.schedulegenerator.placingprocessor

import models.persistence.criteria.TimeSlotCriteria
import models.persistence.scheduletree.TimeSlot
import models.persistence.location.RoomEntity
import models.persistence.lecture.Lecture
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 29.04.14.
 */
class WeeklyLecturePlacer(availableTimeSlotCriterias: List[TimeSlotCriteria], availableTimeSlots: List[TimeSlot], allTimeslots: List[TimeSlot], availableRooms: List[RoomEntity]) extends PlacingProcessor {
  override def doPlacing(lecture: Lecture): Boolean = {
    place(lecture,availableTimeSlots)
  }


  private def place(lecture: Lecture, timeSlots: List[TimeSlot]): Boolean = {
    if (timeSlots.isEmpty) {
      return false
    }
    val slot = timeSlots.head

    if (!availableTimeSlotCriterias.isEmpty && availableTimeSlotCriterias.count(slot.isInTimeSlotCriteria) == 0) {
      return place(lecture, timeSlots.tail)
    }

    val equivalent = findEquivalent(slot, allTimeslots)

    //if (timeSlotContainsDocents(equivalent, lecture.getDocents.toSet) || timeSlotContainsParticipants(equivalent, lecture.getParticipants.toSet)) {
    if(!availableTimeSlots.contains(equivalent)){
      return place(lecture, timeSlots.tail)
    }

    val rooms = availableRooms.diff(slot.getLectures.flatMap(_.getRooms) ++ equivalent.getLectures.flatMap(_.getRooms)).filter(r => isRoomAvailableInTimeSlot(r, slot) && isRoomAvailableInTimeSlot(r, equivalent)).sortBy(_.getCapacity)
    if (rooms.isEmpty) {
      return place(lecture, timeSlots.tail)
    }

    lecture.setRoom(rooms.head)
    slot.setLectures(slot.getLectures :+ lecture)
    equivalent.setLectures(equivalent.getLectures :+ lecture)
    true
  }
}
