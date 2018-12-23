package tool;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.ArrayList;
import java.util.List;

public class DButil extends AbstractVerticle  {
	
	
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
		handler.handle(Future.succeededFuture(init1(vertx, config)));
	}

/*	public static ResultSet query(String sql ,JsonArray json) {
        JsonObject jsonobject = new JsonObject().put("sql",sql).put("json",json);
        vertx.eventBus().send("getDB",jsonobject,handler->{

        });







    }*/

    @Override
    public void start() throws Exception {
    	DButil.init1(vertx,null);
        vertx.eventBus().consumer("queryWithParams",handler ->{
            vertx.executeBlocking(block ->{
                client.getConnection(res->{
                    JsonObject json = (JsonObject)handler.body();
                    String sql = json.getString("sql");
                    JsonArray jsonarray = json.getJsonArray("json");
                    SQLConnection connection = res.result();
                    connection.queryWithParams(sql,jsonarray,handler1->{
                        ResultSet result = handler1.result();
                        List<JsonObject> rows = result.getRows();
                        JsonArray array = new JsonArray(rows);
                        block.complete(array);
                        connection.close();
                    });
                });
            },res->{
                JsonArray resultSet = (JsonArray)res.result();
                handler.reply(resultSet);
            });
        });
        vertx.eventBus().consumer("querySingleWithParams",handler1 ->{
            vertx.executeBlocking(block ->{
                client.getConnection(res->{
                    JsonObject json = (JsonObject)handler1.body();
                    String sql = json.getString("sql");
                    JsonArray jsonarray = json.getJsonArray("json");
                    SQLConnection connection = res.result();
                    connection.querySingleWithParams(sql,jsonarray,handler->{
                        JsonArray array = handler.result();
                        block.complete(array);
                        connection.close();
                    });
                });
            },res->{
				JsonArray resultSet = (JsonArray)res.result();

                handler1.reply(resultSet);
            });
        });


		vertx.eventBus().consumer("querySingle",handler1 ->{
			vertx.executeBlocking(block ->{
				client.getConnection(res->{
					JsonObject json = (JsonObject)handler1.body();
					String sql = json.getString("sql");
					//JsonArray jsonarray = json.getJsonArray("json");
					SQLConnection connection = res.result();
					connection.querySingle(sql,handler->{
						JsonArray array = handler.result();
						block.complete(array);
						connection.close();
					});
				});
			},res->{
				JsonArray resultSet = (JsonArray)res.result();

				handler1.reply(resultSet);
			});
		});
    }

    public static int init1(Vertx vertx, JsonObject config){
		if (client == null) {
			synchronized (DButil.class) {
				if (client == null) {

					// 读取配置加载JDBC所需配置信息
					JsonObject mySQLClientConfig = new JsonObject()
							//.put("url", "jdbc:mysql://42.159.245.82:7918/tdopm?useUnicode=true&characterEncoding=UTF8")
							//.put("host","42.159.245.82")
							.put("url", "jdbc:mysql://139.219.233.114:3306/tdopm?useUnicode=true&characterEncoding=UTF8&?autoReconnect=true")
							.put("user", "cfg")
							.put("password", "Cfg@123")
                            .put("max_pool_size",200)
                            .put("min_pool_size",20)
							//.put("database", "tdopm")
							//.put("port",7918);
		/*					.put("idleConnectionTestPeriod",60)
							.put("breakAfterAcquireFailure",false)
							.put("testConnectionOnCheckin",false)
							.put("acquireRetryAttempts",10)
							.put("acquireRetryDelay",1000)
*/
							.put("driver_class","com.mysql.jdbc.Driver");

					client = JDBCClient.createShared(vertx,mySQLClientConfig);
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
