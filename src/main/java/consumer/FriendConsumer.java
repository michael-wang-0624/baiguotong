package consumer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import model.BusMessage;
import model.DataReqRepMessage;
import org.apache.log4j.Logger;
import tool.DButil;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.util.List;


public class FriendConsumer extends AbstractVerticle {

    private static final Logger logger = Logger.getLogger(FriendConsumer.class);


    @Override
    public void start() throws Exception {

        vertx.eventBus().consumer("searchFriend", handler -> {
            JsonObject json = (JsonObject)handler.body();
            String phoneNumber = json.getString("phoneNumber");
            //String name = json.getString("name");
            String sql = "select UID,TU_ACC,TU_SEX ,TU_SYSTEM_LAN,TU_ADDR from app_user_inf where TU_MOBILE= '"+ phoneNumber +"' AND TU_STATUS ='1' ";
            DButil.getJdbcClient().getConnection(res ->{
               if(res.succeeded()) {
                   final SQLConnection connection = res.result();
                    connection.querySingle(sql, re -> {
                        if(re.succeeded()) {
                            DataReqRepMessage message = new DataReqRepMessage();
                            JsonArray result = re.result();
                            String uid = result.getString(0);
                            String username = result.getString(1);
                            String sex  = result.getString(2);
                            String sysLanguge = result.getString(3);
                            String addr = result.getString(4);
                            JsonObject resultBody = new JsonObject().put("uid",uid)
                                    .put("username",username)
                                    .put("sex",sex)
                                    .put("language",sysLanguge)
                                    .put("addr",addr);


                            BusMessage rep = new BusMessage();
                            rep.setCode(200);
                            rep.setObj(resultBody);
                            message.setResRep(rep);

                            handler.reply(message);
                            connection.close();
                        }
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
                            connection.close();
                        }
                    });


                }
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
                                   rep.setObj(new JsonObject().put("body","已经发送好友请求"));
                                   message.setResRep(rep);
                                   handler.reply(message);
                               } else  {
                                   if (request_bool == !is_request) {
                                       String update = "update im_friend set status=1,modify_time="+System.currentTimeMillis()/1000+" where id=  "+id;
                                       connection.update(update,updateHandler ->{
                                           if (updateHandler.succeeded()) {
                                               rep.setCode(200);
                                               rep.setObj(new JsonObject().put("body","已经是好友关系了"));
                                               message.setResRep(rep);
                                               handler.reply(message);
                                               connection.close();

                                           }

                                       });

                                   } else {
                                       rep.setCode(200);
                                       rep.setObj(new JsonObject().put("body","已经发送好友请求"));
                                       message.setResRep(rep);
                                       handler.reply(message);
                                   }
                               }
                           } else {
                                   JsonArray jsonArray = new JsonArray()
                                           .add(userId1)
                                           .add(userId2)
                                           .add(System.currentTimeMillis()/1000L)
                                           .add(System.currentTimeMillis()/1000L)
                                           .add(0)
                                           .add(is_request);
                                   String sql = "insert into im_friend (`user_id1`,`user_id2`,`create_time`,`modify_time`,`status`,`is_request`) " +
                                           "values (?,?,?,?,?,?)";
                                   //String sql = "select UID,TU_ACC,TU_SEX ,TU_SYSTEM_LAN,TU_ADDR from app_user_inf where TU_MOBILE= '"+ phoneNumber +"' AND TU_STATUS ='1' ";

                                   DButil.getJdbcClient().getConnection(insertHandler -> {
                                       if(res.succeeded()) {

                                           connection.updateWithParams(sql,jsonArray,re2 ->{

                                               if(re2.succeeded()){
                                                   rep.setCode(200);
                                               } else  {
                                                   rep.setCode(201);
                                               }
                                               rep.setObj(new JsonObject().put("body","已发送好友请求"));
                                               message.setResRep(rep);
                                               handler.reply(message);
                                               connection.close();
                                           });

                                       }


                                   });
                               }
                           }

                    });

                });









        });
    }
}
