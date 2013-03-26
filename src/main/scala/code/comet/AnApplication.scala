package code.comet

import net.liftweb.actor.LiftActor
import net.liftweb.util.Schedule
import net.liftweb.util.TimeHelpers.TimeSpan._
import net.liftweb.common.Logger

class AnApplication(name: String, requestingTo: String) extends LiftActor with Logger {
  var nextDelay = 10

  protected def messageHandler = {
    case Blah => {
      LogEventServer ! TimedWebServiceRequest(name, requestingTo)
      nextDelay = (nextDelay + 1) % 100
      Schedule.schedule(this, Blah, nextDelay);
    }
  }
}


case class TimedWebServiceRequest(from: String, to: String)