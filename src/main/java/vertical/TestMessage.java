package vertical;

import io.agora.signal.Signal;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;
import tool.SignalingToken;

public class TestMessage {
    private static final Logger logger = Logger.getLogger(SignalVertical.class);

    public static void main(String[] args) throws Exception{

        Signal signal = new Signal("20aec784743645a6a48d0c2fdccc381d");
        String token = SignalingToken.getToken(  "20171221000136");
        signal.login("20171221000136", token, new Signal.LoginCallback() {
            @Override
            public void onLoginSuccess(Signal.LoginSession session, int uid) {
                logger.info("登录成功" +uid);
            }

            @Override
            public void onLogout(Signal.LoginSession session, int ecode) {

                logger.info("shut down guale ");
            }

            @Override
            public void onLoginFailed(Signal.LoginSession session, int ecode) {
                logger.info("登录失败" +ecode);
            }


            @Override
            public void onMessageInstantReceive(Signal.LoginSession session, String account, int uid, String msg) {

                logger.info("account:" + msg );
            }

        });



    }


}
