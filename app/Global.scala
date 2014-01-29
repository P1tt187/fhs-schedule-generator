/**
 * Created by fabian on 28.01.14.
 */

import java.util
import models.persistence.scheduletree.{Node, Weekday}
import play.api._
import models._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application started")
    val weekdays = WEEKDAY_FINDER.all()
    if (weekdays.isEmpty) {
      Logger.info("Insert Weekdays into Database")



      List(("Monday", 1), ("Tuesday", 2), ("Wednesday", 3), ("Thursday", 4), ("Friday", 5), ("Saturday", 6), ("Sunday", 0)) foreach {
        daytuple =>
          val day = new Weekday
          day.name = daytuple._1
          day.sortIndex = daytuple._2
          day.children = new util.LinkedList[Node]
          day.save()
      }
      Logger.info("insert complete")
    }
    Logger.info("successfully initialized")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }


}
