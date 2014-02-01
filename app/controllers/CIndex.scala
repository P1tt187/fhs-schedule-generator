package controllers

import play.api.mvc._
import play.api.Play._
import play.api.cache.Cached

/**
 * Created by fabian on 22.01.14.
 */
object CIndex extends Controller {

  val NAV = "index"

  def index =
    Cached("CINDEX", DEFAULT_CACHE_DURATION) {
      Action {
        Ok(views.html.index("Home"))
      }
    }
}
