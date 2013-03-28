package code.comet

import net.liftweb.actor.LiftActor
import net.liftweb.util.Schedule
import net.liftweb.util.TimeHelpers.TimeSpan._
import net.liftweb.common.{Failure, Empty, Full, Logger}
import net.liftweb.json.{Printer, Extraction, JsonAST}

class AnApplication(name: String, requestingTo: String) extends LiftActor with Logger {
  var nextDelay = 10

  protected def messageHandler = {
    case Blah => {
      implicit val formats = net.liftweb.json.DefaultFormats
      LogEventServer ! Printer.compact(JsonAST.render(Extraction.decompose(TimedWebServiceRequest(name, requestingTo))))
      nextDelay = (nextDelay + 1) % 300
      Schedule.schedule(this, Blah, nextDelay);
    }
  }
}


case class TimedWebServiceRequest(application: String, request_app: String)