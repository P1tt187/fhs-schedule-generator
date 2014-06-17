package models.fhs.pages.index
import play.api.Play.current


/**
 * @author fabian 
 *         on 16.06.14.
 */
object MIndex {

  lazy val SSH_SERVER = current.configuration.getString("ssh.server").getOrElse("localhost")
  lazy val SSH_PORT= current.configuration.getString("ssh.port").getOrElse("22").toInt
  lazy val ADMINS = current.configuration.getString("administrator").getOrElse("").split(",").toList

}

case class MUser(username:String,password:String)