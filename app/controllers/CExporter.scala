package controllers

import play.api.mvc._
import views.html.exporter._
import play.api.libs.json._
import models.Transactions
import models.persistence.location.HouseEntity
import org.hibernate.FetchMode
import models.fhs.pages.JavaList
import models.persistence.subject.AbstractSubject
import models.export.JsonContainer
import com.fasterxml.jackson.databind.ObjectMapper
import models.persistence.template.WeekdayTemplate
import scala.collection.JavaConversions._
import models.persistence.criteria.TimeslotCriteria
import org.hibernate.criterion.CriteriaSpecification

/**
 * @author fabian 
 *         on 24.03.14.
 */
object CExporter extends Controller {

  val NAV = "Exporter"

  def page = Action {
    Ok(exporter("Expotieren/Importieren"))
  }

  def export = Action {
    implicit request =>



      val jsonExport = Transactions.hibernateAction {
        implicit session =>
          val houses = session.createCriteria(classOf[HouseEntity]).setFetchMode("rooms", FetchMode.JOIN).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[HouseEntity]]
          val subjectCriterion = session.createCriteria(classOf[AbstractSubject]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)

          subjectCriterion.createCriteria("courses").setFetchMode("groups", FetchMode.JOIN)

          val subjects = subjectCriterion.list().asInstanceOf[JavaList[AbstractSubject]]

          val weekdayTemplates = session.createCriteria(classOf[WeekdayTemplate]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[WeekdayTemplate]]

          val jsonContainer = new JsonContainer
          jsonContainer.setHouses(houses)
          jsonContainer.setSubjects(subjects)
          jsonContainer.setWeekdayTemplates(weekdayTemplates)
          jsonContainer
      }

      val mapper = new ObjectMapper

      Ok(Json.prettyPrint(Json.parse(mapper.writeValueAsString(jsonExport)))).withHeaders(("Content-disposition", "attachment; filename=data.json"))

  }

  def uploadFile = Action(parse.multipartFormData) {
    request =>
      request.body.file("fileUpload").map {
        file =>
          val mapper = new ObjectMapper

          import java.io.File
          val filename = file.filename
          val tmpFile = File.createTempFile(filename, "")

          tmpFile.deleteOnExit()
          file.ref.moveTo(tmpFile, replace = true)

          val jsonContainer = mapper.readValue(tmpFile, classOf[JsonContainer])



          Transactions {
            implicit em =>
              val weekdayTemplates = jsonContainer.getHouses.flatMap(_.getRooms.flatMap(_.getCriteriaContainer.getCriterias.map {
                case tcrit: TimeslotCriteria => tcrit.getWeekday
              })).toSet

              weekdayTemplates.foreach(em.persist(_))

              jsonContainer.getWeekdayTemplates.diff(weekdayTemplates.toSeq).map(em.persist(_))
          }

          Transactions {
            implicit em =>
              jsonContainer.getHouses.foreach(em.persist(_))
          }

          Transactions {
            implicit em =>

             // val semesters = jsonContainer.getSubjects.map(_.getSemester).toSet
            val courses = jsonContainer.getSubjects.flatMap(_.getCourses).toSet
            courses.foreach{
              course =>
                val groups =course.getGroups.toSet
                course.setGroups( groups.toList )
              em.persist(course)}

             // semesters.foreach(em.persist(_))
          }

          Transactions {
            implicit em =>

              val docents = jsonContainer.getSubjects.flatMap(_.getDocents).toSet
            docents.foreach(em.persist(_))

            jsonContainer.getSubjects.foreach(em.persist(_))
          }



      }

      Redirect(routes.CExporter.page())
  }
}
