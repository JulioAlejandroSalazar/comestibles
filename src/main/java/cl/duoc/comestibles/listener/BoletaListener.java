package cl.duoc.comestibles.listener;

import com.rabbitmq.client.Channel;

import cl.duoc.comestibles.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class BoletaListener {

    @RabbitListener(id = "listener-myQueue", queues = RabbitMQConfig.MAIN_QUEUE, ackMode = "MANUAL")
    public void recibirMensajeConAckManual(Message mensaje, Channel canal) throws IOException {
        try {
            String body = new String(mensaje.getBody());
            System.out.println("Mensaje recibido: \n" + body);

            // Validar si el contenido textual contiene la palabra clave "error"
            if (body.contains("error")) {
                throw new RuntimeException("Mensaje contiene 'error', se redirigirá a la DLQ");
            }

            Thread.sleep(3000);

            canal.basicAck(mensaje.getMessageProperties().getDeliveryTag(), false);
            System.out.println("ACK enviado correctamente");

        } catch (Exception e) {
            canal.basicNack(mensaje.getMessageProperties().getDeliveryTag(), false, false);
            System.out.println("NACK enviado → mensaje irá a la DLQ");
        }
    }
}
