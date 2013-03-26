package code
package snippet

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import net.liftweb.http.{ScopedLiftActor, S}
import net.liftweb.actor.LiftActor
import net.liftweb.json.JsonAST.{JValue, JString}
import net.liftweb.http.js.JsCmds.Script
import net.liftweb.http.js.JE.JsRaw
import util.parsing.json.JSONFormat
import net.liftweb.json.{DefaultFormats, Formats}

class HelloWorld extends Logger {
  lazy val date: Box[Date] = DependencyFactory.inject[Date] // inject the date

  def howdy = (for {
    sess <- S.session
  } yield {
    val ui = sess.serverActorForClient("receive")
    val backEnd = new ScopedLiftActor {
      override def lowPriority = {
        case Foo(bar) => {ui ! Foo("Hello, I am a foo")}
      }
    }

    Script(JsRaw("var sendMessage = " + sess.clientActorFor(backEnd, (value: JValue) => {
      implicit val format = DefaultFormats

      value.extractOpt.map(Full(_)).getOrElse(Empty)
    }).toJsCmd).cmd & JsRaw("function receive(value) { alert(value.bar); }").cmd)
  }) openOr NodeSeq.Empty
}

case class Foo(bar: String)

