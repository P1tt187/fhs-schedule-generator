package controllers

import controllers.traits.TController
import models.fhs.pages.editdocents.MEditDocents._
import models.fhs.pages.editdocents._
import models.fhs.pages.generator.MGenerator
import models.fhs.pages.timeslot.MTimeslotDisplay
import models.persistence.docents.Docent
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json._
import play.api.mvc._
import views.html.editdocents._

/**
 * @author fabian 
 *         on 13.04.14.
 */
object CEditDocents extends TController {

  val NAV = "DOCENTS"

  val newDocentForm: Form[MDocent] = Form(
    mapping(
      "lastName" -> nonEmptyText(minLength = 3),
      "userId" -> text
    )(MDocent.apply)(MDocent.unapply)
  )

  val expireDateForm: Form[MExpireDate] = Form(
    mapping("date" -> date("yyyy-MM-dd"))(MExpireDate.apply)(MExpireDate.unapply)
  )

  val existingDocentForm: Form[MExistingDocent] = Form(
    mapping(
      "id" -> longNumber,
      "lastName" -> nonEmptyText(minLength = 3),
      "userId" -> text,
      "comments" -> text,
      "timeslots" -> list(
        mapping(
          "timeKind" -> nonEmptyText,
          "duration" -> nonEmptyText,
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

  private def getDocentList(implicit request: Request[AnyContent]) = {
    val allDocents = findAllDocents()
    val session = request.session

    /** docents will only see their own stuff */
    var docentList: List[Docent] = null
    if (!session.get(IS_ADMIN).getOrElse("false").toBoolean) {
      docentList = allDocents.filter(d => session.get(CURRENT_USER).getOrElse("").equals(d.getUserId))
      if (docentList.isEmpty) {
        docentList = allDocents.filter(d => session.get(CURRENT_USER).getOrElse("").equalsIgnoreCase(d.getLastName))
      }
    } else {
      docentList = allDocents
    }
    docentList
  }

  def page = Action {
    implicit request =>

      val docentList = getDocentList
      val expireDate = findExpireDate()
      val currentExpireDateForm = if (expireDate != null) {
        expireDateForm.fill(expireDate)
      } else {
        expireDateForm
      }

      Ok(editDocents("Dozenten", newDocentForm, currentExpireDateForm, docentList))
  }



  def sendDocentFields(id: Long) = Action {
    implicit request =>
      val docent = findDocentById(id)
      val session = request.session

      val timeWishExpireDate = findExpireDate()
      val expireDate = if (timeWishExpireDate != null) {
        timeWishExpireDate.getExpiredate
      } else {
        null
      }

      val (allTimeSlots, timeRanges) = timeRange
      Ok(Json.stringify(Json.obj("htmlresult" -> docentfields(existingDocentForm.fill(docent), findHouses(), findAllRooms(), timeRanges, allTimeSlots, expireDate).toString().trim)))
        .withSession(session + ("docentName" -> docent.getLastName))
  }

  def editDocent = Action(parse.json) {
    implicit request =>
      val session = request.session
      val jsVal = request.body

      val lastName = (jsVal \ existingDocentForm("lastName").name).as[String]
      val id = (jsVal \ existingDocentForm("id").name).as[String].toLong
      val houseCriterias = (jsVal \ existingDocentForm("houseCriterias").name).as[JsArray].value.map(_.as[String].toLong).toList
      val roomAttr = (jsVal \ existingDocentForm("roomAttr").name).as[JsArray].value.map(_.as[String]).toList
      val roomCrit = (jsVal \ existingDocentForm("roomCrit").name).as[JsArray].value.map(_.as[String].toLong).toList
      val comments = (jsVal \ existingDocentForm("comments").name).as[String].trim
      val userId = (jsVal \ existingDocentForm("userId").name).as[String].trim
      val timeslots = (jsVal \ "timeslots").as[JsArray].value.par.map {
        slot =>
          val rangeString = (slot \ "timerange").as[String].split(",")

          val startTime = rangeString(0).split("-")
          val startHour = startTime(0).toInt
          val startMinute = startTime(1).toInt

          val stopTime = rangeString(1).split("-")

          val stopHour = stopTime(0).toInt
          val stopMinute = stopTime(1).toInt

          val weekday = rangeString(2).toInt

          val timeKind = rangeString(3).trim

          val duration = (slot \ "duration").as[String].trim

          MDocentTimeWhish(timeKind, duration, weekday, startHour, startMinute, stopHour, stopMinute)
      }.toList.filter {
        case w =>
          // Logger.debug("" + !w.timeKind.equalsIgnoreCase("n"))
          !w.timeKind.equalsIgnoreCase("n")
      }


      val mDocent = MExistingDocent(id, lastName, userId, comments, timeslots, houseCriterias, roomAttr, roomCrit)

      Logger.debug(mDocent.toString)

      val docent = persistEditedDocent(mDocent)
      val flashing = request.flash +("submitResult", "true")
      val (allTimeSlots, timeRanges) = timeRange
      val timeWishExpireDate = findExpireDate()
      val expireDate = if (timeWishExpireDate != null) {
        timeWishExpireDate.getExpiredate
      } else {
        null
      }

      Ok(Json.obj("htmlresult" -> docentfields(existingDocentForm.fill(docent), findHouses(), findAllRooms(), timeRanges, allTimeSlots, expireDate)(flashing, session).toString))


  }

  def saveExpireDate = Action {
    implicit request =>



      val result = expireDateForm.bindFromRequest()
      Logger.debug("expireform:" + result)

      result.fold(
        error => {
          BadRequest(editDocents("Dozenten", newDocentForm, error, getDocentList))
        },
        mExpireDate => {
          persistExpireDate(mExpireDate)
          Redirect(routes.CEditDocents.page)
        }
      )
  }

  def deleteDocent(id: Long) = Action {
    implicit request =>
      val session = request.session
      val (docentName, connectedSubjects) = removeDocent(id)
      Redirect(routes.CEditDocents.page).flashing("connectedSubjects" -> connectedSubjects.mkString(" "), "docentName" -> docentName).withSession(session + ("docentName" -> docentName))
  }

  def saveNewDocent = Action {
    implicit request =>
      //Logger.debug("add docent header: " + request.headers)
      val docentResult = newDocentForm.bindFromRequest

      docentResult.fold(
        errors => {
          BadRequest(editDocents("Dozenten", errors, expireDateForm, findAllDocents()))
        },
        mDocent => {
          persistNewDocent(docentResult.get)

          Redirect(routes.CEditDocents.page)
        }
      )


  }

}
