package logic.generator.schedulegenerator

import akka.actor.Actor
import models.persistence.scheduletree.{Timeslot, Weekday, Root}
import models.Transactions
import models.persistence.location.RoomEntity
import org.hibernate.criterion.CriteriaSpecification
import models.fhs.pages.JavaList
import scala.collection.JavaConversions._
import models.persistence.template.{TimeslotTemplate, WeekdayTemplate}
import models.persistence.{Schedule, Docent}
import models.persistence.participants.{Course, Group, Participant}
import models.persistence.lecture.{ParallelLecture, Lecture, AbstractLecture}
import play.api.Logger
import scala.annotation.tailrec
import scala.collection.mutable
import models.persistence.enumerations.EDuration
import models.persistence.criteria.{TimeslotCriteria, RoomCriteria}
import org.hibernate.FetchMode
import scala.concurrent.duration._
import akka.util.Timeout
import scala.util.Random

/**
 * @author fabian 
 *         on 23.03.14.
 */
class ScheduleGeneratorSlave extends Actor {

  private var placed = 0

  private var notPlaced = 0

  private var noRoom = 0

  val TIMEOUT_VAL = 30

  implicit val timeout = Timeout(TIMEOUT_VAL seconds)

  private def filterRooms(rooms: List[RoomEntity], lecture: Lecture) = rooms.par.filter(_.getCapacity >= lecture.calculateNumberOfParticipants()).toList

  override def receive = {

    case SlaveGenerate(lectures) =>
      try {
        placeLectures(lectures.sortBy(l => (-l.getCosts, l.getName)))
      }
      catch {
        case e: Exception => Logger.error("error", e)
      }

    case _ =>
  }

