package logic.generator.schedulegenerator

import models.persistence.{Semester, Schedule}
import models.persistence.lecture.Lecture
import java.util.Calendar
import models.persistence.docents.Docent

/**
 * @author fabian 
 *         on 20.03.14.
 */
sealed trait ScheduleGeneratorMessage

case class GenerateSchedule(lectures: List[Lecture], semester : Semester, endTime:Calendar, randomRatio:Int, maxIterationDeep:Int) extends ScheduleGeneratorMessage

case class SlaveGenerate(lectures: List[Lecture]) extends ScheduleGeneratorMessage

case class ScheduleAnswer(schedule: Schedule) extends ScheduleGeneratorMessage

case class ScheduleSlaveAnswer(schedule: Schedule) extends ScheduleGeneratorMessage

case class InplacebleSchedule(lectures:List[Lecture]) extends ScheduleGeneratorMessage

case class TimeWishNotMatch(docents:List[Docent]) extends ScheduleGeneratorMessage

case object PlacingFailure extends ScheduleGeneratorMessage