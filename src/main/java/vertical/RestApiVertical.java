package vertical;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.log4j.Logger;
import io.vertx.ext.web.handler.CorsHandler;
import rest.CommonRest;
import rest.FileRest;
import rest.FriendRest;
import rest.MarkRest;

public class RestApiVertical extends AbstractVerticle {


    public static final Logger logger = Logger.getLogger(RestApiVertical.class);

    @Override
    public void start() throws Exception {

        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.DELETE)
                .allowedHeader("X-PINGARUNER").allowedHeader("Content-Type"));

        router.route().handler(BodyHandler.create());

      /*  1、搜索好友
        2、添加好友
        3、删除好友
        4、好友列表
        5、查看好友信息
        6、修改备注
        7、设置发音人性别及语言
        8、语音留言文件上传
        9、语音留言文件下载
        10、聊天记录**/

       // router.route("/register")
        router.route(HttpMethod.POST,"/upload").handler(new FileRest(vertx)::savePhoto);

        router.route(HttpMethod.POST,"/uploadFile").handler(new FileRest(vertx)::uploadFile);
        //添加好友
        router.route(HttpMethod.PUT,"/addFriends").handler(new FriendRest(vertx)::addFriend);

        //好友列表
        router.route(HttpMethod.GET,"/getFriendList").handler(new FriendRest(vertx)::getFriendList);


       //搜索好友
        router.route(HttpMethod.GET,"/searchFriend").handler(new FriendRest(vertx)::searchFriend);

        //删除好友
        router.route(HttpMethod.DELETE,"/deleteFriend").handler(new FriendRest(vertx)::deleteFriend);

        //查看好友
        router.route(HttpMethod.GET,"/getFriendDetail").handler(new FriendRest(vertx)::searchFriend);

        //修改好友备注
        router.route(HttpMethod.POST,"/updateMark").handler(new MarkRest(vertx)::updateMark);

        router.route(HttpMethod.POST,"/settingMark").handler(new MarkRest(vertx)::settingMark);

        //查看好友备注
        router.route(HttpMethod.GET,"/getDetailMark").handler(new MarkRest(vertx)::getFriendMark);

        //设置语言
        router.route(HttpMethod.POST,"/settingCountry").handler(new CommonRest(vertx)::setLanguage);

        //保存会话记录
        router.route(HttpMethod.PUT,"/saveMessage").handler(new CommonRest(vertx)::saveMessage);

        //分页获取聊天记录
        router.route(HttpMethod.GET,"/getMessages").handler(new CommonRest(vertx)::getMessages);

        //根据日期获取聊天记录
        router.route(HttpMethod.GET,"/searchMessages").handler(new CommonRest(vertx)::searchMessages);

        //获取聊天消息列表
        router.route(HttpMethod.GET,"/talkList").handler((new CommonRest(vertx)::talkList));

        httpServer.requestHandler(router::accept).listen(8081);

    }

    @Override
    public void stop() throws Exception {

    }
}
