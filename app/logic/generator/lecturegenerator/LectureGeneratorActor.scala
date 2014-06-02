package logic.generator.lecturegenerator

import akka.actor.Actor
import models.persistence.lecture.Lecture
import scala.collection.mutable
import models.persistence.subject.{AbstractSubject, ExerciseSubject, LectureSubject}
import models.persistence.participants.{LectureParticipant, Group, Participant}
import scala.collection.JavaConversions._
import models.persistence.enumerations.{ELectureKind, EDuration}
import play.api.Logger
import models.Transactions
import org.hibernate.criterion.{CriteriaSpecification, Restrictions}
import models.fhs.pages.JavaList
import exceptions.NoGroupFoundException
import scala.util.Random
import org.hibernate.FetchMode
import java.math.BigInteger
import com.rits.cloning.{ObjenesisInstantiationStrategy, Cloner}

/**
 * @author fabian 
 *         on 21.03.14.
 */
class LectureGeneratorActor extends Actor {

  private var addedLectures = 0
  private var addedExercises = 0
  private val cloner = new Cloner(new ObjenesisInstantiationStrategy)

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
                lecture.setDocents(lectureSubject.getDocents.map(_.docent2LectureDocent()))
                lecture.setParticipants(Set[Participant]() ++ lectureSubject.getCourses)
                lecture.setLectureParticipants(lectureSubject.getCourses.map(_.participant2LectureParticipant()))

                lecture.setDuration(EDuration.WEEKLY)
                lecture.setName(lectureSubject.getName)
                lecture.setCriteriaContainer(subject.getCriteriaContainer)
                lecture.setLectureSynonyms(cloner.deepClone(lectureSubject.getSubjectSynonyms))
                lecture.setKind(ELectureKind.LECTURE)
                lecture.setExpectedParticipants(lectureSubject.getExpectedParticipants)
                lecture.setDifficultLevel(BigInteger.valueOf(lectureSubject.getUnits.toLong))
                addedLectures += 1
                lecture
              }
              if (lectureSubject.getDuration != EDuration.UNWEEKLY) {
                for (i <- 1 to lectureSubject.getUnits.toInt) {
                  val lecture: Lecture = initLectureLecture
                  result += lecture

                }
                if (isUnWeekly(lectureSubject)) {
                  val lecture = initLectureLecture
                  lecture.setDuration(EDuration.UNWEEKLY)
                  result += lecture
                }
              } else {
                for (i <- 0f to lectureSubject.getUnits.toFloat by 0.5f) {
                  val lecture: Lecture = initLectureLecture
                  lecture.setDuration(EDuration.UNWEEKLY)
                  result += lecture
                }
              }
            case exerciseSubject: ExerciseSubject =>

              val multipleCourseGroups = exerciseSubject.getCourses.map {
                course =>
                  Transactions.hibernateAction {
                    implicit session =>
                      session.createCriteria(classOf[Group]).add(Restrictions.eq("course", course))
                        .add(Restrictions.eq("groupType", exerciseSubject.getGroupType))
                        .setFetchMode("course.groups", FetchMode.JOIN)
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
                lecture.setLectureParticipants(Set[LectureParticipant]() ++ groups.map(_.participant2LectureParticipant()))
                lecture.setDocents(exerciseSubject.getDocents.map(_.docent2LectureDocent()))
                lecture.setCriteriaContainer(exerciseSubject.getCriteriaContainer)
                lecture.setDuration(EDuration.WEEKLY)
                lecture.setName(exerciseSubject.getName)
                lecture.setLectureSynonyms(cloner.deepClone(exerciseSubject.getSubjectSynonyms))
                lecture.setKind(ELectureKind.EXERCISE)
                lecture.setExpectedParticipants(exerciseSubject.getExpectedParticipants)
                lecture.setDifficultLevel(BigInteger.valueOf(exerciseSubject.getUnits.toLong))
                addedExercises += 1
                lecture
              }

              for (groupIndex <- 0 to multipleCourseGroups.map(_.size).max - 1) {
                val groups = multipleCourseGroups.map(_.lift(groupIndex)).map {
                  case Some(group) => group
                  case None => null
                }.filter(_ != null)

                if (exerciseSubject.getDuration != EDuration.UNWEEKLY) {

                  for (i <- 1 to exerciseSubject.getUnits.toInt) {
                    val lecture: Lecture = initExerciseLecture(groups)
                    result += lecture
                  }
                  if (isUnWeekly(exerciseSubject)) {
                    val lecture: Lecture = initExerciseLecture(groups)
                    lecture.setDuration(EDuration.UNWEEKLY)
                    result += lecture
                  }
                } else {
                  for(i<- 0f to exerciseSubject.getUnits.toFloat by 0.5f){
                    val lecture: Lecture = initExerciseLecture(groups)
                    lecture.setDuration(EDuration.UNWEEKLY)
                    result += lecture
                  }
                }
              }

            case anyOther =>
              Logger.warn("No rule for class " + anyOther.getClass.getName)
              List()
          }
          result
      }.toList

      Logger.debug("lecture: " + addedLectures + " exercises: " + addedExercises)

      sender ! LectureAnswer(Random.shuffle(lectures))
    case _ =>
  }

}
