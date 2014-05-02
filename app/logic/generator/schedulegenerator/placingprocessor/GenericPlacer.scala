package logic.generator.schedulegenerator.placingprocessor

import models.persistence.lecture.Lecture
import models.persistence.scheduletree.TimeSlot
import models.persistence.location.RoomEntity
import models.persistence.enumerations.EDuration
import scala.collection.JavaConversions._
import scala.annotation.tailrec
import scala.util.Random
import play.api.Logger

/**
 * @author fabian 
 *         on 29.04.14.
 */
class GenericPlacer(allLectures: List[Lecture], allTimeslots: List[TimeSlot], allRooms: List[RoomEntity]) extends PlacingProcessor {

  def place() = {
    placing(allLectures)
  }

  private var placed=0

  @tailrec
  private def placing(lectures: List[Lecture]): Boolean = {
    if (lectures.isEmpty) {
      return true
    }
    placed+=1
    if (!doPlacing(lectures.head)) {
      Logger.debug("placed: " + placed + " Chancel: " + lectures.head.getName )
      false
    } else {
      placing(lectures.tail)
    }
  }

  override def doPlacing(lecture: Lecture): Boolean = {

    val availableRooms = filterRoomsForLecture(lecture, allRooms)
    val availableTimeSlotCriterias = filterTimeslotCriterias(lecture.getDocents.toSet).toList
    val availableTimeSlots = Random.shuffle(findPossibleTimeSlots(allTimeslots, lecture))


    val result = lecture.getDuration match {
      case EDuration.WEEKLY =>
        new WeeklyLecturePlacer(availableTimeSlotCriterias, availableTimeSlots, allTimeslots, availableRooms).doPlacing(lecture)
      case EDuration.UNWEEKLY =>
        new UnweeklyLecturePlacer(availableTimeSlotCriterias, availableTimeSlots, allTimeslots, availableRooms).doPlacing(lecture)
      case _ => false
    }

    if (!result) {
      prepareNextDuration(allLectures, lecture)
    }
    result
  }
}
