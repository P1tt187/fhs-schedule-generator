package controllers

import models.fhs.pages.editdocents.MEditDocents._
import models.fhs.pages.editdocents._
import models.fhs.pages.generator.MGenerator
import models.fhs.pages.timeslot.MTimeslotDisplay
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
object CEditDocents extends Controller {

  val NAV = "DOCENTS"

  val newDocentForm: Form[MDocent] = Form(
    mapping("lastName" -> nonEmptyText(minLength = 3))(MDocent.apply)(MDocent.unapply)
  )

  val existingDocentForm: Form[MExistingDocent] = Form(
    mapping(
      "id" -> longNumber,
      "lastName" -> nonEmptyText(minLength = 3),
      "comments" ->text,
      "timeslots" -> list(
        mapping(
          "timeKind" ->nonEmptyText,
         "duration"->nonEmptyText,
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



  private def timeRange = {
    val allTimeSlots = MTimeslotDisplay.findAllTimeslots
    val timeRanges = MGenerator.findTimeRanges(allTimeSlots)
    (allTimeSlots,timeRanges)
  }

  def sendDocentFields(id: Long) = Action {
    implicit request =>
      val docent = findDocentById(id)

      val (allTimeSlots,timeRanges) = timeRange
      Ok(Json.stringify(Json.obj("htmlresult" -> docentfields(existingDocentForm.fill(docent), findHouses(), findAllRooms(),timeRanges, allTimeSlots).toString().trim)))
        .withSession(session + ("docentName" -> docent.getLastName))
  }

  def editDocent = Action(parse.json) {
    implicit request =>
      val jsVal = request.body

      val lastName = (jsVal \ existingDocentForm("lastName").name).as[String]
      val id = (jsVal \ existingDocentForm("id").name).as[String].toLong
      val houseCriterias = (jsVal \ existingDocentForm("houseCriterias").name).as[JsArray].value.map(_.as[String].toLong).toList
      val roomAttr = (jsVal \ existingDocentForm("roomAttr").name).as[JsArray].value.map(_.as[String]).toList
      val roomCrit = (jsVal \ existingDocentForm("roomCrit").name).as[JsArray].value.map(_.as[String].toLong).toList
      val comments = (jsVal \ existingDocentForm("comments").name).as[String].trim
      val timeslots = (jsVal \ "timeslots").as[JsArray].value.par.map{
        slot=>
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

          MDocentTimeWhish(timeKind,duration,weekday,startHour,startMinute,stopHour,stopMinute)
      }.toList.filter{
        case w =>
         // Logger.debug("" + !w.timeKind.equalsIgnoreCase("n"))
          !w.timeKind.equalsIgnoreCase("n")
      }


      val mDocent = MExistingDocent(id,lastName, comments,timeslots,houseCriterias,roomAttr,roomCrit)

      Logger.debug(mDocent.toString)

      val docent = persistEditedDocent(mDocent)
      val flashing = flash +("submitResult", "true")
      val (allTimeSlots,timeRanges) = timeRange

      Ok(Json.obj("htmlresult" -> docentfields(existingDocentForm.fill(docent), findHouses(), findAllRooms(),  timeRanges, allTimeSlots)(flashing).toString))


  }

  def deleteDocent(id: Long) = Action {
    implicit request=>
    val (docentName, connectedSubjects) = removeDocent(id)
    Redirect(routes.CEditDocents.page).flashing("connectedSubjects" -> connectedSubjects.mkString(" "), "docentName" -> docentName).withSession(session + ("docentName" -> docentName))
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
