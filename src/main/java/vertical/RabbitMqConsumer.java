package vertical;

import com.rabbitmq.client.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import org.apache.log4j.Logger;
import tool.DButil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;


public class RabbitMqConsumer extends AbstractVerticle {

    private Connection connection;
    private Channel channel = null;
    private static final String exchangeName = "message-exchange";
    private static final String queueName = "message-queue";
    private static final String routingKey = "message-routing";
    private static final String updateLastMessage = "update im_message_last set `msg_id`=? ,`modify_time`=? ,`from` =? ,`to`=? where `mix_id`=?";
    private static final String insertLastMessage = "insert into im_message_last (`msg_id`,`from`,`to`,`mix_id`,`modify_time`) values (?,?,?,?,?)";
    private WorkerExecutor executor;
    private static final Logger logger = Logger.getLogger(RabbitMqConsumer.class);

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
        executor = vertx.createSharedWorkerExecutor("my-worker-pool");

        try {
            connection = factory.newConnection();
            channel = connection.createChannel(20);
            channel.exchangeDeclare(exchangeName, "direct", true, false, null);
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, exchangeName, routingKey);
            String sql = "insert into im_message  (`message_id`,`from`,`to`,`media`,`body`,`send_time`,`link`,`mix_id`,`voiceTime`) values (?,?,?,?,?,?,?,?,?) ";
            boolean autoAck = false;
            channel.basicConsume(queueName, autoAck, "",
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body)

                        {
                            try {
                                String message = new String(body, "utf-8");

                                JsonObject jsonObject = new JsonObject(message);

                                DButil.getJdbcClient().getConnection(res -> {
                                    if (res.succeeded()) {
                                        SQLConnection connection = res.result();


                                        String message_id = jsonObject.getString("message_id", "");
                                        String sqlQuery = "select count(*) from im_message where message_id= '" + message_id + "'";
                                        connection.querySingle(sqlQuery, re -> {
                                            if (re.succeeded()) {
                                                long deliveryTag = envelope.getDeliveryTag();
                                                try {
                                                    channel.basicAck(deliveryTag, false);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                JsonArray result = re.result();
                                                Integer integer = result.getInteger(0);
                                                if (integer == 0) {
                                                    //没存就插入一条
                                                    String from = jsonObject.getString("from", "");
                                                    String to = jsonObject.getString("to", "");

                                                    Long lfrom = Long.valueOf(from);
                                                    Long lto = Long.valueOf(to);

                                                    BigDecimal v1 = new BigDecimal(lfrom).add(new BigDecimal(lto));
                                                    BigDecimal v2 = new BigDecimal(lfrom).add(new BigDecimal(lto + 1))
                                                            .divide(new BigDecimal(2));
                                                    BigDecimal v3 = new BigDecimal(Math.min(lfrom, lto));
                                                    BigDecimal mixId = v1.multiply(v2).add(v3);
                                                    String link = jsonObject.getString("link", "");
                                                    JsonArray json = new JsonArray()
                                                            .add(message_id)
                                                            .add(String.valueOf(from))
                                                            .add(String.valueOf(to))
                                                            .add(jsonObject.getString("media", "text"))
                                                            .add(jsonObject.getString("body", ""))
                                                            .add(jsonObject.getLong("send_time", System.currentTimeMillis() / 1000L))
                                                            .add(link != null ? link : "")
                                                            .add(String.valueOf(mixId))
                                                            .add(jsonObject.getInteger("voiceTime"));

                                                    logger.debug(json);

                                                    if (!from.equals(to)) {

                                                        DButil.getJdbcClient().getConnection(insert ->{
                                                            SQLConnection result2 = insert.result();
                                                            result2.updateWithParams(sql, json, re1 -> {
                                                                if (re1.failed()) {
                                                                    re1.cause().printStackTrace();

                                                                } else {
                                                                    DButil.getJdbcClient().getConnection(selectIdHan->{
                                                                        SQLConnection selectCon = selectIdHan.result();
                                                                        selectCon.querySingle("select id from im_message where `message_id`='" + message_id + "'", handler -> {
                                                                            if (handler.succeeded()) {
                                                                                Integer id = handler.result().getInteger(0);
                                                                                JsonArray array = new JsonArray()
                                                                                        .add(id)
                                                                                        .add(System.currentTimeMillis())
                                                                                        .add(from)
                                                                                        .add(to)
                                                                                        .add(String.valueOf(mixId));

                                                                                //update im_message_last set `msg_id`=? ,`modify_time`=? ,`from` =? ,`to`=? where `mix_id`=?
                                                                                DButil.getJdbcClient().getConnection(updatLastHan ->{
                                                                                    SQLConnection updateLastCon = updatLastHan.result();
                                                                                    updateLastCon.updateWithParams(updateLastMessage, array, handler1 -> {
                                                                                        if (handler1.failed()) {
                                                                                            logger.error(handler1.cause());

                                                                                        } else {
                                                                                            UpdateResult result1 = handler1.result();
                                                                                            int updated = result1.getUpdated();
                                                                                            if (Integer.valueOf(updated) == 0) {
                                                                                                insertmessagelast(id, from, to, String.valueOf(mixId));
                                                                                            }


                                                                                        }
                                                                                        updateLastCon.close();

                                                                                    });
                                                                                });



                                                                            }
                                                                            selectCon.close();
                                                                        });
                                                                    });



                                                                }
                                                                result2.close();
                                                            });

                                                        });
                                                    }




                                                }
                                            }

                                            connection.close();
                                        });


                                    } else {
                                        res.cause().printStackTrace();
                                    }
                                });


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updatemessagelast(String msgId, String from, String to, String mixId) {
        DButil.getJdbcClient().getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection con = res.result();
                JsonArray array = new JsonArray()
                        .add(msgId)
                        .add(System.currentTimeMillis() / 1000)
                        .add(from)
                        .add(to)
                        .add(mixId);
                //"update message_last set `msg_id`=? ,`modify_time`=? ,`from` =? ,`to`=? where `mix_id`=?"
                con.updateWithParams(updateLastMessage, array, handler -> {
                    if (handler.failed()) {
                        logger.error(handler.cause());
                    }
                    con.close();
                });
            }
        });

    }

    private void insertmessagelast(Integer id, String from, String to, String mixId) {
        DButil.getJdbcClient().getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection con = res.result();
                JsonArray array = new JsonArray()
                        .add(id)
                        .add(from)
                        .add(to)
                        .add(mixId)
                        .add(System.currentTimeMillis() / 1000)
                        ;
                //"insert into im_message_last (`msg_id`,`from`,`to`,`mix_id`,`modify_time`) values (?,?,?,?,?)
                con.updateWithParams(insertLastMessage, array, handler -> {
                    if (handler.failed()) {
                        logger.error(handler.cause());
                    }
                    con.close();
                });
            }
        });


    }


    @Override
    public void stop() throws Exception {
        channel.close();
        connection.close();
    }
}
