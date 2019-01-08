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
import java.util.Iterator;
import java.util.List;

public class CommonRest {

    private static final Logger logger = Logger.getLogger(CommonRest.class);
    private Vertx vertx;

    public CommonRest(Vertx vertx) {
        this.vertx = vertx;
    }

    public void getMediaToken(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String uid = request.getParam("uid");
        String channelName = request.getParam("channelName");
        String mediaToken = SignalingToken.getMediaToken(uid, channelName);
        routingContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("mediaToken",mediaToken)));
    }

    public void setLanguage(RoutingContext routeContext) {
        HttpServerRequest request = routeContext.request();
        String uid = request.getParam("uid");
        String language = request.getParam("language");
        String key =uid+"_language";
        RedisUtil.redisClient_.set(key,language,re->{
           if(re.succeeded()) {
               logger.info("put "+key+" successful");
               routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                               .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("body","设置成功")));

           }
        });
    }

    public void saveMessage(RoutingContext routeContext) {
        logger.info("saveMessage");
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

        if (!from.equals(to)){
            vertx.eventBus().send("saveMessage",message,re ->{
                if (re.succeeded()) {
                    routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                            .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("body","消息保存成功")));
                }
            });
        } else {
            routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                    .end(Json.encodePrettily(new JsonObject().put("statusCode",201).put("body","消息未保存")));

        }


    }

    public void getUserDetail(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String uid = request.getParam("uid");
            DButil.getJdbcClient().getConnection(res->{
                SQLConnection connection = res.result();
                connection.querySingle("select TU_ACC,TU_SEX ,TU_ADDR , TU_MOBILE, TU_BIRTH  from app_user_inf where UID='"+uid+"'",handler->{
                    if(handler.succeeded()){
                        JsonArray result = handler.result();
                        
                        if(result!=null) {
                        	String addr = result.getString(2);
                        	String birth =result.getString(4);
                        	RedisUtil.redisClient_.get(uid+"_language",han->{
                        		String lan = han.result();
                        		RedisUtil.redisClient_.get("mark_"+uid,usernameHan->{
                        			if (usernameHan.succeeded()){
                        				String key = "headImage_"+uid;
                        				RedisUtil.redisClient_.get(key, headHandler->{
                        					String headImage = headHandler.result();
                        					String name = usernameHan.result();
                        					String username = result.getString(0);
                        					if (name!=null) {
                        						username= name;
                        					}
                        					
                        					JsonObject resultBody = new JsonObject()
                        							.put("username",username)
                        							.put("sex",result.getString(1))
                        							.put("addr",addr==null ?"":addr)
                        							.put("phoneNumber",result.getString(3))
                        							.put("birth",birth==null?"":birth)
                        							.put("language",lan==null?"":lan)
                        							.put("headImage", headImage==null?"":headImage)
                        							;
                        					routingContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                        					.end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("body",resultBody)));
                        					
                        				});
                        			}
                        		});
                        		
                        		
                        	}) ;
                        } else {
                        	routingContext.response().putHeader("content-type", "application/json;charset=UTF-8")
        					.end(Json.encodePrettily(new JsonObject().put("statusCode",201).put("body",new JsonObject())));
                        }
                    }
                    connection.close();
                });

            });

    }

    public void getToken(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        String uid = request.getParam("uid");

            String sql ="select TU_ACC,TU_SEX ,TU_ADDR , TU_MOBILE, TU_BIRTH  from app_user_inf where UID='"+uid+"'";
            JsonObject ob = new JsonObject().put("sql", sql);
            vertx.eventBus().send("querySingle",ob,res->{
                JsonArray result =   (JsonArray)res.result().body();
                if (result !=null) {
                    String addr = result.getString(2);
                    String birth =result.getString(4);

                    RedisUtil.redisClient_.get(uid+"_language",han->{
                        String lan = han.result();
                        RedisUtil.redisClient_.get("mark_"+uid,usernameHan->{
                            String username = result.getString(0);
                            if (usernameHan.succeeded()){

                                String name = usernameHan.result();
                                if (name!=null) {
                                    username= name;
                                }
                                String token="";
                                try {
                                    token = SignalingToken.getToken(uid);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
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
                            }
                        });
                    }) ;
                } else {
                    routingContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                            .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("body", new JsonObject())));
                }
            });


            //DButil.getJdbcClient().getConnection(res->{
           /*     SQLConnection connection = res.result();
                connection.querySingle("select TU_ACC,TU_SEX ,TU_ADDR , TU_MOBILE, TU_BIRTH  from app_user_inf where UID='"+uid+"'",handler->{
                    if(handler.succeeded()){
                        JsonArray result = handler.result();

                    } else
                    {
                        logger.error(handler.cause().getMessage());
                    }

                });

            });*/





    }
    
    public void getHistoryMessage(RoutingContext routeContext) {
        logger.info("getHistoryMessage");
        HttpServerRequest request = routeContext.request();
        String pageNum = request.getParam("pageNum");
        String pageSize = request.getParam("pageSize");
        String from = request.getParam("from");
        String to = request.getParam("to");
        //Integer isSelf = Integer.valueOf(request.getParam("isSelf"));
        String mixId = getMixId(from, to);
        List<Integer> minPageAndMaxPageNum = DButil.getMinPageAndMaxPageNum(pageNum, pageSize);
           // DButil.getJdbcClient().getConnection(res ->{
                //SQLConnection connection = res.result();
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(mixId);
                jsonArray.add(minPageAndMaxPageNum.get(0));
                jsonArray.add(Integer.valueOf(pageSize));
                logger.info(jsonArray.toString());

                JsonObject ob = new JsonObject();
                String sql = "select * from im_message where mix_id = ?  order by send_time desc limit ?,?";
                ob.put("sql",sql).put("json",jsonArray);
                vertx.eventBus().send("queryWithParams",ob,re ->{
                    JsonArray result =  (JsonArray)re.result().body();

                    List<JsonObject> rows = result.getList();
                   /* for(JsonObject obj :rows) {
                    	int type ;
                    	String fromObj = obj.getString("from");
                    	if(from.equals(fromObj)) {
                    		type = 1;
                    	}else {
                    		type = 0;
                    	}
                    }*/
                    
                    Collections.sort(rows,new MessageComparator());
                    JsonArray jsonRows = new JsonArray(rows);
                    Iterator<Object> iterator = jsonRows.iterator();
                    while(iterator.hasNext()) {
                    	int type ;
                    	JsonObject row = (JsonObject)iterator.next();
                    	String fromObj = row.getString("from");
                    	if(from.equals(fromObj)) {
                    		type = 1;
                    	}else {
                    		type = 0;
                    	}
                    	row.put("type", type);
                    }
                    JsonArray count = new JsonArray();
                    count.add(mixId);
                    String countSql = "select count(*) from im_message where mix_id = ?";
                    JsonObject ob1 = new JsonObject().put("sql", countSql).put("json", count);

                    vertx.eventBus().send("querySingleWithParams",ob1,re1 ->{
                        JsonArray jsonCount =  (JsonArray)re1.result().body();
                        int integer = jsonCount.getInteger(0);
                        Integer intSize = Integer.valueOf(pageSize);
                        int total = integer % intSize == 0 ? (integer / intSize) : (integer / intSize) + 1;

                        JsonObject rowsObj = new JsonObject().put("total",total).put("rows",jsonRows);

                        routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                                .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("body",rowsObj)));
                    });
             

                });
    }

    public void getMessages(RoutingContext routeContext) {
        logger.info("getMessage");
        HttpServerRequest request = routeContext.request();
        String pageNum = request.getParam("pageNum");
        String pageSize = request.getParam("pageSize");
        String from = request.getParam("from");
        String to = request.getParam("to");
        Integer isSelf = Integer.valueOf(request.getParam("isSelf"));
        List<Integer> minPageAndMaxPageNum = DButil.getMinPageAndMaxPageNum(pageNum, pageSize);
           // DButil.getJdbcClient().getConnection(res ->{
                //SQLConnection connection = res.result();
                JsonArray jsonArray = new JsonArray();
                if (isSelf!=null && isSelf==1) {
                    jsonArray.add(from);
                    jsonArray.add(to);
                } else {
                    jsonArray.add(to);
                    jsonArray.add(from);
                }
                jsonArray.add(minPageAndMaxPageNum.get(0));
                jsonArray.add(Integer.valueOf(pageSize));
                logger.info(jsonArray.toString());

                JsonObject ob = new JsonObject();
                String sql = "select * from im_message where `from` = ? and `to`= ?  order by send_time desc limit ?,?";
                ob.put("sql",sql).put("json",jsonArray);
                vertx.eventBus().send("queryWithParams",ob,re ->{
                    JsonArray result =  (JsonArray)re.result().body();

                    List<JsonObject> rows = result.getList();;

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

                    String countSql = "select count(*) from im_message where `from` = ? and `to`= ?";
                    JsonObject ob1 = new JsonObject().put("sql", countSql).put("json", count);

                    vertx.eventBus().send("querySingleWithParams",ob1,re1 ->{
                        JsonArray jsonCount =  (JsonArray)re1.result().body();
                        int integer = jsonCount.getInteger(0);
                        Integer intSize = Integer.valueOf(pageSize);
                        int total = integer % intSize == 0 ? (integer / intSize) : (integer / intSize) + 1;

                        JsonObject rowsObj = new JsonObject().put("total",total).put("rows",jsonRows);

                        routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                                .end(Json.encodePrettily(new JsonObject().put("statusCode",200).put("body",rowsObj)));


                    });
             

                });




               //});
 



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

    public void talkList(RoutingContext routingContext){
        logger.info("talkList");
        HttpServerRequest request = routingContext.request();
        String uid = request.getParam("uid");
        
        vertx.executeBlocking(futher ->{

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
                            if(re2.succeeded()) {
                                String nick = (String )re2.result().body();

                                int type ;

                                if (uid.equals(from)) {
                                    type = 1;
                                } else {
                                    type = 0;
                                }
                                String target = "";
                                if(from.equals(uid)) {
                                    target = to;
                                } else {
                                    target = from;
                                }
                                String key = "headImage_"+target;
                                RedisUtil.redisClient_.get(key, headHandler->{
                                	
                                     
                                	String headImage = headHandler.result();
                                	JsonObject bodyJson = new JsonObject()
                                            .put("id",id)
                                            .put("target",key.split("_")[1])
                                            .put("time",time)
                                            .put("media",media)
                                            .put("body",body)
                                            .put("link",link)
                                            .put("headImage", headImage==null?"":headImage)
                                            .put("nick",nick).put("type",type);
                                    objectList.add(bodyJson);
                                    future.complete();
                                	
                                });	 

                                
                            }
                        });
                        futureList.add(future);
                    }

                    CompositeFuture.all(futureList).setHandler(rpv->{
                        if(rpv.succeeded()){
                            JsonObject ll = new JsonObject();
                            Collections.sort(objectList,new MessageLastComparator());
                            ll.put("body",objectList);
                            ll.put("statusCode",200);
                            futher.complete();
                            routingContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                                    .end(Json.encodePrettily(ll));
                        }
                    });




                });


            });
        },res->{
        	if(res.failed()) {
        		routingContext.fail(500);
        		logger.info("talk list success");
        	}
        });




    }


    private static class MessageComparator implements Comparator<JsonObject> {
        @Override
        public int compare(JsonObject o1, JsonObject o2) {
           // return o1.getChildElement("sendtime", "").getTextTrim().compareTo( o2.getChildElement("sendtime", "").getTextTrim());

            return o1.getInteger("id").compareTo(o2.getInteger("id"));

        }
    }

    private static class MessageLastComparator implements Comparator<JsonObject> {
        @Override
        public int compare(JsonObject o1, JsonObject o2) {
            return o2.getLong("time").compareTo(o1.getLong("time"));
        }
    }


}
