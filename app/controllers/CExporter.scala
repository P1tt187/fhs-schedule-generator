package controllers

import play.api.mvc._
import views.html.exporter._
import models.Transactions
import models.persistence.location.{RoomAttributesEntity, HouseEntity}
import org.hibernate.FetchMode
import models.fhs.pages.JavaList
import models.persistence.subject.AbstractSubject
import models.export.JsonContainer
import com.fasterxml.jackson.databind.ObjectMapper
import models.persistence.template.WeekdayTemplate
import scala.collection.JavaConversions._
import models.persistence.criteria.TimeSlotCriteria
import org.hibernate.criterion.{Restrictions, CriteriaSpecification}
import models.fhs.pages.exporter.MExporter._
import models.fhs.pages.generator.MGenerator._
import java.nio.file._
import models.persistence.participants.{Group, Course}
import models.persistence.scheduletree.{TimeSlot, Weekday}
import models.persistence.lecture.Lecture
import models.persistence.enumerations.ELectureKind
import play.api.libs.json._
import java.nio.charset.Charset
import java.net.URI


/**
 * @author fabian 
 *         on 24.03.14.
 */
object CExporter extends Controller {

  val NAV = "Exporter"

  def page = Action {
    import models.fhs.pages.exporter.MExporter._
    Ok(exporter("Expotieren/Importieren", findSemesters))
  }

  def export = Action {

    implicit request =>

      val jsonExport = Transactions.hibernateAction {
        implicit session =>
          val houses = session.createCriteria(classOf[HouseEntity]).setFetchMode("rooms", FetchMode.JOIN).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[HouseEntity]]
          val subjectCriterion = session.createCriteria(classOf[AbstractSubject]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)

          subjectCriterion.createCriteria("courses").setFetchMode("groups", FetchMode.JOIN)

          //val subjects = subjectCriterion.list().asInstanceOf[JavaList[AbstractSubject]]

          val weekdayTemplates = session.createCriteria(classOf[WeekdayTemplate]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list().asInstanceOf[JavaList[WeekdayTemplate]]

          val jsonContainer = new JsonContainer
          jsonContainer.setHouses(houses)
          //jsonContainer.setSubjects(subjects)
          jsonContainer.setSubjects(List[AbstractSubject]())
          jsonContainer.setWeekdayTemplates(weekdayTemplates)
          jsonContainer
      }

      val mapper = new ObjectMapper

      Ok(Json.prettyPrint(Json.parse(mapper.writeValueAsString(jsonExport)))).withHeaders(("Content-disposition", "attachment; filename=data.json"))

  }

