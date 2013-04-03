package code.comet

import net.liftweb.http._
import js.JsCmds.{Script, Run}
import net.liftweb.util.{Helpers}
import Helpers._
import util.Random
import js.{JsExp, JsCmds}
import net.liftweb.common.{Full, Box, Logger}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.json.JsonAST.JValue
import scala.Some
import net.liftweb.actor.LiftActor
import net.liftweb.json
import json.{DefaultFormats, NoTypeHints}

class DiagramProducer extends CometActor with CometListener with Logger {
  val graph = Area(Coordinates(0,0), Coordinates(1600, 800))
  val nodes: scala.collection.mutable.Map[String, Cell] = scala.collection.mutable.Map()
  val edges: scala.collection.mutable.Map[String, Edge] = scala.collection.mutable.Map()

  var theGraph = new Graph(1600, 800, nodes.values.toSeq, edges.values.toSeq, this)

  protected def registerWith = LogEventServer

  override protected def dontCacheRendering = true

  override def lowPriority = {
    case AddNode(name) => {
      if (nodes.get(name).isEmpty) {
        val cell = Cell(name, DiagramProducer.freeCoordinates(nodes.values, graph.quarter))
        theGraph.addCell(cell)
        nodes.put(name, cell)
      }
    }

    case AddEdge(from, to, weight) => {
      val option = edges.get(from + to)
      if (option.isEmpty) {
        val edge = Edge(from, to, weight)
        theGraph.addEdge(edge)
        edges.put(from + to, edge)
      } else {
        val edge = Edge(from, to, weight)
        if (option.get != edge) {
          theGraph.updateEdge(edge)
          edges.put(from + to, edge)
        }
      }
    }

    case Full(c: Cell) => {
      info("XXXXXXXXXXXXXXXXXXXXXXXXXXXX: Dropped %s at %s".format(c.name, c.coordinates))
      nodes.put(c.name, c)
    }
  }

  def render = {
    theGraph = new Graph(1600, 800, nodes.values.toSeq, edges.values.toSeq, this)
    info("XXXXXXXXXXXXXXXXXXXXXX: Rendering!")
    nodes.values.foreach(theGraph.addCell)
    ".graph" #> theGraph.render
  }
}

case class AddCell(newCell: Cell)

case class AddArrow(newArrow: Edge)

case class UpdateArrow(updatedArrow: Edge)

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

class Graph(width: Int, height: Int, cells: Seq[Cell], arrows: Seq[Edge], backEnd: LiftActor) extends Logger {
  val graph = Area(Coordinates(0,0), Coordinates(width, height))

  val name = Helpers.nextFuncName

  val ui: Box[LiftActor] = S.session.map(_.serverActorForClient(name + ".processMessage"))

  implicit val formats = DefaultFormats
  val client: Box[JsExp] = S.session.map(_.clientActorFor(backEnd, e => e.extractOpt[Cell].map(Full(_))))

  def render = {
    import json._
    implicit val formats = Serialization.formats(NoTypeHints)

    client match {
      case Full(backEndProxy) => Script(Run("var " + name + " = new Graphing.Graph(%s, %s, %s)".format(backEndProxy.toJsCmd, graph.end.x, graph.end.y)) &
                                        Run(name + ".render()") &
                                        cells.map(cell => {
                                          Run(name + ".processMessage(%s)".format(Serialization.write(AddCell(cell))))
                                        }) &
                                        arrows.map(arrow => {
                                          Run(name + ".processMessage(%s)".format(Serialization.write(AddArrow(arrow))))
                                        }))
      case _ => <span>Failed to initialize</span>
    }
  }

  def addCell(cell: Cell) = {
    ui.foreach(_ ! AddCell(cell))
  }

  def addEdge(edge: Edge) = {
    ui.foreach(_ ! AddArrow(edge))
  }

  def updateEdge(edge: Edge) = {
    ui.foreach(_ ! UpdateArrow(edge))
  }
}

case class AddNode(name: String)

case class AddEdge(from: String, to: String, weight: Int)

case class Dropped(name: String, coordinates: Coordinates)

