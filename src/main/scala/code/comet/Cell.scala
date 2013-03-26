package code.comet

import net.liftweb.http.{SHtml, Templates, S}
import net.liftweb.util.Helpers._
import net.liftweb.http.js.jquery.{JqJE, JqJsCmds}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.{JsCmds, JsMember, JsCmd}
import net.liftweb.http.js.JE.{AnonFunc, JsRaw}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.DefaultFormats
import net.liftweb.http.js.jquery.JqJsCmds.ModalDialog

case class Cell(name: String, coordinates: Coordinates, width: Int = 200, height: Int = 50, id: String = nextFuncName) {
  def x = coordinates.x
  def y = coordinates.y

  def add: JsCmd = {
    val onClick = AnonFunc(SHtml.ajaxCall(JsRaw(""), (value: String) => {
      ModalDialog(<div>
        <span>Zoom in on {name}</span>
        {SHtml.ajaxButton("Close", () => JqJsCmds.Unblock)}
      </div>)
    })).toJsCmd

    Run("graph.addNode({x: %s, y: %s, name: '%s', width: %s, height: %s, onClick: %s})".format(x, y, name, width, height, onClick))
  }
}