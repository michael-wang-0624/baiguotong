package consumer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import org.apache.log4j.Logger;
import rest.MarkRest;
import tool.DButil;
import tool.RedisUtil;

public class MarkConsumer extends AbstractVerticle {
    private static final Logger logger = Logger.getLogger(MarkRest.class);

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("getFriendUid",re ->{
            JsonObject json = (JsonObject)re.body();
            String uid = json.getString("uid");
            String friendUid = json.getString("friendUid");

            String key = "friend_mark_" + uid;
            RedisUtil.redisClient_.hget(key,friendUid, handler ->{
                if (handler.succeeded()) {
                    String result = handler.result();
                    if (result !=null) {
                        //已经为好友设置了昵称。
                        re.reply(result);
                    } else {
                        //获取好友自己设置的昵称
                        String selfKey = "mark_" + friendUid;
                        RedisUtil.redisClient_.get(selfKey,selfHandler  -> {
                            String selfSettingMark = selfHandler.result();
                            if (selfSettingMark !=null) {
                                re.reply(selfSettingMark);
                            } else {
                                //好友没有设置自己的昵称，获取mysql 中默认的用户名
                                String sql = "select TU_ACC from app_user_inf where `UID` = '" + friendUid +"'";
                                DButil.getJdbcClient().getConnection(res -> {
                                    if (res.succeeded()) {
                                        final SQLConnection connection = res.result();
                                        connection.querySingle(sql, re1 -> {
                                                if (re1.succeeded()) {
                                                JsonArray jsonArray = re1.result();
                                                String defaultMark = jsonArray.getString(0);

                                                //加到缓存中
                                                re.reply(defaultMark);
                                                RedisUtil.redisClient_.set(selfKey,defaultMark,re2 -> {
                                                    if (re2.failed()) {
                                                        logger.error("设置失败"+selfKey);
                                                    }
                                                });

                                            }
                                        });
                                    }
                                });



                            }

                        });
                    }
                }
            });
        });

    }
}
