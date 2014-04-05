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
import scala.util.Random
import scala.annotation.tailrec
import scala.collection.mutable
import models.persistence.enumerations.EDuration


/**
 * @author fabian 
 *         on 23.03.14.
 */
class ScheduleGeneratorSlave extends Actor {

  private var placed = 0

  private var notPlaced = 0

  private var noRoom = 0

  private def filterRooms(rooms: List[RoomEntity], lecture: Lecture) = rooms.par.filter(_.getCapacity >= lecture.calculateNumberOfParticipants()).toList

  override def receive = {

    case SlaveGenerate(lectures) =>

      Logger.debug("number of lectures: " + lectures.size)

      val rooms = Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[RoomEntity]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[RoomEntity]].toList
      }

      val weekdays: List[Weekday] = Transactions.hibernateAction {
        implicit session =>
          session.createCriteria(classOf[WeekdayTemplate]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[WeekdayTemplate]].toList.sortBy(_.getSortIndex)
      }

      val root = new Root

      root.setChildren(weekdays)

      lectures.foreach {
        lecture =>

          //Logger.debug("lecture " + lecture.getName + " " + lecture.calculateNumberOfParticipants() + " " + lecture.getParticipants.map(_.getSize.toInt).sum)

          if (lecture.getDuration == EDuration.UNWEEKLY) {
            val parallelLectures = root.getChildren.flatMap(_.getChildren.flatMap {
              case slot: Timeslot => slot.getLectures.filter {
                theLecture =>

                  val theLectureCourses = theLecture.getParticipants.map(_.getCourse)
                  val lectureCourses = lecture.getParticipants.map(_.getCourse)

                  val participantsClassMatch = theLecture.getParticipants.forall(lecture.getParticipants.head.getClass.isInstance(_))

                  val theLectureContainsCourses = theLectureCourses.containsAll(lectureCourses) && (theLectureCourses.size == lectureCourses.size)

                  val theLectureContainsDocents = theLecture.getDocents.containsAll(lecture.getDocents) && (theLecture.getDocents.size() == lecture.getDocents.size())

                  val theLectureContainsParticipant = lecture.getParticipants.head match {
                    case _: Group => timeslotContainsParticipants(slot, lecture.getParticipants.toSet)
                    case _ => false
                  }

                  theLecture.isInstanceOf[ParallelLecture] && theLectureContainsCourses && theLectureContainsDocents && participantsClassMatch && !theLectureContainsParticipant
              }
                .asInstanceOf[mutable.Buffer[ParallelLecture]]
            })

            // Logger.debug("parallel lectures: " + parallelLectures.flatMap(_.getLectures.map(_.getName)))

            def createParallelLecture() {
              val parallelLecture = new ParallelLecture
              parallelLecture.setLectures(List(lecture))
              lecture.setDuration(EDuration.EVEN)
              val possibleTimeslots = findPossibleTimeslots(root, parallelLecture)
              initTimeslotAndRoom(lecture, Random.shuffle(possibleTimeslots.toList), filterRooms(rooms, lecture)) match {
                case Some(timeslot) =>
                  timeslot.setLectures(timeslot.getLectures :+ parallelLecture)
                case None =>
              }
            }
            if (parallelLectures.isEmpty) {
              createParallelLecture()
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

            //TODO filter weekdays
            val possibleTimeslots = findPossibleTimeslots(root, lecture)
            initTimeslotAndRoom(lecture, Random.shuffle(possibleTimeslots.toList), filterRooms(rooms, lecture)) match {
              case Some(timeslot) => timeslot.setLectures(timeslot.getLectures :+ lecture)
              case None =>
            }
          }
      }

      val schedule = new Schedule

      schedule.setRoot(root)
      Logger.debug("placed: " + placed + ", not placed: " + notPlaced + ", no room: " + noRoom)

      sender() ! ScheduleAnswer(schedule)


    case _ =>
  }


  private def findPossibleTimeslots(root: Root, lecture: AbstractLecture) = {
    root.getChildren.flatMap {
      weekday =>
        weekday.getChildren.toList.asInstanceOf[List[Timeslot]].filter {
          //TODO filter with timecriterias
          timeslot =>
            !timeslotContainsDocents(timeslot, lecture.getDocents.toSet) && !timeslotContainsParticipants(timeslot, lecture.getParticipants.toSet)
        }
    }
  }

  @tailrec
  private def initTimeslotAndRoom(lecture: Lecture, possibleTimeslots: List[Timeslot], rooms: List[RoomEntity]): Option[Timeslot] = {
    //TODO filter with room criterias
    possibleTimeslots.headOption match {
      case None => Logger.warn("cannot place " + lecture + " no timeslots available")
        notPlaced += 1
        None
      case Some(timeslot) =>
        val possibleRooms = rooms.diff(timeslot.getLectures.flatMap(_.getRooms)).sortBy(_.getCapacity)
        if (possibleRooms.isEmpty) {
          Logger.debug("no room in timeslot " + timeslot + " for lecture " + lecture)
          noRoom += 1
          initTimeslotAndRoom(lecture, possibleTimeslots.tail, rooms)
        } else {
          lecture.setRoom(possibleRooms.head)
          placed += 1
          Some(timeslot)
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

    if (participants.contains(group)) {
      return true
    }
    containsInParentGroup(group.getParent, participants)
  }

  private def containsInSubGroups(group: Group, participants: mutable.Buffer[Participant]): Boolean = {
    if (participants.contains(group)) {
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

