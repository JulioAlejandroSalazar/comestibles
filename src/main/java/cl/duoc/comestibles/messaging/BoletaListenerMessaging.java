package cl.duoc.comestibles.messaging;

import com.rabbitmq.client.Channel;
import cl.duoc.comestibles.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BoletaListenerMessaging {

    @RabbitListener(id = "listener-myQueue-2", queues = RabbitMQConfig.MAIN_QUEUE, ackMode = "MANUAL")
    public void recibirMensajeConAckManual(Message mensaje, Channel canal) throws IOException {
        try {
            String body = new String(mensaje.getBody());
            System.out.println("Mensaje recibido: \n" + body);

            if (body.contains("error")) {
                throw new RuntimeException("Contenido contiene 'error', se enviará a la DLQ");
            }

            Thread.sleep(5000);

            canal.basicAck(mensaje.getMessageProperties().getDeliveryTag(), false);
            System.out.println("Acknowledge OK enviado");

        } catch (Exception e) {
            canal.basicNack(mensaje.getMessageProperties().getDeliveryTag(), false, false);
            System.out.println("Acknowledge NO OK enviado (irá a la DLQ si está configurada)");
        }
    }
}
