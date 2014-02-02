package models.fhs.pages.timeslot

/**
 * Created by fabian on 23.01.14.
 */
case class MTimeslotDefine(startHour: Int, startMinutes: Int, stopHour: Int, stopMinutes: Int, weekdays: List[String])

case class MTimeslotDisplay(startHour: Int, startMinutes: Int, stopHour: Int, stopMinutes: Int, weekday: String, weekdayIndex: Int) extends Ordered[MTimeslotDisplay] {
  def compare(that: MTimeslotDisplay): Int = {
    val retSeq = Seq(this.weekdayIndex.compareTo(that.weekdayIndex), this.startHour.compareTo(that.startHour), this.stopHour.compareTo(that.stopHour), this.stopMinutes.compareTo(that.stopMinutes)).filter(_ != 0)
    if (retSeq.isEmpty) {
      0
    } else {
      retSeq(0)
    }
  }
}
