package com.thingzdo.smartplug.udpserver.commdef;

public class ServerCommDefine {

	/**公共消息头*/
	public final static String NOTIFY_POWER_STATUS			= "NOTIFYPOWER";		//推送插座状态
	public final static String NOTIFY_CURTAIN_STATUS		= "NOTIFYCURTAIN";		//推送窗帘状态
	public final static String NOTIFY_KETTLE_STATUS			= "NOTIFYKETTLE";		//推送水壶状态
	public final static String NOTIFY_UPGRADEAP_STATUS		= "NOTIFYUPGRADEAP";	//推送升级进度状态
	
	public final static String NOTIFY_ENERGE				= "NOTIFYENERGY";
	public final static String APP_ENABLE_ENERGE			= "APPENABLEENERGE";
	public final static String ENABLE_ENERGE				= "ENABLEENERGE";
	public final static String APP_QUERY_GONGLV				= "APPQRYGONGLV";
	public final static String QUERY_GONGLV					= "QRYGONGLV";
	public final static String APP_QUERY_CHARGE				= "APPQRYENERGE";
	public final static String QUERY_CHARGE					= "QRYENERGE";

	public final static String NOTIFY_BATTERYENERGE			= "NOTIFYBATTERYENERGY";
	public final static String NOTIFY_BATTERYLOCATION		= "NOTIFYBATTERYLOCATION";
	
	public final static String APP_QUERY_BATTERYENERGE		= "APPQRYBATTERYENERGE";
	public final static String APP_QUERY_BATTERYLOCATON		= "APPQRYBATTERYLOCATON";
	
	public final static String QUERY_NETIP					= "APPQRYNETIP";
	
	/**APP消息头*/
	public final static String APP_REMOTE_PRINT_MSG_HEADER 	= "APPREMOTEPRINT";				//注册
	public final static String APP_REGUSER_MSG_HEADER 		= "APPREG";				//注册
	public final static String APP_MOD_PWD_MSG_HEADER		= "APPMODPWD";				//修改密码
	public final static String APP_MOD_EMAIL_MSG_HEADER		= "APPMODEMAIL";			//修改邮箱
	public final static String APP_RSTPWD_MSG_HEADER		= "APPFINDPWD";			//重置用户密码
	public final static String APP_LOGIN_MSG_HEADER 		= "APPLOGIN";				//
	public final static String APP_LOGOUT_MSG_HEADER		= "APPLOGOUT";
	public final static String APP_ADD_PLUG_MSG_HEADER		= "APPADDPLUG";
	public final static String APP_DEL_PLUG_MSG_HEADER		= "APPDELPLUG";
	public final static String APP_MOD_PLUG_MSG_HEADER		= "APPMODPLUG";
	public final static String APP_QRY_PLUG_MSG_HEADER		= "APPQRYPLUG";
	public final static String APP_NOTIFY_ONLINE_MSG_HEADER	= "APPNOTIFYONLINE";
	public final static String APP_LIGHT_CTRL_MSG_HEADER		= "APPLIGHT";				//小夜灯
	public final static String APP_POWER_CTRL_MSG_HEADER		= "APPPOWER";				//继电器
	public final static String APP_BACK2AP_CTRL_MSG_HEADER		= "APPBACK2AP";				//继电器
	public final static String APP_QRY_IRDATA_MSG_HEADER		= "APPQRYIRDATA";			//空调红外遥控器
	public final static String APP_QRY_TV_IRDATA_MSG_HEADER		= "APPQRYTVIRDATA";			//电视红外遥控器
	
	public final static String APP_CURTAIN_CTRL_MSG_HEADER		= "APPCURTAIN_ACTION";				//窗帘
	public final static String APP_WINDOW_CTRL_MSG_HEADER		= "APPWINDOW_ACTION";				//窗户
	public final static String APP_ALED_CTRL_MSG_HEADER			= "APPALED";				//智能灯
	public final static String APP_AIRCON_CTRL_MSG_HEADER		= "APPAIRCON";				//空调红外遥控器接口1
	public final static String APP_AIRCON_SERVER_CTRL_MSG_HEADER= "APPAIRCONSERVER";//空调红外遥控器接口2
	public final static String APP_TV_SERVER_CTRL_MSG_HEADER	= "APPTVSERVER";//电视红外遥控器接口2
	
	public final static String APP_SET_TIMER_ENABLE_MSG_HEADER	= "APPSETTIMERENABLE";		//设置定时器的使能状
	public final static String APP_ADD_TIMER_MSG_HEADER			= "APPADDTIMER";
	public final static String APP_MOD_TIMER_MSG_HEADER			= "APPMODTIMER";
	public final static String APP_DEL_TIMER_MSG_HEADER			= "APPDELTIMER";
	public final static String APP_LIGHT_RGB_MSG_HEADER			= "APPRGB";
	public final static String APP_USB_CTRL_MSG_HEADER			= "APPUSB";
	public final static String APP_UPGRADE_START_MSG_HEADER		= "APPUPGRADESTART";
	public final static String APP_BELL_ON_MSG_HEADER			= "APPBELLON";
	public final static String APP_TCP_UDP_MSG_HEADER			= "APPTCPUDP";
	public final static String APP_PARENT_CTRL_MSG_HEADER		= "APPPARENTCTRL";
	public final static String APP_PASSTHROUGH_MSG_HEADER		= "APPPASSTHROUGH";			//Server不做处理，透传APP发过来的命令给MOUDLE

