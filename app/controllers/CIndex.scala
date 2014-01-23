package controllers

import play.api.mvc._
import play.api._

/**
 * Created by fabian on 22.01.14.
 */
object CIndex extends Controller{

  def index = Action{
    Ok(views.html.index("Your new application is ready."))
  }

}
