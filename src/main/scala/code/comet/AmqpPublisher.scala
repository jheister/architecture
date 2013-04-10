package code.comet

import net.liftweb.actor.LiftActor
import net.liftweb.json.JsonAST.JValue
import net.liftmodules.amqp.{AMQPMessage, AMQPSender}
import com.rabbitmq.client.{Connection, Channel, ConnectionFactory}
import net.liftweb.json.{JsonAST, Printer}
import java.nio.charset.Charset


object AmqpPublisher extends LiftActor {
  val newConnection: Connection = new ConnectionFactory().newConnection("localhost", 5672)
  val channel = newConnection.createChannel()

  protected def messageHandler = {
    case v: JValue => {
      channel.basicPublish("blah", "", null, Printer.compact(JsonAST.render(v)).getBytes(Charset.forName("UTF-8")))
    }
  }
}