	/* APP 红外数据场景 */
	public final static String APP_ADD_SCENE_MSG_HEADER			= "APPADDSCENE";
	public final static String APP_DEL_SCENE_MSG_HEADER			= "APPDELSCENE";
	public final static String APP_MODIFY_SCENE_MSG_HEADER		= "APPMODIFYSCENE";
	public final static String APP_QUERY_SCENE_MSG_HEADER		= "APPQUERYSCENE";
	public final static String APP_ENABLE_SCENE_MSG_HEADER		= "APPENABLESCENE";
	public final static String START_SCENE_MSG_HEADER			= "STARTSCENE";
	
	public final static String ADD_SCENE_MSG_HEADER			= "ADDSCENE";
	public final static String DEL_SCENE_MSG_HEADER			= "DELSCENE";
	public final static String MODIFY_SCENE_MSG_HEADER		= "MODIFYSCENE";
	public final static String QUERY_SCENE_MSG_HEADER		= "QUERYSCENE";
	public final static String ENABLE_SCENE_MSG_HEADER		= "ENABLESCENE";
	
	/**模块消息头*/
	public final static String MODULE_LOGIN_MSG_HEADER								= "LOGIN";
	public final static String MODULE_QUERY_POWER_STATUS							= "QRYPWRSTA";			//APP主动查询继电器状态
	public final static String MODULE_HEART_MSG_HEADER								= "HEART";				//模块心跳包消息
	public final static String MODULE_QUERY_LIGHT_STATUS_MSG_HEADER					= "QRYLGTSTA"; 			//查询小夜灯状态
	public final static String MODULE_ADJUST_TIME_MSG_HEADER						= "ADJTIME";
	public final static String MODULE_UPGRADE_SEND_MSG_HEADER						= "UPGRADESEND";
	public final static String MODULE_UPGRADE_END_MSG_HEADER						= "UPGRADEEND";
	public final static String MODULE_UPGRADE_OVER									= "UPGRADEOVER";
	public final static String MODULE_UPGRADE_RESEND_MSG_HEADER						= "UPGRADERESEND";		//主动重新发送
	public final static String MODULE_UPGRADE_REEND_MSG_HEADER						= "UPGRADEREEND";		//主动重新发送
	public final static String LIGHT_CTRL_MSG_HEADER		= "LIGHT";				//小夜灯
	public final static String POWER_CTRL_MSG_HEADER		= "POWER";				//继电器
	public final static String BACK2AP_CTRL_MSG_HEADER		= "BACK2AP";				//继电器
	
	public final static String CURTAIN_CTRL_MSG_HEADER		= "CURTAIN_ACTION";			//窗帘
	public final static String WINDOW_CTRL_MSG_HEADER		= "WINDOW_ACTION";				//窗户
	public final static String ALED_CTRL_MSG_HEADER			= "ALED";				//智能灯
	public final static String AIRCON_CTRL_MSG_HEADER		= "AIRCON";				//红外遥控器
	
	public final static String SET_TIMER_ENABLE_MSG_HEADER	= "SETTIMERENABLE";		//设置定时器的使能状
	public final static String ADD_TIMER_MSG_HEADER			= "ADDTIMER";
	public final static String MOD_TIMER_MSG_HEADER			= "MODTIMER";
	public final static String DEL_TIMER_MSG_HEADER			= "DELTIMER";
	public final static String MOD_PLUG_MSG_HEADER			= "MODPLUG";
	public final static String LIGHT_RGB_MSG_HEADER			= "RGB";
	public final static String USB_CTRL_MSG_HEADER			= "USB";
	public final static String UPGRADE_START_MSG_HEADER		= "UPGRADESTART";
	public final static String BELL_ON_MSG_HEADER			= "BELLON";
	public final static String TCP_UDP_MSG_HEADER			= "TCPUDP";
	public final static String PARENT_CTRL_MSG_HEADER		= "PARENTCTRL";
	public final static String MOD_MODULE_NAME_MSG_HEADER	= "MODNAME";
	
	/* MODULE 红外数据 */
	public final static String MODULE_ADD_IR_TIMER_MSG_HEADER		= "ADDIRTIMER";
	public final static String MODULE_DEL_IR_TIMER_MSG_HEADER		= "DELIRTIMER";
	public final static String MODULE_ENAMBE_IR_TIMER_MSG_HEADER	= "ENABLEIRTIMER";

	
	/** 无线摄像头传递图片文件  **/
	public final static String MODULE_RECV_FILE_START_MSG_HEADER	= "JPG_START";
	public final static String MODULE_RECV_FILE_SEND_MSG_HEADER		= "JPG_SEND";
	public final static String MODULE_RECV_FILE_END_MSG_HEADER		= "JPG_END";
	public final static String MODULE_RECV_FILE_OVER_MSG_HEADER		= "JPG_OVER";

	/** 模块日志文件  **/
	public final static String MODULE_LOG_FILE_START_MSG_HEADER		= "MODULELOG";
	
	/** 转发器功能  **/
	public final static String TRANSMIT_HEARBEAT_MSG_HEADER		= "TRANSMIT_HEARBEAT";
	public final static String TRANSMIT_TRANS_MSG_HEADER		= "TRANSMIT_TRANS";
	
	/**模块状态标识*/
	public final static int MODULE_OFF_LINE		= 0;	//模块离线状态
	public final static int MODULE_ON_LINE 		= 1;	//模块在线状态
	
	/**模块继电器通断*/
	public final static int MODULE_STATUS_POWER_OFF	= 0;	//继电器断电
	public final static int MODULE_STATUS_POWER_ON		= 1;	//继电器通电
	
	/**命令分隔符*/
	public final static String CMD_SPLIT_STRING			= "[,#]";
	/**默认模块ID*/
	public final static String DEFAULT_MODULE_ID 	= "0";
	/**默认COOKIE*/
	public final static String DEFAULT_COOKIE		= "0";
}
