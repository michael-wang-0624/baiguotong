package rest;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.log4j.Logger;
import tool.RedisUtil;

public class MarkRest {

    private static final Logger logger = Logger.getLogger(MarkRest.class);
    private Vertx vertx;

    public MarkRest(Vertx vertx) {
        this.vertx = vertx;
    }

    public void updateMark(RoutingContext routeContext) {
        HttpServerRequest request = routeContext.request();
        String mark = request.getParam("mark");
        String uid = request.getParam("uid");
        String friendUid = request.getParam("friendUid");
        String key = "friend_mark_" + uid;
        RedisUtil.redisClient_.hset(key,friendUid,mark,re ->{
            JsonObject ll = new JsonObject();
           if (re.succeeded()) {
               logger.info("设置成功");
               ll.put("statusCode", 200);
               ll.put("body","昵称设置成功");
           } else {
               logger.error("设置昵称失败");
               ll.put("statusCode", 201);
               ll.put("body","昵称设置失败，请联系管理员");
           }

            routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                    .end(Json.encodePrettily(ll));
        });



    }

    public void settingMark(RoutingContext routeContext) {

        HttpServerRequest request = routeContext.request();
        String mark = request.getParam("mark");
        String uid = request.getParam("uid");
        String key = "mark_" + uid;

        RedisUtil.redisClient_.set(key,mark,re ->{
            JsonObject ll = new JsonObject();

            if (re.succeeded()) {
                logger.info("设置成功");
                ll.put("statusCode", 200);
                ll.put("body","昵称设置成功");

            } else {
                logger.error("设置昵称失败");
                ll.put("statusCode", 201);
                ll.put("body","昵称设置失败，请联系管理员");
            }

            routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                    .end(Json.encodePrettily(ll));
        });


    }

    public void getFriendMark(RoutingContext routeContext) {

        HttpServerRequest request = routeContext.request();
         String uid = request.getParam("uid");
        String friendUid = request.getParam("friendUid");
        String key = "mark_" + uid;
        JsonObject json = new JsonObject()
                .put("uid",uid)
                .put("friendUid",friendUid);

        vertx.eventBus().send("getFriendUid",json,re ->{
            if(re.succeeded()) {
                String body = (String )re.result().body();
                JsonObject ll = new JsonObject();
                ll.put("body",body);
                ll.put("statusCode",200);
                routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                        .end(Json.encodePrettily(ll));
            }
        });






    }

    public void  getFriendMark(String uid,String friendUid ){





    }

}
