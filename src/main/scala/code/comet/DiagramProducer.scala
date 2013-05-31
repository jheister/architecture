package code.comet

import net.liftweb.http._
import net.liftweb.util.{Helpers}
import net.liftweb.common.Logger
import net.liftweb.http.{SHtml, S}
import net.liftweb.http.js.jquery.{JqJsCmds}
import net.liftweb.http.js.{JsCmds}
import net.liftweb.http.js.jquery.JqJsCmds.ModalDialog


class DiagramProducer extends CometActor with CometListener with Logger {
  val widget = DiagramWidget(1600, 800, onDrop, onCellClick)

  protected def registerWith = LogEventServer

  override protected def dontCacheRendering = true

  override def lowPriority = {
    case AddNode(name) => {
      widget.add(Cell(name, widget.freeCoordinates)).foreach(cmd => partialUpdate(cmd))
    }

    case AddEdge(from, to, weight) => {
      widget.add(Edge(from, to, weight)).foreach(cmd => partialUpdate(cmd))
    }

    case Dropped(name, coordinates) => {
      info("Dropped %s at %s".format(name, coordinates))
    }
  }

  def onDrop(drop: Dropped) = {
    S.session.get.findComet("DiagramProducer").foreach(_ ! drop)
    JsCmds.Noop
  }

  def onCellClick(value: String) = {
    ModalDialog(<div>
      <span>Zoom in on {name}</span>
      {SHtml.ajaxButton("Close", () => JqJsCmds.Unblock)}
    </div>)
  }

  def render = ".theWidget" #> widget.init
}

case class AddNode(name: String)

case class AddEdge(from: String, to: String, weight: Int)

case class Dropped(name: String, coordinates: Coordinates)