  private def placeLectures(lectures: List[Lecture]) {


    val rooms = Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[RoomEntity]).setCacheable(true).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).setFetchMode("house.rooms", FetchMode.JOIN).list().asInstanceOf[JavaList[RoomEntity]].toList
    }

    val weekdays: List[Weekday] = Transactions.hibernateAction {
      implicit session =>
        session.createCriteria(classOf[WeekdayTemplate]).setCacheable(true).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[WeekdayTemplate]].toList.sortBy(_.getSortIndex)
    }

    val root = new Root

    root.setChildren(weekdays)

    val allTimeslots = root.getChildren.flatMap(_.getChildren).toList.asInstanceOf[List[Timeslot]]

    doPlacing(lectures, allTimeslots, rooms, root)



    if (notPlaced == 0) {
      val schedule = new Schedule
      Logger.debug("placed: " + placed + ", not placed: " + notPlaced + ", no room: " + noRoom)
      schedule.setRoot(root)
      sender() ! ScheduleAnswer(schedule)
    }

  }

  @tailrec
  private def doPlacing(lectures: List[Lecture], allTimeslots: List[Timeslot], rooms: List[RoomEntity], root: Root) {

    def prepareNextDuration(lecture: Lecture) {

      lecture.increaseDifficultLevel();
      lectures.par.foreach {
        l =>
          if (l.getDuration != EDuration.WEEKLY) {
            l.setDuration(EDuration.UNWEEKLY)
          }
      }
      sender() ! PlacingFailure
    }


    if (lectures.isEmpty) {
      return
    }
    val lecture = lectures.head

    val (availableRooms, availableTimeslots) = (filterRooms(lecture, rooms), allTimeslots)

    if (lecture.getDuration == EDuration.UNWEEKLY) {
      val parallelLectures = root.getChildren.flatMap(_.getChildren.flatMap {
        case slot: Timeslot => slot.getLectures.filter {
          theLecture =>

            val theLectureCourses = theLecture.getParticipants.map(_.getCourse)
            val lectureCourses = lecture.getParticipants.map(_.getCourse)

            val participantsClassMatch = theLecture.getParticipants.forall(lecture.getParticipants.head.getClass.isInstance(_))

            val participantsSizeMatch = theLecture match {
              case p: ParallelLecture =>
                p.getLectures.head.getParticipants.map(_.getSize.toInt).sum == lecture.getParticipants.map(_.getSize.toInt).sum
              case _ => false
            }

            val theLectureContainsCourses = theLectureCourses.containsAll(lectureCourses) && (theLectureCourses.size == lectureCourses.size)

            val theLectureContainsDocents = theLecture.getDocents.containsAll(lecture.getDocents) && (theLecture.getDocents.size() == lecture.getDocents.size())

            val theLectureParticipantsEqualsCurrentParticipants = theLecture.getParticipants.containsAll(lecture.getParticipants) && theLecture.getParticipants.size() == lecture.getParticipants.size()

            val theLectureContainsParticipant = lecture.getParticipants.head match {
              case _: Group =>
                timeslotContainsParticipants(slot, lecture.getParticipants.toSet)
              case _ => false
            }

            theLecture.isInstanceOf[ParallelLecture] && theLectureContainsCourses && theLectureContainsDocents && (participantsClassMatch || participantsSizeMatch) && (theLectureParticipantsEqualsCurrentParticipants || !theLectureContainsParticipant)
        }
          .asInstanceOf[mutable.Buffer[ParallelLecture]]
      })

      // Logger.debug("parallel lectures: " + parallelLectures.flatMap(_.getLectures.map(_.getName)))

      def createParallelLecture() {
        val parallelLecture = new ParallelLecture
        parallelLecture.setLectures(List(lecture))
        lecture.setDuration(EDuration.EVEN)
        val possibleTimeslots = findPossibleTimeslots(availableTimeslots, parallelLecture)
        initTimeslotAndRoom(lecture, possibleTimeslots.toList, availableRooms) match {
          case Some(timeslot) =>
            timeslot.setLectures(timeslot.getLectures :+ parallelLecture)
          case None =>
            prepareNextDuration(lecture)
        }
      }
      if (parallelLectures.isEmpty) {
        createParallelLecture()
        if (notPlaced > 0) {
          return
        }
      } else {
        val existingParallelLectures = parallelLectures.filter(_.getLectures.size() == 1)
        if (existingParallelLectures.isEmpty) {
          createParallelLecture()
        } else {
          val existingLecture = existingParallelLectures.head
          lecture.setDuration(EDuration.UNEVEN)
          lecture.setRoom(existingLecture.getLectures.head.getRoom)
          existingLecture.setLectures(existingLecture.getLectures :+ lecture)
          placed += 1
        }
      }

    } else {


      val possibleTimeslots = findPossibleTimeslots(availableTimeslots.toList, lecture)
      initTimeslotAndRoom(lecture, possibleTimeslots.toList, availableRooms.toList) match {
        case Some(timeslot) => timeslot.setLectures(timeslot.getLectures :+ lecture)
        case None =>

          prepareNextDuration(lecture)

          return

      }
    }

    doPlacing(lectures.tail, allTimeslots, rooms, root)

  }

  private def filterRooms(lecture: Lecture, allRooms: List[RoomEntity]): List[RoomEntity] = {

    @tailrec
    def filterRecursive(criterias: Set[RoomCriteria], roomBuffer: Set[RoomEntity] = Set[RoomEntity]()): Set[RoomEntity] = {

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

      filterRecursive(criterias.tail, roomBuffer ++ rooms)
    }

    val roomCriterias = (lecture.getCriteriaContainer.getCriterias ++ lecture.getDocents.flatMap(_.getCriteriaContainer.getCriterias)).filter(_.isInstanceOf[RoomCriteria]).map {
      case rcrit: RoomCriteria => rcrit
    }
    if (roomCriterias.isEmpty) {
      return allRooms
    }

    filterRecursive(roomCriterias.toSet).toList
  }


  private def findPossibleTimeslots(timeslots: List[Timeslot], lecture: AbstractLecture) = {
    timeslots.filter {
      timeslot =>
        !timeslotContainsDocents(timeslot, lecture.getDocents.toSet) && !timeslotContainsParticipants(timeslot, lecture.getParticipants.toSet)
    }
  }

  @tailrec
  private def initTimeslotAndRoom(lecture: Lecture, possibleTimeslots: List[Timeslot], rooms: List[RoomEntity]): Option[Timeslot] = {
     Random.shuffle(possibleTimeslots).headOption match {
    //possibleTimeslots.headOption match {
      case None => Logger.warn("already placed " + placed + " cannot place " + lecture.getName + " " + lecture.getKind + " " + lecture.getParticipants.map(_.getName) + " no timeslots available")
        notPlaced += 1
        None
      case Some(timeslot) =>
        val possibleRooms = rooms.diff(timeslot.getLectures.flatMap(_.getRooms)).sortBy(_.getCapacity)
        if (possibleRooms.isEmpty) {
          // Logger.debug("no room in timeslot " + timeslot + " for lecture " + lecture)
          noRoom += 1
          initTimeslotAndRoom(lecture, possibleTimeslots.tail, rooms)
        } else {

          val filteredRooms = filterRooms(possibleRooms.filter {
            room =>
              val criteria = room.getCriteriaContainer.getCriterias.map {
                case t: TimeslotCriteria => t
              }

              if (criteria.isEmpty) {
                true
              } else {
                criteria.count(c => timeslot.isInTimeslotCriteria(c)) > 0
              }

          }, lecture).sortBy(_.getCapacity)

          if (filteredRooms.isEmpty) {
            // Logger.debug("no room in timeslot " + timeslot + " for lecture " + lecture.getName)
            noRoom += 1
            initTimeslotAndRoom(lecture, possibleTimeslots.tail, rooms)
          } else {

            lecture.setRoom(filteredRooms.head)
            placed += 1
            Some(timeslot)
          }
        }


    }
  }

  private def timeslotContainsParticipants(timeslot: Timeslot, participants: Set[Participant]): Boolean = {
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

  private def timeslotContainsDocents(timeslot: Timeslot, docents: Set[Docent]): Boolean = {
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

  private implicit def weekdayTemplateList2WeekdayList(wl: List[WeekdayTemplate]): List[Weekday] = {
    wl.map(weekdayTemplate2Weekday)
  }

  private implicit def weekdayTemplate2Weekday(weekdayTemplate: WeekdayTemplate): Weekday = {
    val ret = new Weekday()

    ret.setName(weekdayTemplate.getName)
    ret.setSortIndex(weekdayTemplate.getSortIndex)
    ret.setChildren(weekdayTemplate.getChildren.map(timeslotTemplate2Timeslot(_, ret)).sorted)
    ret
  }

  private def timeslotTemplate2Timeslot(timeslotTemplate: TimeslotTemplate, weekday: Weekday): Timeslot = {
    val ret = new Timeslot
    ret.setStartHour(timeslotTemplate.getStartHour)
    ret.setStartMinute(timeslotTemplate.getStartMinute)
    ret.setStopHour(timeslotTemplate.getStopHour)
    ret.setStopMinute(timeslotTemplate.getStopMinute)
    ret.setParent(weekday)
    ret.setLectures(List[AbstractLecture]())
    ret
  }


}

