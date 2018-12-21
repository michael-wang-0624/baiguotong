package vertical;


import io.agora.signal.Signal;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;
import tool.SignalingToken;


public class SignalVertical extends AbstractVerticle {
    private static final Logger logger = Logger.getLogger(SignalVertical.class);
    private static final String appId = "20aec784743645a6a48d0c2fdccc381d";
    @Override
    public void start() throws Exception {
        //
        Signal signal = new Signal(appId);
        // 登录 Agora 信令系统


        String token = SignalingToken.getToken(  "1");


        login(signal, token);


    }

    public void login(Signal signal, String token) {
        signal.login("1", token, new Signal.LoginCallback() {
            MessageConsumer<Object> consumer = vertx.eventBus().consumer("subscribe");

            @Override
            public void onLoginSuccess(Signal.LoginSession session, int uid) {
                logger.info("登录成功" +uid);
                consumer.handler(handler ->{
                    JsonObject body = (JsonObject) handler.body();
                    JsonObject msg = body.getJsonObject("message");
                    String account = body.getString("account");
                    session.messageInstantSend(account,msg.toString(),new Signal.MessageCallback(){
                        @Override
                        public void onMessageSendSuccess(Signal.LoginSession session) {
                            logger.info("发送成功"+msg.toString());
                            handler.reply("ok");

                        }
                        @Override
                        public void onMessageSendError(Signal.LoginSession session, int ecode) {
                            logger.info("发送失败"+msg.toString());
                            handler.reply("ok");

                        }
                    });

                });
            }

            @Override
            public void onLoginFailed(Signal.LoginSession session, int ecode) {
                logger.info("登录失败" +ecode);
                consumer.unregister();
                //重新连接
                try {

                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                login(signal,token);
            }

            @Override
            public void onError(Signal.LoginSession session, int ecode, String reason) {
                //重新连接
                consumer.unregister();
                try {

                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                login(signal,token);
            }

            @Override
            public void onLogout(Signal.LoginSession session, int ecode) {
                logger.info("reason22" +ecode);
                consumer.unregister();
                //重新连接
                try {

                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                login(signal,token);

            }
        });
    }


}
