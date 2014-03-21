package logic.generator.lecturegenerator

import akka.actor.Actor
import models.persistence.lecture.Lecture
import scala.collection.mutable
import models.persistence.subject.{ExerciseSubject, LectureSubject}
import models.persistence.participants.{Group, Participant}
import scala.collection.JavaConversions._
import models.persistence.enumerations.EDuration
import play.api.Logger
import models.Transactions
import org.hibernate.criterion.{CriteriaSpecification, Restrictions}
import models.fhs.pages.JavaList

/**
 * @author fabian 
 *         on 21.03.14.
 */
class LectureGeneratorActor extends Actor {

  override def receive = {

    case GenerateLectures(subjects) =>

      val lectures = subjects.flatMap {
        subject =>
          Logger.debug(subject.toString)
          val result = mutable.Buffer[Lecture]()

          subject match {
            case lectureSubject: LectureSubject =>

              def initLectureLecture: Lecture = {
                val lecture = new Lecture
                lecture.setDocents(lectureSubject.getDocents)
                lecture.setParticipants(Set[Participant]() ++ lectureSubject.getCourses)
                lecture.setDuration(EDuration.WEEKLY)
                lecture.setName(lectureSubject.getName)
                lecture.setCriteriaContainer(subject.getCriteriaContainer)
                lecture
              }
              for (i <- 1 to lectureSubject.getUnits.toInt) {
                val lecture: Lecture = initLectureLecture
                result += lecture
              }
              if(lectureSubject.getUnits!=lectureSubject.getUnits.toInt.toFloat){
                val lecture = initLectureLecture
                lecture.setDuration(EDuration.UNWEEKLY)
                result += lecture
              }

            case exerciseSubject:ExerciseSubject =>

              val groups = exerciseSubject.getCourses.par.flatMap{
                course =>
                  Transactions.hibernateAction{
                    implicit session =>
                      session.createCriteria(classOf[Group]).add(Restrictions.eq("course",course))
                        .add(Restrictions.eq("groupType",exerciseSubject.getGroupType))
                        .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[Group]].toSet
                  }
              }


              val lecture = new Lecture
              lecture.setParticipants(Set[Participant]() ++ groups)
              lecture.setDocents(exerciseSubject.getDocents)
            case anyOther =>
              Logger.warn("No rule for class " + anyOther.getClass.getName)
              List()
          }
        result
      }



      sender ! LectureAnswer(lectures)
    case _ =>
  }

}
