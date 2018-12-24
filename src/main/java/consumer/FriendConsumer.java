package consumer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import model.BusMessage;
import model.DataReqRepMessage;
import org.apache.log4j.Logger;
import tool.DButil;
import tool.RedisUtil;



public class FriendConsumer extends AbstractVerticle {

    private static final Logger logger = Logger.getLogger(FriendConsumer.class);


    @Override
    public void start() throws Exception {

        vertx.eventBus().consumer("findFriendDetail",handler->{
            JsonObject json = (JsonObject)handler.body();
            String id = json.getString("id");
            String selfUid = json.getString("uid");
            String sql = "select UID,TU_ACC,TU_SEX ,TU_SYSTEM_LAN,TU_ADDR from app_user_inf where UID= '"+ id +"' AND TU_STATUS ='1' ";
            DButil.getJdbcClient().getConnection(res -> {
                if (res.succeeded()) {
                    final SQLConnection connection = res.result();
                    connection.querySingle(sql, re -> {
                        if (re.succeeded()) {
                            DataReqRepMessage message = new DataReqRepMessage();
                            JsonArray result = re.result();
                            JsonObject resultBody = null;
                            BusMessage rep = new BusMessage();
                            if (result == null) {
                                resultBody = new JsonObject();
                                rep.setCode(200);
                                rep.setObj(resultBody);
                                message.setResRep(rep);

                                handler.reply(message);
                            } else {
                                String uid = result.getString(0);
                                String sex = result.getString(2);
                               // String sysLanguge = result.getString(3);
                                String addr = result.getString(4);
                                JsonObject json1 = new JsonObject()
                                        .put("uid",selfUid)
                                        .put("friendUid",uid);
                                vertx.eventBus().send("getFriendUid",json1,re2 ->{
                                    if(re.succeeded()) {
                                        String nick = (String )re2.result().body();
                                        RedisUtil.redisClient_.get(uid+"_language", han->{
                                            String sysLanguge=han.result();
                                            JsonObject resultBody1 = new JsonObject().put("uid", uid)
                                                    .put("username", nick)
                                                    .put("sex", sex)
                                                    .put("language", sysLanguge == null ? "" : sysLanguge)
                                                    .put("addr", addr == null ? "" : addr);
                                            rep.setCode(200);
                                            rep.setObj(resultBody1);
                                            message.setResRep(rep);

                                            handler.reply(message);
                                        });


                                    }
                                });



                            }

                        }
                        connection.close();
                    });


                }
            });
        });

        vertx.eventBus().consumer("searchFriend", handler -> {
            JsonObject json = (JsonObject)handler.body();
            String phoneNumber = json.getString("phoneNumber");
            String name = json.getString("uid");
            String sql = "select UID,TU_ACC,TU_SEX ,TU_SYSTEM_LAN,TU_ADDR from app_user_inf where TU_MOBILE= '"+ phoneNumber +"' AND TU_STATUS ='1' ";
            DButil.getJdbcClient().getConnection(res ->{
               if(res.succeeded()) {
                   final SQLConnection connection = res.result();
                    connection.querySingle(sql, re -> {
                        if(re.succeeded()) {
                            DataReqRepMessage message = new DataReqRepMessage();
                            JsonArray result = re.result();
                            JsonObject resultBody = null;
                            BusMessage rep = new BusMessage();
                            JsonArray jsonArray = new JsonArray();
                            if (result == null) {
                                resultBody = new JsonObject();
                            } else {
                                String uid = result.getString(0);
                                if (uid.equals(name)){
                                    resultBody = new JsonObject();
                                } else {
                                    String username = result.getString(1);
                                    String sex  = result.getString(2);
                                    String sysLanguge = result.getString(3);
                                    String addr = result.getString(4);
                                    resultBody = new JsonObject().put("uid",uid)
                                            .put("username",username)
                                            .put("sex",sex)
                                            .put("language",sysLanguge==null ?"":sysLanguge)
                                            .put("addr",addr==null ?"":addr);
                                    jsonArray.add(resultBody);
                                }

                            }

                            rep.setCode(200);



                            rep.setObj(jsonArray);
                            message.setResRep(rep);

                            handler.reply(message);
                        }
                        connection.close();
                    });


               }
            });


        });

        vertx.eventBus().consumer("reply:friend",handler->{
            JsonObject json = (JsonObject)handler.body();
            int id = json.getInteger("id");
            DButil.getJdbcClient().getConnection(res ->{
               final SQLConnection connection = res.result();
               if(res.succeeded()) {

                   Integer type = json.getInteger("type");
                   int status ;
                   if (type==0) {
                       status = 1;
                   } else {
                       status =2;

                   }
                   long currentTime = Long.valueOf(String.valueOf(System.currentTimeMillis()));
//统一
                   connection.update("update im_friend set `status` = "+status+" ,`modify_time`= "+currentTime+" where id = "+id,resultHandler ->{
                       if (resultHandler.succeeded()) {
                           handler.reply(200);
                           JsonArray jsonArray = new JsonArray();
                           jsonArray.add(status);
                           jsonArray.add(id);
                           DButil.getJdbcClient().getConnection(upConn ->{
                               SQLConnection sqlConnection = upConn.result();
                               sqlConnection.updateWithParams("update im_subscribe set `is_add`=? where f_id =?",jsonArray,update->{
                                   sqlConnection.close();
                               });
                           });
                       } else {
                           handler.reply(201);
                       }
                       connection.close();

                   });
               }
            });
        });
        vertx.eventBus().consumer("deleteFriend", handler -> {
            JsonObject json = (JsonObject)handler.body();
            String phoneNumber = json.getString("phoneNumber");
            //String name = json.getString("name");

             String uid = json.getString("uid");
            String friendUid = json.getString("friendUid");
            String userId1, userId2;
            final boolean is_request  ;
            if (Long.valueOf(uid) < Long.valueOf(friendUid)) {
                userId1 = uid;
                userId2 = friendUid;
                is_request = true;
            } else {
                userId1 = friendUid;
                is_request = false;
                userId2 = uid;
            }
            String sql = "delete from im_friend where user_id1 =   '" + userId1+"'";
            DButil.getJdbcClient().getConnection(res ->{
                if(res.succeeded()) {
                    final SQLConnection connection = res.result();
                    connection.querySingle(sql, re -> {
                        if(re.succeeded()) {
                            DataReqRepMessage message = new DataReqRepMessage();
                            BusMessage rep = new BusMessage();
                            rep.setCode(200);
                            rep.setObj(new JsonObject().put("body","删除成功"));
                            message.setResRep(rep);

                            handler.reply(message);
                        }
                        connection.close();
                    });


                }
            });

            DButil.getJdbcClient().getConnection(res->{
                SQLConnection connection = res.result();
                String  updateSubsribe = "delete from  im_subscribe   where (`from`=? and  `to`=?) or " +
                        "(`to`=? and `from`=?) ";
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(uid)
                        .add(friendUid)
                        .add(friendUid)
                        .add(uid);

                connection.updateWithParams(updateSubsribe,jsonArray,re->{
                    connection.close();
                });

            });


        });

        vertx.eventBus().consumer("addFriend",handler ->{
            JsonObject json = (JsonObject)handler.body();
            String uid = json.getString("uid");
            String friendUid = json.getString("friendUid");
            String userId1, userId2;

            final boolean is_request  ;
            if (Long.valueOf(uid) < Long.valueOf(friendUid)) {
                userId1 = uid;
                userId2 = friendUid;
                is_request = true;
            } else {
                userId1 = friendUid;
                is_request = false;
                userId2 = uid;
            }
            DataReqRepMessage message = new DataReqRepMessage();
            BusMessage rep = new BusMessage();
            if (!userId1.equals(userId2)) {

                //判断是否存在记录
                String query = "select `id`, `status`,`is_request` from im_friend where  `user_id1` = ? and `user_id2` =? ";

                JsonArray queryAraray = new JsonArray();
                queryAraray.add(userId1).add(userId2);
                // Call some blocking API that takes a significant amount of time to return
                DButil.getJdbcClient().getConnection(res ->{
                    final SQLConnection connection = res.result();
                    connection.querySingleWithParams(query,queryAraray,re-> {
                        if(re.succeeded()) {
                            JsonArray array = re.result();
                            if(array !=null) {
                                int id = array.getInteger(0);
                                int status = array.getInteger(1);
                                int request = array.getInteger(2);
                                boolean request_bool = request == 0 ? false :true;
                                if (status == 1) {
                                    rep.setCode(200);
                                    rep.setObj(new JsonObject().put("body","已经是好友关系了"));
                                    message.setResRep(rep);
                                    handler.reply(message);
                                } else if  (status ==2) {
                                    rep.setCode(200);
                                    rep.setObj(new JsonObject().put("body","已经发送好友请求").put("id",id)
                                    );
                                    sendMsg(friendUid,uid,id);
                                    message.setResRep(rep);
                                    handler.reply(message);
                                } else  {
                                    if (request_bool == !is_request) {
                                        String update = "update im_friend set status=1,modify_time="+System.currentTimeMillis()+" where id=  "+id;
                                        DButil.getJdbcClient().getConnection(updateHan->{
                                            SQLConnection updateCon = updateHan.result();
                                            updateCon.update(update,updateHandler ->{
                                                if (updateHandler.succeeded()) {
                                                    rep.setCode(200);
                                                    rep.setObj(new JsonObject().put("body","已经是好友关系了").put("id",id));
                                                    message.setResRep(rep);
                                                    //sendMsg(friendUid,uid,id);
                                                    handler.reply(message);
                                                }
                                                updateCon.close();
                                            });
                                        });


                                    } else {
                                        rep.setCode(200);
                                        rep.setObj(new JsonObject().put("body","已经发送好友请求").put("id",id));
                                        message.setResRep(rep);
                                        sendMsg(friendUid,uid,id);
                                        handler.reply(message);
                                    }
                                }
                            } else {
                                JsonArray jsonArray = new JsonArray()
                                        .add(userId1)
                                        .add(userId2)
                                        .add(System.currentTimeMillis())
                                        .add(System.currentTimeMillis())
                                        .add(0)
                                        .add(is_request);
                                String sql = "insert into im_friend (`user_id1`,`user_id2`,`create_time`,`modify_time`,`status`,`is_request`) " +
                                        "values (?,?,?,?,?,?)";

                                DButil.getJdbcClient().getConnection(insertHan->{
                                    SQLConnection insertCon = insertHan.result();
                                    insertCon.updateWithParams(sql,jsonArray,re2 ->{

                                        if(re2.succeeded()){
                                            UpdateResult upResult = re2.result();

                                            int id = upResult.getKeys().getInteger(0);
                                            rep.setCode(200);
                                            sendMsg(friendUid,uid,id);
                                            rep.setObj(new JsonObject().put("body","已发送好友请求").put("id",id));
                                        } else  {
                                            rep.setCode(201);
                                        }
                                        insertCon.close();
                                        message.setResRep(rep);
                                        handler.reply(message);
                                    });
                                });

                            }
                        }
                        connection.close();
                    });

                });



            } else {
                rep.setCode(201);
                rep.setObj(new JsonObject().put("body","不能添加自己为好友"));
                message.setResRep(rep);
                handler.reply(message);
            }



        });
    }



