/**
 * Created by fabian on 28.01.14.
 */



import play.api._
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter()) with GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application started")


    Logger.info("successfully initialized")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }


}
