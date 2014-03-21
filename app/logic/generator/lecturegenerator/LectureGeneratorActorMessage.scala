package logic.generator.lecturegenerator

import models.persistence.subject.AbstractSubject
import models.persistence.lecture.Lecture

/**
 * @author fabian 
 *         on 21.03.14.
 */
sealed trait LectureGeneratorActorMessage

case class GenerateLectures(subjects:List[AbstractSubject]) extends LectureGeneratorActorMessage

case class LectureAnswer(lectures:List[Lecture]) extends LectureGeneratorActorMessage
