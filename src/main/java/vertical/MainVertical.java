package vertical;

import consumer.FriendConsumer;
import consumer.MarkConsumer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import model.DataReqRepMessage;
import model.DataReqRepMessageCover;
import org.apache.log4j.Logger;
import tool.DButil;
import tool.RedisUtil;

public class MainVertical extends AbstractVerticle {
    public static final Logger logger = Logger.getLogger(MainVertical.class);

    public static void main(String[] args) {

        Vertx.vertx().deployVerticle(MainVertical.class.getName());
    }


    @Override
    public void start() {
        try {
            DButil.init(vertx,null);
            RedisUtil.init(vertx,null);
            vertx.eventBus().registerDefaultCodec(DataReqRepMessage.class, new DataReqRepMessageCover());

            vertx.deployVerticle(RestApiVertical.class.getName(),new DeploymentOptions().setWorker(true),re ->{
                if(re.succeeded()) {
                    System.out.println("success " + re.result());
                } else  {
                    System.out.println("failed " + re.cause());
                }
             });

            vertx.deployVerticle(FriendConsumer.class.getName(),new DeploymentOptions().setWorker(true), re ->{
                if(re.succeeded()) {
                    System.out.println("success " + re.result());
                } else  {
                    System.out.println("failed " + re.cause());
                }
            });

            vertx.deployVerticle(MarkConsumer.class.getName(),new DeploymentOptions().setWorker(true),re -> {
                if(re.succeeded()) {
                    System.out.println("success " + re.result());
                } else  {
                    System.out.println("failed " + re.cause());
                }
            });

            vertx.deployVerticle(RabbitMqConsumer.class.getName(),new DeploymentOptions().setWorker(true),re -> {
                if(re.succeeded()) {
                    System.out.println("success " + re.result());
                } else  {
                    System.out.println("failed " + re.cause());
                }

            });
            vertx.deployVerticle(RabbiMqStart.class.getName(),new DeploymentOptions().setWorker(true),re -> {
                if(re.succeeded()) {
                    System.out.println("success " + re.result());
                } else  {
                    System.out.println("failed " + re.cause());
                }

            });
        } catch (Exception e) {
            logger.error("启动服务失败!");
        }
    }



}
