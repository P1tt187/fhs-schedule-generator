package models.fhs.pages.roomdefinition

import play.api.libs.json.Json

/**
 * Created by fabian on 04.02.14.
 */
case class MTtimeslotCritDefine(startHour: Int, startMinutes: Int, stopHour: Int, stopMinutes: Int, weekdays: List[Int], duration: String)

object MTtimeslotCritDefine {
  implicit val timeslotCritDefineFormat = Json.format[MTtimeslotCritDefine]
}