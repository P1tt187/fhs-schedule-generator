import com.avaje.ebean.Ebean
import models.persistence.scheduletree.{Timeslot, Weekday}
import play.db.ebean.Model.Finder

/**
 * Created by fabian on 27.01.14.
 */
package object models {

  val TIMESLOT_FINDER = Ebean.find(classOf[Timeslot]).fetch("parent")
  val WEEKDAY_FINDER = new Finder(classOf[Long],classOf[Weekday])

}
