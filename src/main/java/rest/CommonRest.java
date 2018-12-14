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
import org.apache.log4j.Logger;
import sun.security.action.PutAllAction;
import tool.DButil;
import tool.SignalingToken;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class CommonRest {

    private static final Logger logger = Logger.getLogger(CommonRest.class);
    private Vertx vertx;

    public CommonRest(Vertx vertx) {
        this.vertx = vertx;
    }

    public void setLanguage(RoutingContext routeContext) {



    }

    public void saveMessage(RoutingContext routeContext) {
        HttpServerRequest request = routeContext.request();
        String body = request.getParam("body");
        String from = request.getParam("from");
        String to = request.getParam("to");
        String media = request.getParam("media");
        String messageId = request.getParam("messageId");
        String link = request.getParam("link");

        JsonObject message = new JsonObject()
             .put("body",body)
             .put("from",from)
             .put("to",to)
             .put("message_id",messageId)
             .put("link",link)
             .put("send_time",System.currentTimeMillis()/1000)
             .put("media",media);


        vertx.eventBus().send("saveMessage",message,re ->{
            if (re.succeeded()) {
                routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                        .end(Json.encodePrettily(new JsonObject().put("statusCode",200)));
            }
        });
    }

    public void getToken(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String uid = request.getParam("uid");
        try {
            String token = SignalingToken.getToken("uid");

            routingContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                    .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("token",token)));

        } catch (NoSuchAlgorithmException e ) {
            e.printStackTrace();
        }
    }

    public void getMessages(RoutingContext routeContext) {
        HttpServerRequest request = routeContext.request();
        String pageNum = request.getParam("pageNum");
        String pageSize = request.getParam("pageSize");
        String from = request.getParam("from");
        String to = request.getParam("to");

        String mixId = getMixId(from, to);

        List<Integer> minPageAndMaxPageNum = DButil.getMinPageAndMaxPageNum(pageNum, pageSize);
        DButil.getJdbcClient().getConnection(res ->{
            SQLConnection connection = res.result();
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(mixId);
            jsonArray.add(minPageAndMaxPageNum.get(0));
            jsonArray.add(minPageAndMaxPageNum.get(1));
            connection.queryWithParams("select * from im_message where mix_id = ?  order by send_time desc limit ?,?", jsonArray,re -> {
                if (re.succeeded()) {
                    ResultSet result = re.result();
                    List<JsonObject> rows = result.getRows();
                    JsonArray jsonRows = new JsonArray(rows);
                    logger.info(mixId);
                    connection.querySingle("select count(*) from im_message where mix_id = '"+mixId+"'",queryHandler ->{
                        JsonArray jsonCount = queryHandler.result();
                        Integer integer = jsonCount.getInteger(0);
                        connection.close();
                        JsonObject rowsObj = new JsonObject().put("total",integer).put("rows",jsonRows);


                        routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                                .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("body",rowsObj)));


                    });
                }

            });

        });




    }

    private String getMixId(String from, String to) {
        Long lfrom = Long.valueOf(from);
        Long lto = Long.valueOf(to);

        BigDecimal v1 = new BigDecimal(lfrom).add(new BigDecimal(lto));
        BigDecimal v2 = new BigDecimal(lfrom).add(new BigDecimal(lto + 1))
                .divide(new BigDecimal(2));
        BigDecimal v3 = new BigDecimal(Math.min(lfrom, lto));
        BigDecimal mixId = v1.multiply(v2).add(v3);
        return String.valueOf(mixId);
    }

    public void searchMessages(RoutingContext routingContext) {

    }

    public void talkList(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String uid = request.getParam("uid");
        DButil.getJdbcClient().getConnection(res ->{
            SQLConnection con = res.result();
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(uid);
            jsonArray.add(uid);
            con.queryWithParams("select a.from,a.to ,a.msg_id  , a.modify_time,b.media,b.body,b.link from " +
                    "im_message_last a join im_message  b on a.msg_id = b.id where a.from=? or a.to=? ",jsonArray,re ->{

                ResultSet resultSet = re.result();
                List<JsonArray> results = resultSet.getResults();
                List<Future> futureList = new ArrayList<>();
                List<JsonObject> objectList = new ArrayList<JsonObject>();
                for (JsonArray result: results) {
                    String from = result.getString(0);
                    String to = result.getString(1);
                    int id = result.getInteger(2);
                    int time = result.getInteger(3);
                    String media = result.getString(4);
                    String body = result.getString(5);
                    String link = result.getString(6);


                    JsonObject json = new JsonObject()
                            .put("uid",uid);
                    if (from.equals(uid)) {
                        json.put("friendUid",to);
                    } else {
                        json.put("friendUid",from);
                    }
                    Future future = Future.future();
                    vertx.eventBus().send("getFriendUid",json,re2 ->{
                        if(re.succeeded()) {
                            String nick = (String )re2.result().body();

                            JsonObject bodyJson = new JsonObject()
                                    .put("id",id)
                                    .put("time",time)
                                    .put("media",media)
                                    .put("body",body)
                                    .put("link",link)
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
}
