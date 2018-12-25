package vertical;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class RabbiMqStart extends AbstractVerticle {
    private Connection connection ;
    private Channel channel = null;
    private static final String exchangeName = "message-exchange_";
    private static final String queueName = "message-queue_";
    private static final String routingKey = "message-routing_";

    @Override
    public void start() throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("139.196.136.209");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("123456");
        factory.setVirtualHost("/");
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(5000);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel(20);
            channel.exchangeDeclare(exchangeName, "direct", true,false,null);
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, exchangeName, routingKey);
            vertx.eventBus().consumer("saveMessage",re ->{
                JsonObject message = (JsonObject) re.body();

                try {
                    channel.basicPublish(exchangeName,routingKey,null,message.toString().getBytes());
                    re.reply(new JsonObject());
                } catch (Exception e) {
                    e.printStackTrace();
                }


            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        channel.close();
        connection.close();
    }
}