  def createSpiritSchedule(id: Long) = Action {

    def timeSlotToString(timeSlot: TimeSlot) = {
      val sb = new StringBuilder

      sb append timeSlot.getStartHour.formatted("%02d")
      sb append "."
      sb append timeSlot.getStartMinute.formatted("%02d")
      sb append "-"
      sb append timeSlot.getStopHour.formatted("%02d")
      sb append "."
      sb append timeSlot.getStopMinute.formatted("%02d")
      sb.toString
    }


    val semester = findSemester(id)
    val schedule = findScheduleForSemester(semester)
    val fileName = "schedule" + semester.getName.replaceAll("/", "") + System.currentTimeMillis()
    val path = Files.createTempDirectory(fileName)

    val env = Map("create" -> "true")
    val zipUri = URI.create(("jar:file:" + path.getParent.toString + "/" + fileName + ".zip").replaceAll(" ", ""))
    val zipFs = FileSystems.newFileSystem(zipUri, env)



    val weekDays = Map((1, "Montag"), (2, "Dienstag"), (3, "Mittwoch"), (4, "Donnerstag"), (5, "Freitag"), (6, "Samstag"), (0, "Sonntag"))

    val importScriptFile = Files.createFile(Paths.get(path.toString, "import.sh"))

    var firstRun = true

    Transactions.hibernateAction {
      implicit s =>
        val courses = s.createCriteria(classOf[Course]).list().asInstanceOf[JavaList[Course]]
        var importCommands = courses.par.map {
          course =>

            val tmpFile = Files.createFile(Paths.get(path.toString, course.getName.toLowerCase + ".json"))
            val filteredSchedule = schedule.filter(course)

            val allTimeSlots = filteredSchedule.getRoot.getChildren.flatMap {
              case wd: Weekday =>
                wd.getChildren.asInstanceOf[JavaList[TimeSlot]]
            }

            val allLectures = filteredSchedule.getRoot.getChildren.flatMap {
              case wd: Weekday =>
                wd.getChildren.flatMap {
                  case ts: TimeSlot => ts.getLectures
                }
            }.toSet

            val plan = allLectures.map {
              case lecture: Lecture =>

                val timeSlot = allTimeSlots.filter(_.getLectures.contains(lecture)).head
                val day = weekDays(timeSlot.getParent.asInstanceOf[Weekday].getSortIndex)
                val eventType = lecture.getKind match {
                  case ELectureKind.LECTURE => "Vorlesung"
                  case ELectureKind.EXERCISE => "Uebung"
                }

                val group = lecture.getKind match {
                  case ELectureKind.LECTURE => ""
                  case ELectureKind.EXERCISE => lecture.getParticipants.find(_.getCourse.equals(course)).get.asInstanceOf[Group].getGroupIndex.toString
                }

                Json.stringify(Json.obj(
                  "appointment" -> Json.obj(
                    "day" -> day,
                    "location" -> Json.obj(
                      "alternative" -> Json.arr(),
                      "place" -> Json.obj(
                        "building" -> lecture.getRoom.getHouse.getName,
                        "room" -> lecture.getRoom.getNumber
                      )
                    ),
                    "time" -> timeSlotToString(timeSlot),
                    "week" -> lecture.getDuration.getShortName
                  ),
                  "className" -> course.getShortName.toLowerCase,
                  "eventType" -> eventType,
                  "group" -> (if (group.isEmpty) {
                    group
                  } else {
                    " " + group
                  }),
                  "member" ->
                    lecture.getDocents.map {
                      d =>
                        Json.obj("fhs_id" -> "",
                          "name" -> d.getLastName
                        )
                    }
                  ,
                  "titleLong" -> lecture.getName.replaceAll("Ä", "AE").replaceAll("Ö", "OE").replaceAll("Ü", "UE"),
                  "titleShort" -> lecture.getShortName
                )
                )
            }
            val content = List("[" + plan.mkString(",") + "]")

            Files.write(tmpFile, content, Charset.forName("UTF-8"), StandardOpenOption.WRITE)

            val sb = new StringBuilder

            sb append "mongoimport --db spirit_news --collection schedulerecords --type json --file "

            sb append course.getShortName.toLowerCase
            sb append ".json --jsonArray "

            if (firstRun && !content.head.trim.equals("[]")) {
              firstRun = false
              sb append "--drop"
            }
            if(content.head.trim.equals("[]")){
              sb append "NOIMPORT"
            }

            sb.toString()
        }.toList.filterNot(_.endsWith("NOIMPORT")).sortBy(-_.length)

        importCommands = "#!/bin/bash" :: "echo \"\" > ./entrycountersFields.txt" :: "echo \"\" > ./entrysFields.txt" :: "mongoexport --db spirit_news --collection entrycounters --fieldFile entrycountersFields.txt --csv -o entrycounters.csv" :: "mongoexport --db spirit_news --collection entrys --fieldFile entrysFields.txt --csv -o entrys.csv" :: importCommands

        importCommands = importCommands :+ "mongoimport --db spirit_news --collection entrycounters --type csv --file entrycounters.csv --fieldFile entrycountersFields.txt"

        importCommands = importCommands :+ "mongoimport --db spirit_news --collection entrys --type csv --file entrys.csv --fieldFile entrysFields.txt"

        Files.write(importScriptFile, importCommands, Charset.forName("UTF-8"), StandardOpenOption.WRITE)

        Files.newDirectoryStream(path).foreach {
          p =>
            val zipPath = zipFs.getPath("/", p.getFileName.toString)
            Files.copy(p, zipPath)
            Files.delete(p)
        }
        zipFs.close()
        Files.delete(path)
        val fileContent = Paths.get(path.getParent.toString, ("/" + fileName + ".zip").replaceAll(" ", "")).toFile

        fileContent.deleteOnExit()

        Ok.sendFile(content = fileContent, fileName = _ => semester.getName.replaceAll("/","") + ".zip")
    }


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

          Transactions.hibernateAction {
            implicit session =>
              val houseTemplates = jsonContainer.getHouses.flatMap(_.getRooms.flatMap(_.getCriteriaContainer.getCriterias.map {
                case tcrit: TimeSlotCriteria => tcrit.getWeekday
              }))

              (houseTemplates ++ jsonContainer.getWeekdayTemplates).map {
                wt =>
                  val dbresult = session.createCriteria(classOf[WeekdayTemplate]).add(Restrictions.eq("sortIndex", wt.getSortIndex)).uniqueResult()
                  if (dbresult == null) {
                    session.save(wt)
                  }
              }
          }

          Transactions.hibernateAction {
            implicit session =>
              jsonContainer.getHouses.foreach(_.getRooms.foreach(_.getRoomAttributes.foreach {
                attr =>
                  val dbResult = session.createCriteria(classOf[RoomAttributesEntity]).add(Restrictions.eq("attribute", attr.getAttribute)).uniqueResult()
                  if (dbResult == null) {
                    session.save(attr)
                  }
              }))

              jsonContainer.getHouses.foreach(session.save(_))
          }

          Transactions {
            implicit em =>

              // val semesters = jsonContainer.getSubjects.map(_.getSemester).toSet
              val courses = jsonContainer.getSubjects.flatMap(_.getCourses).toSet
              courses.foreach {
                course =>
                  val groups = course.getGroups.toSet
                  course.setGroups(groups.toList)
                  em.persist(course)
              }

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
