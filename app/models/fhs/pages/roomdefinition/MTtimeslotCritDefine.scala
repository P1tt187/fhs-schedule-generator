package models.fhs.pages.roomdefinition

/**
 * Created by fabian on 04.02.14.
 */
case class MTtimeslotCritDefine(priority: Int, tolerance: Boolean, startHour: Int, startMinutes: Int, stopHour: Int, stopMinutes: Int, weekdays: List[String])
