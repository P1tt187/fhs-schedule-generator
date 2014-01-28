/**
 * Created by fabian on 28.01.14.
 */

import java.util
import models.persistence.{Node, Weekday}
import play.api._
import models._

object Global extends GlobalSettings {

  override def onStart(app:Application){
    Logger.info("Application started")
    val weekdays = WEEKDAY_FINDER.all()
    if(weekdays.isEmpty){
      Logger.info("Insert Weekdays into Database")
      List("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday") foreach  {
        dayname=>
          val day = new Weekday
          day.name=dayname
          day.children=new util.LinkedList[Node]
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
