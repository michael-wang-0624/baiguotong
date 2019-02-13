package vertical;

import java.util.ArrayList;

import com.clevercloud.rabbitmq.RabbitMQAutoConnection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import tool.Constant;

public class RabbiMqStart extends AbstractVerticle {
    private Connection connection ;
    private Channel channel = null;
    private static final String exchangeName = "message-exchange_new";
    private static final String queueName = "message-queue_new";
    private static final String routingKey = "message-routing_new";

    @Override
    public void start() throws Exception {
 
    	ArrayList<String> list = new ArrayList<String>();
    	list.add(Constant.rabbit_host);
	    connection = new RabbitMQAutoConnection(list,Constant.rabbit_port,Constant.rabbit_username,Constant.rabbit_password,"/");
        try {
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
