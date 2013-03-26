package code.comet

import net.liftweb.http.Templates
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds.Run
import net.liftweb.http.js.JsCmd

case class Edge(from: String, to: String, weight: Int) {
  def add: JsCmd = {
    Run("graph.addEdge({from: '%s', to: '%s', weight: %s});".format(from, to, weight))
  }

  def update: JsCmd = {
    Run("graph.updateEdge({from: '%s', to: '%s', weight: %s});".format(from, to, weight))
  }
}
