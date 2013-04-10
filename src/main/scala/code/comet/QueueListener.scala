package code.comet

import net.liftmodules.amqp.{AMQPMessage, SerializedConsumer, AMQPDispatcher}
import com.rabbitmq.client._
import java.io.{ByteArrayInputStream, ObjectInputStream}
import net.liftweb.json.JsonAST
import net.liftweb.common.Logger
import net.liftweb.actor.LiftActor
import java.nio.charset.Charset

class QueueListener extends AMQPDispatcher(new ConnectionFactory(), "localhost", 5672) with Logger {
  def configure(channel: Channel) {
    channel.queueDeclare("blahQueue", false)
    channel.queueBind("blahQueue", "blah", "*")
    channel.basicConsume("blahQueue", false, new JsonConsumer(channel, this))
  }
}

class JsonConsumer(channel: Channel, recipient: LiftActor) extends DefaultConsumer(channel) {
  override def handleDelivery(tag: String, env: Envelope, props: AMQP.BasicProperties, body: Array[Byte]) {
    val deliveryTag = env.getDeliveryTag

    try {
      recipient ! AMQPMessage(net.liftweb.json.parse(new String(body, Charset.forName("UTF-8"))))
    } catch {
      case e: Exception => e.printStackTrace()
    }

    channel.basicAck(deliveryTag, false);
  }
}