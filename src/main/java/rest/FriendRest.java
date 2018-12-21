package rest;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import model.BusMessage;
import model.DataReqRepMessage;
import org.apache.log4j.Logger;
import tool.DButil;

import java.util.ArrayList;
import java.util.List;

public class FriendRest {

    private static final Logger logger = Logger.getLogger(FileRest.class);
    private Vertx vertx;

    public FriendRest(Vertx vertx) {
        this.vertx = vertx;
    }

    public void addFriend(RoutingContext routeContext) {
        logger.info("addFriend");
        HttpServerRequest request = routeContext.request();
        String uid = request.getParam("uid");
        String friendUid = request.getParam("friendUid");
        vertx.eventBus().send("addFriend",new JsonObject().put("uid",uid).put("friendUid",friendUid),re ->{
            if(re.succeeded()) {
                routeContext.response().setChunked(true);
                logger.info(re.result().body());
                DataReqRepMessage replyMessage = (DataReqRepMessage)(re.result().body());
                BusMessage m = (BusMessage) replyMessage.getResRep();
                JsonObject ll = (JsonObject) m.getObj();
                Integer id = ll.getInteger("id");
                ll.remove("id");
                ll.put("statusCode", m.getCode());

                routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                            .end(Json.encodePrettily(ll));

            } else {
                logger.error(re.cause());
            }

        });


    }

    public void searchFriend(RoutingContext routeContext) {
        logger.info("searchFriend");
        HttpServerRequest request = routeContext.request();

        String phoneNumber = request.getParam("phoneNumber");
        String uid = request.getParam("uid");
        vertx.eventBus().send("searchFriend",new JsonObject().put("phoneNumber",phoneNumber).put("uid",uid),re ->{
           if(re.succeeded()) {
               routeContext.response().setChunked(true);
               logger.info(re.result().body());
               DataReqRepMessage replyMessage = (DataReqRepMessage)(re.result().body());


               BusMessage m = (BusMessage) replyMessage.getResRep();
               if (m.getCode() == 200) {
                   JsonArray  res = (JsonArray) m.getObj();
                   JsonObject ll = new JsonObject();
                   ll.put("statusCode", m.getCode());
                   ll.put("body",res);

                   routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                           .end(Json.encodePrettily(ll));
               } else {
                   routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                           .end(Json.encodePrettily("{'error':'query failed!'}"));
               }


           }
        });

    }

    public void deleteFriend(RoutingContext routeContext) {
        logger.info("deleteFriend");
        HttpServerRequest request = routeContext.request();
        String uid = request.getParam("uid");
        String friendUid = request.getParam("friendUid");
        vertx.eventBus().send("deleteFriend",new JsonObject().put("uid",uid).put("friendUid",friendUid),re ->{
            if(re.succeeded()) {
                routeContext.response().setChunked(true);
                logger.info(re.result().body());
                DataReqRepMessage replyMessage = (DataReqRepMessage)(re.result().body());
                BusMessage m = (BusMessage) replyMessage.getResRep();
                JsonObject ll = (JsonObject) m.getObj();
                ll.put("statusCode", m.getCode()).put("body","删除好友成功");
                routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                        .end(Json.encodePrettily(ll));


            } else {
                logger.error(re.cause());
            }

        });


    }

    public void getAddFriendList(RoutingContext routeContext) {
        logger.info("getAddFriendList");
        HttpServerRequest request = routeContext.request();
        String uid = request.getParam("uid");
        DButil.getJdbcClient().getConnection(res->{
            SQLConnection connection = res.result();
            connection.query("select * from im_subscribe where `to` = '"+uid+"' and `is_add` <> 1 ",re->{
                ResultSet result = re.result();

                JsonObject ll = new JsonObject();
                if(re.succeeded()) {
                    ll.put("statusCode",200);
                    List<JsonObject> rows = result.getRows();
                    JsonArray output = new JsonArray(rows);
                    ll.put("body",output);
                } else {
                    ll.put("statusCode",201);
                }
                routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                        .end(Json.encodePrettily(ll));
            });
        });

    }

    public void replyRequest(RoutingContext routeContext) {
        logger.info("replyRequest");
        HttpServerRequest request = routeContext.request();
        int type = Integer.valueOf(request.getParam("type"));
        int id = Integer.valueOf(request.getParam("id"));
        JsonObject message = new JsonObject();
        message.put("type",type)
                .put("id",id);

        vertx.eventBus().send("reply:friend",message,re ->{
            JsonObject ll = new JsonObject();
            if(re.succeeded()) {
                int statusCode = (int)re.result().body();
                ll.put("statusCode",statusCode);
                ll.put("body","操作成功");
            } else {
                ll.put("statusCode",201);
                ll.put("body","操作失败");
            }

            routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                    .end(Json.encodePrettily(ll));
        });


    }

    public  void getFriendList(RoutingContext routingContext) {
        logger.info("getFriendList");
        HttpServerRequest request = routingContext.request();
        String uid = request.getParam("uid");
        DButil.getJdbcClient().getConnection(res->{
            SQLConnection connection = res.result();
            JsonArray parm = new JsonArray().add(uid).add(uid);
            connection.queryWithParams(" SELECT user_id1,user_id2  FROM im_friend WHERE user_id1= ? AND `status`=1 UNION ALL" +
                    "   SELECT user_id1,user_id2  FROM im_friend WHERE user_id2= ? AND `status`=1",parm,re ->{
                ResultSet result = re.result();
                connection.close();
                List<JsonArray> results = result.getResults();
                List objectList = new ArrayList<JsonObject>();
                List<Future> futureList = new ArrayList<>();
                for (JsonArray jsonArray:results) {
                    String userId1 = jsonArray.getString(0);
                    String userId2 = jsonArray.getString(1);
                    String friendUid = userId1.equals(uid) ? userId2:userId1;
                    JsonObject json = new JsonObject()
                            .put("uid",uid)
                            .put("friendUid",friendUid);

                    Future future = Future.future();
                    vertx.eventBus().send("getFriendUid",json,re2 ->{
                        if(re.succeeded()) {
                            String nick = (String )re2.result().body();

                            JsonObject bodyJson = new JsonObject()
                                    .put("id",friendUid)
                                    .put("nick",nick);
                            objectList.add(bodyJson);
                            future.complete();
                        }
                    });
                    futureList.add(future);
                }
                CompositeFuture.all(futureList).setHandler(rpv->{
                    if(rpv.succeeded()){
                        JsonObject ll = new JsonObject();
                        ll.put("body",objectList);
                        ll.put("statusCode",200);
                        routingContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                                .end(Json.encodePrettily(ll));

                    }
                });




            });


        });


    }

    public void getFriendDetail(RoutingContext routeContext) {
        logger.info("findFriendDetail");
        HttpServerRequest request = routeContext.request();

        String phoneNumber = request.getParam("uid");
        String uid = request.getParam("id");
        vertx.eventBus().send("findFriendDetail",new JsonObject().put("id",phoneNumber).put("uid",uid),re ->{
            if(re.succeeded()) {
                routeContext.response().setChunked(true);
                DataReqRepMessage replyMessage = (DataReqRepMessage)(re.result().body());
                BusMessage m = (BusMessage) replyMessage.getResRep();
                if (m.getCode() == 200) {
                    JsonObject  res = (JsonObject) m.getObj();
                    JsonObject ll = new JsonObject();
                    ll.put("statusCode", m.getCode());
                    ll.put("body",res);

                    routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                            .end(Json.encodePrettily(ll));
                } else {
                    routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                            .end(Json.encodePrettily("{'error':'query failed!'}"));
                }


            }
        });
    }
}
