package tool;



import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
public class RedisUtil {



    public static RedisClient redisClient_ =null;

    public static void initAsyncRedis(Vertx vertx, JsonObject config, Handler<AsyncResult<Integer>> handler) {
        handler.handle(Future.succeededFuture(init(vertx, config)));
    }

    public static RedisClient redisClient(){
        return redisClient_;
    }

    public static int init(Vertx vertx,JsonObject config){
        if (redisClient_==null) {
            synchronized (RedisUtil.class) {
                if (redisClient_==null) {
                    RedisOptions redisConfig = new RedisOptions()
                            //.setHost("120.78.218.117").setAuth("ckg-redis");
                            .setHost("139.196.136.209")
                            .setAuth("123456");
                    redisClient_ = RedisClient.create(vertx, redisConfig);
                }
            }
        }
        return 0;
    }

}
