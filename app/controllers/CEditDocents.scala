package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._

import views.html.editdocents._
import models.fhs.pages.editdocents._
import models.fhs.pages.editdocents.MEditDocents._

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
          "tolerant" -> boolean,
          "weekday" -> number,
          "startHour" -> number,
          "startMinute" -> number,
          "stopHour" -> number,
          "stopMinute" -> number
        )(MTimeslotCriteria.apply)(MTimeslotCriteria.unapply)
      ),

      "houseCriterias" -> list(
        mapping(
          "name" -> nonEmptyText
        )(MHouseCriteria.apply)(MHouseCriteria.unapply)
      ),
      "roomAttr" -> list(
        mapping("name" -> nonEmptyText)(MRoomAttribute.apply)(MRoomAttribute.unapply)
      ),
      "roomCrit" -> list(
        mapping(
          "houseName" -> nonEmptyText,
          "number" -> nonEmptyText
        )(MRoomCriteria.apply)(MRoomCriteria.unapply)
      )


    )(MExistingDocent.apply)(MExistingDocent.unapply)
  )

  def page = Action {
    implicit request =>
      Ok(editDocents("Dozenten", newDocentForm, findAllDocents()))
  }

  def sendDocentFields(id: Long) = Action {
    Ok(Json.stringify(Json.obj("htmlresult" -> docentfields(existingDocentForm.fill(findDocentById(id)), findHouses()).toString())))
  }

  def saveNewDocent = Action {
    implicit request =>
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
