package tool;

import java.util.ArrayList;

public class Constant {
	//声网
	public static int CURRENT_APPID = 0;
	public static ArrayList <String>app_ids = new ArrayList<String>();
	static {
		app_ids.add("20aec784743645a6a48d0c2fdccc381d");
		//app_ids.add("Your_appId");
	}
	public static String COMMAND_LOGOUT="logout";
	public static String COMMAND_LEAVE_CHART="leave";
	
	//11public static String COMMAND
	public static String COMMAND_TYPE_SINGLE_POINT="2";
	public static String COMMAND_TYPE_CHANNEL="3";
	
	public static String RECORD_FILE_P2P="test_p2p.tmp";
	public static String RECORD_FILE_CHANEEL="test_channel.tmp";
	public static int TIMEOUT=20000;
	
 
	public static String COMMAND_CREATE_SIGNAL="0";
	public static String COMMAND_CREATE_ACCOUNT="1";
	
	public static String COMMAND_SINGLE_SIGNAL_OBJECT = "0";
	public static String COMMAND_MULTI_SIGNAL_OBJECT = "1";
	
	//mysql
	public static final String jdbcUrl = "jdbc:mysql://42.159.245.82:7918/tdopm?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true";
	public static final String username = "td_pay";
	public static final String password = "tdqazwsx_pay";
	
	// rabbitMq
	public static final String rabbit_host = "139.196.136.209";
	public static final String rabbit_username = "admin";
	public static final int rabbit_port = 5672;
	public static final String rabbit_password = "123456";
	
	//redis
	public static final String redis_host = "139.196.136.209";
	public static final int redis_port = 6379;
	public static final String redis_password = "123456";
	
	//是否是本地测试
	public static final boolean isLocale = true;// 本地 true ,服务器 false
	
	
}
