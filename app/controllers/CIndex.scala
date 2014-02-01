package controllers

import play.api.mvc._

/**
 * Created by fabian on 22.01.14.
 */
object CIndex extends Controller {

  val NAV = "index"

  def index =

      Action {
        Ok(views.html.index("Home"))
      }

}
