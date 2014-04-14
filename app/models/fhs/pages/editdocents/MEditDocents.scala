package models.fhs.pages.editdocents

import models.persistence.Docent
import models.persistence.criteria.CriteriaContainer
import models.Transactions
import org.hibernate.criterion.{Restrictions, CriteriaSpecification}
import org.hibernate.FetchMode
import models.fhs.pages.JavaList
import scala.collection.JavaConversions._

/**
 * @author fabian 
 *         on 13.04.14.
 */
object MEditDocents {
  def persistNewDocent(mDocent: MDocent) {
    val docent = new Docent

    docent.setLastName(mDocent.lastName)

    docent.setCriteriaContainer(new CriteriaContainer)

    Transactions {
      implicit em =>
        em.persist(docent)
    }
  }

  def findAllDocents()={
    Transactions.hibernateAction{
      implicit s=>
        s.createCriteria(classOf[Docent]).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).setFetchMode("criteriaContainer",FetchMode.SELECT).list().asInstanceOf[JavaList[Docent]].toList.sorted
    }
  }

  def findDocentById(id:Long)={
    Transactions.hibernateAction {
      implicit s =>
        s.createCriteria(classOf[Docent]).add(Restrictions.idEq(id)).uniqueResult().asInstanceOf[Docent]
    }
  }

  implicit def docent2MExistingDocent(docent:Docent)={
    MExistingDocent(docent.getId,docent.getLastName)
  }
}

case class MDocent(lastName: String)

case class MExistingDocent(id:Long,lastName:String)

