package controllers


import com.decodified.scalassh._
import models.fhs.pages.index._
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._


/**
 * @author fabian
 *         on 22.01.14.
 */
object CIndex extends Controller {

  val NAV = "index"

  val WRONG_LOGIN = "wrongLogin"

  val CURRENT_USER = "currentUser"

  val IS_ADMIN = "isAdmin"

  val IS_DOCENT="isDocent"

  val IS_DEVELOPER="isDeveloper"

  val loginForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(MUser.apply)(MUser.unapply)
  )

  def index =
    Action {
      implicit request =>
        Ok(views.html.index.index("Home"))
    }

  def doLogin() = Action {
    implicit request =>

      val result = loginForm.bindFromRequest()
      result.fold(
        error => {
          BadRequest(views.html.index.index("Home")).withSession(session + WRONG_LOGIN -> "true")
        },
        mUser => {
          val user = mUser.username
          val password = mUser.password

          try {
            val results = SSH(host = MIndex.SSH_SERVER, configProvider = HostConfig(
              login = PasswordLogin(user, password),
              hostName = MIndex.SSH_SERVER,
              port = MIndex.SSH_PORT,
              hostKeyVerifier = HostKeyVerifiers.DontVerify
            )) {
              client =>
                client.exec("groups").right.map { result =>
                  result.stdOutAsString()
                }
            }


            results.right.toOption match {
              case None =>
                Redirect(routes.CIndex.index()).withSession(session + (WRONG_LOGIN -> "true"))
              case Some(resultString) =>
                Logger.debug("loginresult: " + resultString)

                val isDocent = resultString.trim.split(" ").find(_.equals(MIndex.DOCENT_GROUP)).nonEmpty
                val isAdmin = MIndex.ADMINS.contains(user)
                val isDeveloper=MIndex.DEVELOPERS.contains(user)

                Redirect(routes.CIndex.index()).withSession(session +
                  (WRONG_LOGIN -> "false") + (CURRENT_USER -> mUser.username) + (IS_ADMIN -> isAdmin.toString) + (IS_DOCENT -> isDocent.toString) + (IS_DEVELOPER -> isDeveloper.toString))
            }


          }
          catch {
            case e: Exception =>
              Logger.debug("error on login", e)
              BadRequest(views.html.index.index("Home")).withSession(session + (WRONG_LOGIN -> "true"))
          }


        }
      )

  }

  def doLogout() = Action {
    implicit request =>
      Redirect(routes.CIndex.index()).withNewSession
  }

}
