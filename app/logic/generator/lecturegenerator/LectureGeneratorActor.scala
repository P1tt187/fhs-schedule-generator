package logic.generator.lecturegenerator

import akka.actor.Actor
import models.persistence.lecture.Lecture
import scala.collection.mutable
import models.persistence.subject.{AbstractSubject, ExerciseSubject, LectureSubject}
import models.persistence.participants.{Group, Participant}
import scala.collection.JavaConversions._
import models.persistence.enumerations.EDuration
import play.api.Logger
import models.Transactions
import org.hibernate.criterion.{CriteriaSpecification, Restrictions}
import models.fhs.pages.JavaList
import exceptions.NoGroupFoundException
import scala.util.Random

/**
 * @author fabian 
 *         on 21.03.14.
 */
class LectureGeneratorActor extends Actor {

  private var addedLectures = 0
  private var addedExercises = 0

  override def receive = {

    case GenerateLectures(subjects) =>

      val lectures = subjects.par.flatMap {
        subject =>

          val result = mutable.Buffer[Lecture]()

          def isUnWeekly(subject: AbstractSubject): Boolean = {
            subject.getUnits != subject.getUnits.toInt.toFloat
          }
          subject match {
            case lectureSubject: LectureSubject =>

              def initLectureLecture: Lecture = {
                val lecture = new Lecture
                lecture.setDocents(lectureSubject.getDocents)
                lecture.setParticipants(Set[Participant]() ++ lectureSubject.getCourses)
                lecture.setDuration(EDuration.WEEKLY)
                lecture.setName(lectureSubject.getName)
                lecture.setCriteriaContainer(subject.getCriteriaContainer)
                lecture.setLectureSynonyms(lectureSubject.getSubjectSynonyms)
                lecture
              }
              for (i <- 1 to lectureSubject.getUnits.toInt) {
                val lecture: Lecture = initLectureLecture
                result += lecture
                addedLectures += 1
              }
              if (isUnWeekly(lectureSubject)) {
                val lecture = initLectureLecture
                lecture.setDuration(EDuration.UNWEEKLY)
                result += lecture
                addedLectures += 1
              }

            case exerciseSubject: ExerciseSubject =>

              val multipleCourseGroups = exerciseSubject.getCourses.map {
                course =>
                  Transactions.hibernateAction {
                    implicit session =>
                      session.createCriteria(classOf[Group]).add(Restrictions.eq("course", course))
                        .add(Restrictions.eq("groupType", exerciseSubject.getGroupType))
                        .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[Group]].toList
                  }
              }.toList


              if (!multipleCourseGroups.map(_.isEmpty).forall(result => !result)) {
                val e = new NoGroupFoundException("No group found for type: " + exerciseSubject.getGroupType + " " + exerciseSubject.getCourses.map(_.getShortName))
                Logger.error("error", e)
                throw e
              }

              def initExerciseLecture(groups: List[Group]): Lecture = {
                val lecture = new Lecture
                lecture.setParticipants(Set[Participant]() ++ groups)
                lecture.setDocents(exerciseSubject.getDocents)
                lecture.setCriteriaContainer(exerciseSubject.getCriteriaContainer)
                lecture.setDuration(EDuration.WEEKLY)
                lecture.setName(exerciseSubject.getName)
                lecture.setLectureSynonyms(exerciseSubject.getSubjectSynonyms)
                lecture
              }
              if (multipleCourseGroups.size > 1) {

                def checkIfGroupSizeMatch: Boolean = {
                  multipleCourseGroups.map(_.size).toSet.size == 1
                }


                if (!checkIfGroupSizeMatch) {

                  multipleCourseGroups.foreach {
                    groups =>
                      groups.foreach {
                        group =>
                          initSingleGroup(group)
                      }
                  }

                } else {
                  for (groupIndex <- 0 to multipleCourseGroups(0).size - 1) {
                    val groups = multipleCourseGroups.map(_(groupIndex))
                    for (i <- 1 to exerciseSubject.getUnits.toInt) {
                      val lecture: Lecture = initExerciseLecture(groups)
                      result += lecture
                      addedExercises += 1
                    }
                    if (isUnWeekly(exerciseSubject)) {
                      val lecture: Lecture = initExerciseLecture(groups)
                      lecture.setDuration(EDuration.UNWEEKLY)
                      result += lecture
                      addedExercises += 1
                    }
                  }
                }
              }
              def initSingleGroup(group: Group): Any = {
                for (i <- 1 to exerciseSubject.getUnits.toInt) {
                  val lecture = initExerciseLecture(List(group))
                  result += lecture
                }

                if (isUnWeekly(exerciseSubject)) {
                  val lecture = initExerciseLecture(List(group))
                  lecture.setDuration(EDuration.UNWEEKLY)
                  result += lecture
                }
              }
              if (multipleCourseGroups.size == 1) {

                multipleCourseGroups(0).foreach {
                  group =>
                    initSingleGroup(group)

                }

              }

            case anyOther =>
              Logger.warn("No rule for class " + anyOther.getClass.getName)
              List()
          }
          result
      }.toList

      Logger.debug("lecture: " + addedExercises + " exercises: " + addedExercises)

      sender ! LectureAnswer(Random.shuffle(lectures))
    case _ =>
  }

}
