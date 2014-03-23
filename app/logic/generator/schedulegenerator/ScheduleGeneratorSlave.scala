package logic.generator.schedulegenerator

import akka.actor.Actor

/**
 * @author fabian 
 *         on 23.03.14.
 */
class ScheduleGeneratorSlave extends Actor{

  override def receive = {

    case SlaveGenerate(lectures)=>

    case _=>
  }

}