    private void sendMsg(String friendUid,String uid,int id){
        JsonObject msg = new JsonObject();
        msg.put("account",friendUid);
        msg.put("message",new JsonObject().put("type","subscribe").put("uid",uid).put("id",id).put("body","请求添加好友"));
        vertx.eventBus().send("subscribe", msg);
        
        String sql = "insert into im_subscribe (`from`,`to`,`time`,`nick`,`f_id`) " +
                "values (?,?,?,?,?) ON DUPLICATE KEY UPDATE time = ? ,f_id=?,is_add=0";
        DButil.getJdbcClient().getConnection(res->{
            SQLConnection connection = res.result();
            connection.querySingle("select TU_ACC from app_user_inf where UID = '"+uid+"'",query ->{
                JsonArray array = query.result();
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(uid);
                jsonArray.add(friendUid);
                jsonArray.add(System.currentTimeMillis());

                String nick;
                if (array!=null){
                     nick = array.getString(0);
                } else {
                    nick = uid;
                }
                jsonArray.add(nick);
                jsonArray.add(id);
                jsonArray.add(System.currentTimeMillis());
                jsonArray.add(id);
                DButil.getJdbcClient().getConnection(updateHan->{
                    SQLConnection updaetCon = updateHan.result();
                    updaetCon.updateWithParams(sql,jsonArray,re->{
                        updaetCon.close();
                    });
                });
                connection.close();
            });
        });
    }
}
