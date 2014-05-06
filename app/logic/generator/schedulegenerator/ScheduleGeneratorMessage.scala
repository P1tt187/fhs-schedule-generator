package logic.generator.schedulegenerator

import models.persistence.{Semester, Schedule}
import models.persistence.subject.AbstractSubject
import models.persistence.lecture.Lecture
import java.util.Calendar

/**
 * @author fabian 
 *         on 20.03.14.
 */
sealed trait ScheduleGeneratorMessage

case class GenerateSchedule(subjects: List[AbstractSubject], semester : Semester, endTime:Calendar) extends ScheduleGeneratorMessage

case class SlaveGenerate(lectures: List[Lecture]) extends ScheduleGeneratorMessage

case class ScheduleAnswer(schedule: Schedule, rate:Int) extends ScheduleGeneratorMessage

case class ScheduleSlaveAnswer(schedule: Schedule)

case class InplacebleSchedule(lectures:List[Lecture])

case object PlacingFailure extends ScheduleGeneratorMessage