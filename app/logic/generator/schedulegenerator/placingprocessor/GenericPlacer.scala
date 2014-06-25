package logic.generator.schedulegenerator.placingprocessor

import exceptions.{DocentsNotAtSameTimeAvailableException, NoRoomException}
import models.persistence.criteria.DocentTimeWish
import models.persistence.enumerations.EDuration
import models.persistence.lecture.Lecture
import models.persistence.location.RoomEntity
import models.persistence.scheduletree.TimeSlot

import scala.annotation.tailrec
import scala.collection.JavaConversions._


/**
 * @author fabian 
 *         on 29.04.14.
 */
class GenericPlacer(allLectures: List[Lecture], allTimeslots: List[TimeSlot], allRooms: List[RoomEntity]) extends PlacingProcessor {

  def place() = {
    placing(allLectures)
  }

  private var placed = 0

  @tailrec
  private def placing(lectures: List[Lecture]): Boolean = {
    if (lectures.isEmpty) {
      return true
    }
    placed += 1
    if (!doPlacing(lectures.head)) {
      //  Logger.debug("placed: " + placed + " Chancel: " + lectures.head.getName + " " + lectures.head.getDuration /*+ " difficult: " + lectures.head.getDifficulty*/)
      false
    } else {
      placing(lectures.tail)
    }
  }

  override def doPlacing(lecture: Lecture): Boolean = {

    val availableRooms = filterRoomsForLecture(lecture, allRooms)
    val classRooms = getClassRooms(lecture)
    val availableTimeSlotCriterias = filterTimeWishes(lecture.getDocents.toSet).toList
    //val availableTimeSlots = Random.shuffle(findPossibleTimeSlots(allTimeslots, lecture))
    val availableTimeSlots = findPossibleTimeSlots(allTimeslots, lecture)
    val lectureTimeslotCriterias = getTimeCritsForLecture(lecture)

    if (availableRooms.isEmpty) {
      val ex = new NoRoomException("no room for lecture " + lecture.getName + " " + lecture.getDocents.mkString(", "))
      ex.setLecture(lecture)
      throw ex
    }

    val areThereDocentTimeWishes = lecture.getDocents.find{ d=> d.getCriteriaContainer.getCriterias.find(_.isInstanceOf[DocentTimeWish]).nonEmpty }.nonEmpty
    if(availableTimeSlotCriterias.isEmpty && areThereDocentTimeWishes){
      val ex  = new DocentsNotAtSameTimeAvailableException("Docents not at the same time available " + lecture.getDocents.map(_.getLastName).mkString(", "))
      ex.setDocents(lecture.getDocents.toList)
      throw ex
    }

    val result = lecture.getDuration match {
      case EDuration.WEEKLY =>
        new WeeklyLecturePlacer(availableTimeSlotCriterias, lectureTimeslotCriterias, availableTimeSlots, allTimeslots, availableRooms,classRooms).doPlacing(lecture)
      case EDuration.UNWEEKLY =>
        new UnweeklyLecturePlacer(availableTimeSlotCriterias, lectureTimeslotCriterias, availableTimeSlots, allTimeslots, availableRooms,classRooms).doPlacing(lecture)
      case _ => false
    }

    if (!result) {
      prepareNextDuration(allLectures, lecture)
    }
    result
  }
}
