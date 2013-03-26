package code.comet

import net.liftweb.http.{SHtml, Templates, S}
import net.liftweb.util.Helpers._
import net.liftweb.http.js.jquery.{JqJE, JqJsCmds}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.{JsCmds, JsMember, JsCmd}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.DefaultFormats

case class Cell(name: String, coordinates: Coordinates, width: Int = 200, height: Int = 50, id: String = nextFuncName) {
  def x = coordinates.x
  def y = coordinates.y

  def add: JsCmd = {
    Run("graph.addNode({x: %s, y: %s, name: '%s', width: %s, height: %s})".format(x, y, name, width, height))
  }
}