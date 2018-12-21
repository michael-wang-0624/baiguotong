package vertical;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.apache.log4j.Logger;
import tool.DButil;

public class TestMessage extends AbstractVerticle {
    private static final Logger logger = Logger.getLogger(SignalVertical.class);

    public static void main(String[] args) throws Exception{
        Vertx vertx = Vertx.vertx();
        DButil.init(vertx,null);
        vertx.deployVerticle(TestMessage.class.getName());

    }

    @Override
    public void start() throws Exception {

    }
}
