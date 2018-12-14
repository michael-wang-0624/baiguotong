package tool;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import sun.security.action.PutAllAction;

import java.util.ArrayList;
import java.util.List;

public class DButil{
	
	
	private static SQLClient client = null;

	
	public static SQLClient getJdbcClient() {
		return client;
	}
	
	
	/***
	 * 异步方式初始连接
	 * 
	 * @param vertx
	 * @param config
	 * @return
	 */
	public static void initAsync(Vertx vertx, JsonObject config, Handler<AsyncResult<Integer>> handler) {
		handler.handle(Future.succeededFuture(init(vertx, config)));
	}
 
	public static int init(Vertx vertx, JsonObject config){
		if (client == null) {
			synchronized (DButil.class) {
				if (client == null) {
					// 读取配置加载JDBC所需配置信息
					JsonObject mySQLClientConfig = new JsonObject()
							//.put("url", "jdbc:mysql://47.104.143.47:3306/baiguotong?useUnicode=true&characterEncoding=UTF8")
							.put("host","42.159.245.82")
							.put("username", "td_pay")
							.put("password", "tdqazwsx_pay")
							.put("database", "tdopm")
							.put("maxPoolSize",50)
							.put("port",7918);


					client = MySQLClient.createShared(vertx,mySQLClientConfig,"mysqlPool");

					//client = MySQLClient.createShared(vertx, mySQLClientConfig, "MySQLPool1");
				}
			}
		}
		return 0;
	}

	public static List<Integer> getMinPageAndMaxPageNum(String nowpage, String pagesize) {//1 ,10   2,5

		List<Integer> resList = new ArrayList<Integer>();
		int inowpage = Integer.parseInt(nowpage);
		int ipagesize = Integer.parseInt(pagesize);
		int scol = ipagesize * (inowpage - 1);
		int ecol = ipagesize * inowpage;
		resList.add(scol);
		resList.add(ecol);
		return resList;
	}

}
