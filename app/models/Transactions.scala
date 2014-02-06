package models

/**
 * Created by fabian on 06.02.14.
 */

import javax.persistence.{EntityManager, Persistence}
import org.hibernate.Session

object Transactions {

  val emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")

  def apply[A](action: EntityManager => A): A = {
    val entityManager = emf.createEntityManager()

    def transaction = entityManager.getTransaction
    transaction.begin()
    try {
      val result = action(entityManager)
      if (transaction.isActive) {
        transaction.commit()
      }
      result
    }
    catch {
      case exception: Throwable =>
        if (transaction != null && transaction.isActive)
          transaction.rollback()
        throw exception
    }
    finally {
      entityManager.close()
    }
  }

  def hibernateAction[A](action: Session => A): A = {
    val session = emf.createEntityManager().getDelegate.asInstanceOf[Session]

    def transaction = session.getTransaction

    transaction.begin()
    try {
      val result = action(session)
      if (transaction.isActive) {
        transaction.commit()
      }
      result
    } catch {
      case exception: Throwable => {
        if (transaction != null && transaction.isActive)
          transaction.rollback()
        throw exception
      }
    }
    finally {
      session.close()
    }
  }

  def rollback()(implicit session: Session) {
    session.getTransaction.rollback()
  }

  def rollback()(implicit entitymanager: EntityManager) {
    entitymanager.getTransaction().rollback()
  }

}
