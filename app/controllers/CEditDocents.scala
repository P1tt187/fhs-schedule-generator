package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._

import views.html.editdocents._
import models.fhs.pages.editdocents._
import models.fhs.pages.editdocents.MEditDocents._
import play.api.Logger

/**
 * @author fabian 
 *         on 13.04.14.
 */
object CEditDocents extends Controller {

  val NAV = "DOCENTS"

  val newDocentForm: Form[MDocent] = Form(
    mapping("lastName" -> nonEmptyText(minLength = 3))(MDocent.apply)(MDocent.unapply)
  )

  val existingDocentForm: Form[MExistingDocent] = Form(
    mapping(
      "id" -> longNumber,
      "lastName" -> nonEmptyText(minLength = 3),
      "timeslots" -> list(
        mapping(
          "timeKind" ->nonEmptyText,
          "weekday" -> number,
          "startHour" -> number,
          "startMinute" -> number,
          "stopHour" -> number,
          "stopMinute" -> number
        )(MDocentTimeWhish.apply)(MDocentTimeWhish.unapply)
      ),

      "houseCriterias" -> list(
        longNumber
      ),
      "roomAttr" -> list(
        nonEmptyText
      ),
      "roomCrit" -> list(
        longNumber
      )
    )(MExistingDocent.apply)(MExistingDocent.unapply)
  )

  def page = Action {
    implicit request =>
      Ok(editDocents("Dozenten", newDocentForm, findAllDocents()))
  }

  def sendTimeCritFields(index: Int) = Action {
    Ok(Json.stringify(Json.obj("htmlresult" -> timeCritFields(index, existingDocentForm).toString())))
  }

  def sendDocentFields(id: Long) = Action {
    implicit request =>
      val docent = findDocentById(id)
      Ok(Json.stringify(Json.obj("htmlresult" -> docentfields(existingDocentForm.fill(docent), findHouses(), findAllRooms()).toString()))).withSession("docentName" -> docent.getLastName)
  }

  def editDocent = Action {
    implicit request =>

    // Logger.debug("edit docent header: " + request.headers)
      val docentResult = existingDocentForm.bindFromRequest
      docentResult.fold(
        error => {
          val flashing = flash +("submitResult", "false")
          Logger.debug("error in form - " + error.value)
          Ok(Json.obj("htmlresult" -> docentfields(error, findHouses(), findAllRooms())(flashing).toString()))
        },
        mDocent => {
          Logger.debug("edit data - " + mDocent)

          val docent = persistEditedDocent(mDocent)
          val flashing = flash +("submitResult", "true")

          Ok(Json.obj("htmlresult" -> docentfields(existingDocentForm.fill(docent), findHouses(), findAllRooms())(flashing).toString))
        }
      )
  }

  def deleteDocent(id: Long) = Action {
    val (docentName, connectedSubjects) = removeDocent(id)
    Redirect(routes.CEditDocents.page).flashing("connectedSubjects" -> connectedSubjects.mkString(" "), "docentName" -> docentName).withSession("docentName" -> docentName)
  }

  def saveNewDocent = Action {
    implicit request =>
    //Logger.debug("add docent header: " + request.headers)
      val docentResult = newDocentForm.bindFromRequest

      docentResult.fold(
        errors => {
          BadRequest(editDocents("Dozenten", errors, findAllDocents()))
        },
        mDocent => {
          persistNewDocent(docentResult.get)

          Redirect(routes.CEditDocents.page)
        }
      )


  }

}
