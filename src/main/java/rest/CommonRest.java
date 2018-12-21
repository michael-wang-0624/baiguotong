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
import tool.DButil;
import tool.RedisUtil;
import tool.SignalingToken;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommonRest {

    private static final Logger logger = Logger.getLogger(CommonRest.class);
    private Vertx vertx;

    public CommonRest(Vertx vertx) {
        this.vertx = vertx;
    }

    public void setLanguage(RoutingContext routeContext) {
        HttpServerRequest request = routeContext.request();
        String uid = request.getParam("uid");
        String language = request.getParam("language");
        String key =uid+"_language";
        RedisUtil.redisClient_.set(key,language,re->{
           if(re.succeeded()) {
               logger.info("put "+key+" successful");
               routeContext.response().putHeader("content-type", "application/jintson;charset=UTF-8")
                               .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("body","设置成功")));

           }
        });



    }

    public void saveMessage(RoutingContext routeContext) {

        HttpServerRequest request = routeContext.request();

        String body = request.getParam("body");
        String from = request.getParam("from");
        String to = request.getParam("to");
        String media = request.getParam("media");
        String messageId = request.getParam("messageId");
        String link = request.getParam("link");
        String voiceTime1 = request.getParam("voiceTime");
        Integer voiceTime = Integer.valueOf(voiceTime1!=null ?voiceTime1:"0");

        JsonObject message = new JsonObject()
             .put("body",body==null?"":body )
             .put("from",from)
             .put("to",to)
             .put("message_id",messageId)
             .put("link",link==null?"":link)
             .put("send_time",System.currentTimeMillis())
             .put("media",media)
             .put("voiceTime",voiceTime)   ;
        logger.info(message.toString());

        vertx.eventBus().send("saveMessage",message,re ->{
            if (re.succeeded()) {
                routeContext.response().putHeader("content-type", "application/jintson;charset=UTF-8")
                        .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("body","消息保存成功")));
            }
        });
    }

    public void getToken(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String uid = request.getParam("uid");
        try {
            String token = SignalingToken.getToken(uid);
            DButil.getJdbcClient().getConnection(res->{
                SQLConnection connection = res.result();
                connection.querySingle("select TU_ACC,TU_SEX ,TU_ADDR , TU_MOBILE, TU_BIRTH  from app_user_inf where UID='"+uid+"'",handler->{
                    if(handler.succeeded()){
                        JsonArray result = handler.result();
                        String username = result.getString(0);
                        String addr = result.getString(2);
                        String birth =result.getString(4);

                       RedisUtil.redisClient_.get(uid+"_language",han->{
                           String lan = han.result();
                           JsonObject resultBody = new JsonObject()
                                   .put("username",username)
                                   .put("sex",result.getString(1))
                                   .put("addr",addr==null ?"":addr)
                                   .put("phoneNumber",result.getString(3))
                                   .put("birth",birth==null?"":birth)
                                   .put("token",token)
                                   .put("language",lan==null?"":lan)
                                   ;
                           routingContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                                   .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("body",resultBody)));

                       }) ;
                        }
                    connection.close();
                });

            });




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
        Integer isSelf = Integer.valueOf(request.getParam("isSelf"));
        List<Integer> minPageAndMaxPageNum = DButil.getMinPageAndMaxPageNum(pageNum, pageSize);
        DButil.getJdbcClient().getConnection(res ->{
            SQLConnection connection = res.result();
            JsonArray jsonArray = new JsonArray();
            if (isSelf!=null && isSelf==1) {
                jsonArray.add(from);
                jsonArray.add(to);
            } else {
                jsonArray.add(to);
                jsonArray.add(from);
            }
            jsonArray.add(minPageAndMaxPageNum.get(0));
            jsonArray.add(minPageAndMaxPageNum.get(1));
            connection.queryWithParams("select * from im_message where `from` = ? and`to`= ?  order by send_time desc limit ?,?", jsonArray,re -> {
                if (re.succeeded()) {
                    ResultSet result = re.result();
                    List<JsonObject> rows = result.getRows();

                    Collections.sort(rows,new MessageComparator());
                    JsonArray jsonRows = new JsonArray(rows);

                    JsonArray count = new JsonArray();
                    if (isSelf!=null && isSelf==1) {
                        count.add(from);
                        count.add(to);
                    } else {
                        count.add(to);
                        count.add(from);
                    }

                    DButil.getJdbcClient().getConnection(queryHan->{
                        SQLConnection sqlConnection = queryHan.result();
                        sqlConnection.querySingleWithParams(
                                "select count(*) from im_message where `from` = ? and `to`= ?",count,queryHandler ->{
                                    JsonArray jsonCount = queryHandler.result();
                                    int integer = jsonCount.getInteger(0);
                                    Integer intSize = Integer.valueOf(pageSize);
                                    int total = integer % intSize == 0 ? (integer / intSize) : (integer / intSize) + 1;
                                    sqlConnection.close();

                                    JsonObject rowsObj = new JsonObject().put("total",total).put("rows",jsonRows);


                                    routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                                            .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("body",rowsObj)));


                                });

                    });

                }
                connection.close();

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
        logger.info("talkList");
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
                con.close();
                for (JsonArray result: results) {
                    String from = result.getString(0);
                    String to = result.getString(1);
                    int id = result.getInteger(2);
                    long time = result.getLong(3);
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
                            String target = "";
                            if(from.equals(uid)) {
                                target = to;
                            } else {
                                target = from;
                            }

                            JsonObject bodyJson = new JsonObject()
                                    .put("id",id)
                                    .put("target",target)
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


    private static class MessageComparator implements Comparator<JsonObject> {
        @Override
        public int compare(JsonObject o1, JsonObject o2) {
           // return o1.getChildElement("sendtime", "").getTextTrim().compareTo( o2.getChildElement("sendtime", "").getTextTrim());

            return o1.getInteger("id").compareTo(o2.getInteger("id"));

        }
    }
}
