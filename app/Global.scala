/**
 * Created by fabian on 28.01.14.
 */


import models.persistence.scheduletree.Node
import play.api._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application started")


    Logger.info("successfully initialized")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }


}
