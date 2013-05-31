package code.comet

import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._
import scala.Some
import net.liftweb.json.DefaultFormats
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.{Num, AnonFunc, JsRaw}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.http.js.JsCmds.{Run, JsCrVar}
import net.liftmodules.extras.JsExtras.CallNew
import net.liftweb.http._
import js.JE.JsRaw
import js.JE.JsVar
import js.JsCmds.Run
import js.JsCmds.SetExp
import js.JsCmds.{SetExp, Run}
import net.liftweb.util.{Helpers}
import Helpers._
import util.Random
import js.{JsCmd, JsExp, JsCmds}
import net.liftweb.common.Logger
import js.JE._
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.{DefaultFormats}
import scala.Some
import net.liftmodules.extras.JsExtras.CallNew
import net.liftweb.http.{SHtml, Templates, S}
import net.liftweb.util.Helpers._
import net.liftweb.http.js.jquery.{JqJE, JqJsCmds}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.{JsCmds, JsMember, JsCmd}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.DefaultFormats
import net.liftweb.http.js.jquery.JqJsCmds.ModalDialog
import net.liftmodules.extras.JsExtras.CallNew
import xml.{Elem, NodeSeq}

case class DiagramWidget(width: Int,
                         height: Int,
                         onDrop: Dropped => JsCmd,
                         cellOnClick: String => JsCmd) {
  val name = nextFuncName
  val graph = Area(Coordinates(0,0), Coordinates(width, height))
  val nodes: scala.collection.mutable.Map[String, Cell] = scala.collection.mutable.Map()
  val edges: scala.collection.mutable.Map[String, Edge] = scala.collection.mutable.Map()

  def init = {
    implicit val formats = DefaultFormats
    val dropFunction = SHtml.jsonCall(JsRaw("val"), (value: JValue) => {
      update(value.extract[Dropped])
      onDrop(value.extract[Dropped])
    })

    <div id={name}>
      <script>{(JsCrVar(name, CallNew("Graphing.Graph", AnonFunc("val", dropFunction), Num(width), Num(height), name)) &
        Run(name + ".render()") &
        nodes.values.map(addCell).toSeq &
        edges.values.map(addEdge).toSeq).toJsCmd}
      </script>
    </div>
  }

  def add(cell : Cell): Option[JsCmd] = {
    if (nodes.get(cell.name).isEmpty) {
      nodes.put(cell.name, cell)
      Some(addCell(cell))
    } else {
      None
    }
  }

  private def addCell(cell: Cell) =
    Run(name + ".addNode({x: %s, y: %s, name: '%s', width: %s, height: %s, onClick: %s})".format(cell.x, cell.y, cell.name, cell.width, cell.height, AnonFunc(SHtml.ajaxCall(JsRaw(""), cellOnClick)).toJsCmd))

  private def update(drop: Dropped) =
    nodes.get(drop.name).map(_.copy(coordinates = drop.coordinates)).foreach(nodes.put(drop.name, _))

  def add(edge: Edge): Option[JsCmd] = {
    edges.get(edge.id) match {
      case None => {
        edges.put(edge.id, edge)
        Some(addEdge(edge))
      }
      case Some(existingEdge) if existingEdge != edge => {
        edges.put(edge.id, edge)
        Some(updateEdge(edge))
      }
      case _ => None
    }
  }

  private def addEdge(edge: Edge) =
    Run(name + ".addEdge({from: '%s', to: '%s', weight: %s});".format(edge.from, edge.to, edge.weight))

  private def updateEdge(edge: Edge) =
    Run(name + ".updateEdge({from: '%s', to: '%s', weight: %s});".format(edge.from, edge.to, edge.weight))

  def freeCoordinates: Coordinates =
    freeCoordinates(nodes.values, graph.quarter)

  def freeCoordinates(nodes: Iterable[Cell], areas: List[Area]): Coordinates = {
    val (count, area) = areas.map(_.count(nodes)).zip(areas).sortBy(_._1).head

    if (count == 0) {
      area.randomPosition
    } else {
      freeCoordinates(nodes, area.quarter)
    }
  }
}
