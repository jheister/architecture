package code.comet

import net.liftweb.http._
import js.JsCmds.Run
import net.liftweb.util.{Helpers}
import Helpers._
import util.Random
import net.liftweb.http.js.JsCmds
import net.liftweb.common.Logger
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.{DefaultFormats}
import scala.Some

class DiagramProducer extends CometActor with CometListener with Logger {
  val graph = Area(Coordinates(0,0), Coordinates(1600, 800))
  val nodes: scala.collection.mutable.Map[String, Cell] = scala.collection.mutable.Map()
  val edges: scala.collection.mutable.Map[String, Edge] = scala.collection.mutable.Map()

  protected def registerWith = LogEventServer

  override protected def dontCacheRendering = true

  override def lowPriority = {
    case AddNode(name) => {
      if (nodes.get(name).isEmpty) {
        val cell = Cell(name, DiagramProducer.freeCoordinates(nodes.values, graph.quarter))
        partialUpdate(cell.add)
        nodes.put(name, cell)
      }
    }

    case AddEdge(from, to, weight) => {
      val option = edges.get(from + to)
      if (option.isEmpty) {
        val edge = Edge(from, to, weight)
        partialUpdate(edge.add)
        edges.put(from + to, edge)
      } else {
        val edge = Edge(from, to, weight)
        if (option.get != edge) {
          partialUpdate(edge.update)
          edges.put(from + to, edge)
        }
      }
    }

    case Dropped(name, coordinates) => {
      info("Dropped %s at %s".format(name, coordinates))
      nodes.get(name).map(_.copy(coordinates = coordinates)).foreach(nodes.put(name, _))
    }
  }

  val cmd: String = SHtml.jsonCall(JsRaw("val"), (value: JValue) => {
    implicit val formats = DefaultFormats
    S.session.get.findComet("DiagramProducer").foreach(_ ! value.extract[Dropped])
    JsCmds.Noop
  }).toJsCmd

  def render = ".init *" #> Run("var graph = new Graphing.Graph(function(val) { %s }, %s, %s); graph.render();".format(cmd, graph.end.x, graph.end.y)) &
               ".nodes *" #> JsCmds.seqJsToJs(nodes.values.map(_.add).toSeq) &
               ".edges *" #> JsCmds.seqJsToJs(edges.values.map(_.add).toSeq)
}

object DiagramProducer {
  def freeCoordinates(nodes: Iterable[Cell], areas: List[Area]): Coordinates = {
    val (count, area) = areas.map(_.count(nodes)).zip(areas).sortBy(_._1).head

    if (count == 0) {
      area.randomPosition
    } else {
      freeCoordinates(nodes, area.quarter)
    }
  }
}

case class AddNode(name: String)

case class AddEdge(from: String, to: String, weight: Int)

case class Dropped(name: String, coordinates: Coordinates)

