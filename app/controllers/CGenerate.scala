package controllers

import play.api.mvc._
import views.html.generator._
import logic.ImportFile
import play.api.Play.current
import scala.util.Failure
import play.api.libs.concurrent.Execution.Implicits._

/**
 * @author fabian 
 *         on 20.03.14.
 */
object CGenerate extends Controller {

  val NAV = "GENERATE"

def page() = Action{
  Ok(generator("Generator"))
}

}
