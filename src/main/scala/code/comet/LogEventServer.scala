package code.comet

import net.liftweb.http.ListenerManager
import net.liftweb.actor.LiftActor
import net.liftweb.util.{TimeHelpers, Schedule}
import net.liftweb.util.TimeHelpers.TimeSpan
import util.Random

object LogEventServer extends LiftActor with ListenerManager {
  protected def createUpdate = null

  val services: List[String] = (1 to 10).map(_ => Random.alphanumeric.take(5).mkString).toList

  val requestRates: scala.collection.mutable.Map[String, List[Long]] = scala.collection.mutable.Map()

  override def lowPriority = {
    case TimedWebServiceRequest(from, to) => {
      requestRates.keys.foreach(key => {
        requestRates.put(key,requestRates.get(key).map(_.filterNot(_ < (System.currentTimeMillis() - 1000))).getOrElse(List()))
      })

//      val from :: to :: Nil = Random.shuffle(services).take(2)
      updateListeners(AddNode(from))
      updateListeners(AddNode(to))
      updateListeners(AddEdge(from, to, requestWeightToWeight(requestRates.get(from + to).map(_.size).getOrElse(1))))
//      updateListeners(AddEdge(from, to, Random.nextInt(10)))


      requestRates.put(from + to, requestRates.get(from + to).map(_ ++ List(System.currentTimeMillis())).getOrElse(List(System.currentTimeMillis())))

//      Schedule.schedule(LogEventServer, Blah, 100)
    }
  }

  def requestWeightToWeight(count: Int) = {
    count + 1
  }
}

object Blah

case class Node(name: String)

case class RequestMade(from: Node, to: Node)