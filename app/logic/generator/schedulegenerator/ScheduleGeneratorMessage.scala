package logic.generator.schedulegenerator

import models.persistence.Schedule
import models.persistence.subject.AbstractSubject
import models.persistence.lecture.Lecture

/**
 * @author fabian 
 *         on 20.03.14.
 */
sealed trait ScheduleGeneratorMessage

case class GenerateSchedule(subjects: List[AbstractSubject]) extends ScheduleGeneratorMessage

case class SlaveGenerate(lectures: List[Lecture]) extends ScheduleGeneratorMessage

case class ScheduleAnswer(schedule: Schedule) extends ScheduleGeneratorMessage