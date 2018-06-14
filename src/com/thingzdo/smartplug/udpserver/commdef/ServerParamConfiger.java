package com.thingzdo.smartplug.udpserver.commdef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.commdefine.PlatformCommDefine;

public class ServerParamConfiger {
	public final static String PRODUCT_NAME		= "smartplugudp";	//产品名称
	
	private static int iTimeoutModuleRespose		= 70000;		//APP等待模块响应的超时时间
	private static int iAppReceivedBufferSize		= 128;			//APP缓冲区大小
	private static int iModuleReceivedBufferSize	= 128;			//模块缓冲区大小
	private static int iTimeoutModuleLogin			= 30000;		//模块连接服务器后，服务器等待模块登录的超时时间
	private static int iTimeoutAdjustTime			= 12;			//服务器通知模块校正时间使用的定时器的周期
	private static int iTimeOutHeartBeat			= 55000;		//模块心跳包定时器
	private static int iTimeOutUpgrade				= 10000;		//模块升级定时器
	private static int iModuleRgbFreq				= 1000;			//RGB三色频率
	private static int iMaxConnectNum				= 150;			//数据库最大连接池数目
	private static boolean iRecordModuleData        = true;			// true： 记录模块在线/离线数据； false： 不记录
	private static String strUpgradeBinPath			= "/root/thingzdo/smartplugudp";
	private static String strUpgradeBinFileName		= "";
	private static String[] strUserBinPath = new String[]{"/root/thingzdo/smartplugudp/uerbin0.bin", "/root/thingzdo/smartplugudp/uerbin1.bin", "/root/thingzdo/smartplugudp/uerbin2.bin", "/root/thingzdo/smartplugudp/uerbin3.bin"};
	public static String strIRFileName		= "/root/thingzdo/smartplugudp/IR_AirCon.json";	//红外数据库文件
	public static String strTVIRFileName		= "/root/thingzdo/smartplugudp/IR_TV.json";	//红外数据库文件

	public static String strRecvFilePath	= "/root/thingzdo/smartplugudp/snap";	//接收图片文件目录
	public static String strLogFilePath		= "/root/thingzdo/smartplugudp/modulelog";	//接收模块日志文件目录
	
	
	//参数名称字符串
	private final static String TIMEOUT_MODULE_RESPONSE 			= "TIMEOUT_MODULE_RESPONSE";
	private final static String APP_RECEIVED_BUFFER_SIZE			= "APP_RECEIVED_BUFFER_SIZE";
	private final static String MODULE_RECEIVED_BUFFER_SIZE			= "MODULE_RECEIVED_BUFFER_SIZE";
	private final static String TIMEOUT_MODULE_LOGIN				= "TIMEOUT_MODULE_LOGIN";
	private final static String TIMEOUT_ADJUST_TIME					= "TIMEOUT_ADJUST_TIME";
	private final static String TIMEOUT_HEART_BEAT					= "TIMEOUT_HEART_BEAT";
	private final static String TIMEOUT_UPGRADE						= "TIMEOUT_UPGRADE";
	private final static String MODULE_RGB_FREQ						= "MODULE_RGB_FREQ";
	private final static String MODULE_UPGRADE_PATH					= "UPGRADE_MODULE_FILE_PATH";
	private final static String MODULE_USER_BIN_0					= "MODULE_USER_BIN_0";
	private final static String MODULE_USER_BIN_1					= "MODULE_USER_BIN_1";
	private final static String MODULE_USER_BIN_2					= "MODULE_USER_BIN_2";
	private final static String MODULE_USER_BIN_3					= "MODULE_USER_BIN_3";
	private final static String MAX_CONNECT_NUM						= "MAX_DB_LINK_SIZE";
	private final static String RECV_FILE_PATH						= "RECV_FILE_PATH";
	private final static String RECV_MODULE_LOG_FILE_PATH			= "RECV_MODULE_LOG_FILE_PATH";
	private final static String IR_DATA_FILE_NAME					= "IR_DATA_FILE_NAME";
	private final static String TV_IR_DATA_FILE_NAME				= "TV_IR_DATA_FILE_NAME";
	
	/**********************************************************************************************************
	 * @name Do 执行参数配置操作
	 * @return  boolean 是否成功
	 * @throws IOException 
	 * @description 参数源来自配置文件；配置文件位于\root\thingzdo\产品名称\config.int
	 * 文件格式定义为：#开头表示注释
	 * 								例如 TIMEOUT_MODULE_RESPONSE=7000
	 * @author zxluan
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	public static boolean Do()
	{
		String strFilePath = String.format("%s/%s/config.ini", PlatformCommDefine.BASE_DIRECTORY,PRODUCT_NAME);
		File file = new File(strFilePath);
		//如果文件已存在
		if(file.isFile() && file.exists())
		{
			try {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader buffer_reader = new BufferedReader(read);
				String strLineText = null;
				while( (strLineText = buffer_reader.readLine()) != null)
				{
					strLineText = strLineText.trim();
					if(strLineText.startsWith("#"))
					{
						//#开头表示注释，该行数据丢弃
						continue;
					}
					//将该行数据以"="及"#"分隔，只取前两个，一个为KEY,一个为VALUE
					String strRet[] = strLineText.split("[=#]");
					if(strRet[0].equalsIgnoreCase(APP_RECEIVED_BUFFER_SIZE))
					{
						iAppReceivedBufferSize = Integer.valueOf(strRet[1].trim());
					}
					else if(strRet[0].equalsIgnoreCase(MODULE_RECEIVED_BUFFER_SIZE))
					{
						iModuleReceivedBufferSize = Integer.valueOf(strRet[1].trim());
					}
					else if(strRet[0].equalsIgnoreCase(TIMEOUT_ADJUST_TIME))
					{
						//以小时为单位的，转换为以MS为单位
						iTimeoutAdjustTime = Integer.valueOf(strRet[1].trim()) * 60 * 60 * 1000;
					}
					else if(strRet[0].equalsIgnoreCase(TIMEOUT_MODULE_LOGIN))
					{
						iTimeoutModuleLogin = Integer.valueOf(strRet[1].trim());
					}
					else if(strRet[0].equalsIgnoreCase(TIMEOUT_MODULE_RESPONSE))
					{
						iTimeoutModuleRespose = Integer.valueOf(strRet[1].trim());
					}
					else if(strRet[0].equalsIgnoreCase(TIMEOUT_HEART_BEAT))
					{
						iTimeOutHeartBeat = Integer.valueOf(strRet[1].trim());
					}
					else if(strRet[0].equalsIgnoreCase(TIMEOUT_UPGRADE))
					{
						iTimeOutUpgrade = Integer.valueOf(strRet[1].trim());
					}
					else if(strRet[0].equalsIgnoreCase(MODULE_RGB_FREQ))
					{
						iModuleRgbFreq = Integer.valueOf(strRet[1].trim());
					}
					else if (strRet[0].equalsIgnoreCase(MODULE_UPGRADE_PATH)) {
						strUpgradeBinPath = strRet[1].trim();
					}
					else if(strRet[0].equalsIgnoreCase(MODULE_USER_BIN_0))
					{
						strUserBinPath[0] = strRet[1].trim();
					}
					else if(strRet[0].equalsIgnoreCase(MODULE_USER_BIN_1))
					{
						strUserBinPath[1] = strRet[1].trim();
					}
					else if(strRet[0].equalsIgnoreCase(MODULE_USER_BIN_2))
					{
						strUserBinPath[2] = strRet[1].trim();
					}
					else if(strRet[0].equalsIgnoreCase(MODULE_USER_BIN_3))
					{
						strUserBinPath[3] = strRet[1].trim();
					}
					else if(strRet[0].equalsIgnoreCase(MAX_CONNECT_NUM))
					{
						iMaxConnectNum = Integer.valueOf(strRet[1].trim());
					}
					else if (strRet[0].equalsIgnoreCase(RECV_FILE_PATH))
					{
						strRecvFilePath = strRet[1].trim();
					}
					else if (strRet[0].equalsIgnoreCase(RECV_MODULE_LOG_FILE_PATH))
					{
						strLogFilePath = strRet[1].trim();
					}
					else if (strRet[0].equalsIgnoreCase(IR_DATA_FILE_NAME))
					{
						strIRFileName = strRet[1].trim();
					}
					else if (strRet[0].equalsIgnoreCase(TV_IR_DATA_FILE_NAME))
					{
						strTVIRFileName = strRet[1].trim();
					}
				}
				LogWriter.WriteTraceLog(LogWriter.SRV_SELF_LOG, "Succeed to load server parameter.");
				read.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			LogWriter.WriteTraceLog(LogWriter.SRV_SELF_LOG, "Failed to load server parameter,use default values.");
		}
		
		//打印参数结果
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG,"--------------param list begin------------");
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t iAppReceivedBufferSize=%d byte", iAppReceivedBufferSize));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t iModuleReceivedBufferSize=%d byte", iModuleReceivedBufferSize));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t iTimeoutAdjustTime=%d ms", iTimeoutAdjustTime));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t iTimeoutModuleLogin=%d ms", iTimeoutModuleLogin));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t iTimeoutModuleRespose=%d ms", iTimeoutModuleRespose));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t iTimeOutHeartBeat=%d ms", iTimeOutHeartBeat));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t iTimeOutUpdate=%d ms", iTimeOutUpgrade));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t iModuleRgbFreq=%d", iModuleRgbFreq));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t iMaxConnectNum=%d", iMaxConnectNum));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t strRecvFilePath=%s", strRecvFilePath));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t strLogFilePath=%s", strLogFilePath));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t strIRFileName=%s", strIRFileName));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t strTVIRFileName=%s", strTVIRFileName));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t strUpgradeBinPath=%s", strUpgradeBinPath));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t userbin0=%s", strUserBinPath[0]));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t userbin1=%s", strUserBinPath[1]));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t userbin2=%s", strUserBinPath[2]));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG, String.format("\t userbin3=%s", strUserBinPath[3]));
		LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG,"--------------param list end------------");
		return true;
	}
	public static int getTimeoutModuleRespose() {
		return iTimeoutModuleRespose;
	}
	public static int getiAppReceivedBufferSize() {
		return iAppReceivedBufferSize;
	}
	public static int getiModuleReceivedBufferSize() {
		return iModuleReceivedBufferSize;
	}
	public static int getiTimeoutModuleLogin() {
		return iTimeoutModuleLogin;
	}
	public static int getiTimeoutAdjustTime() {
		return iTimeoutAdjustTime;
	}
	public static void main(String[] args)
	{
			ServerParamConfiger.Do();
	}
	public static int getiTimeOutUpgrade() {
		return iTimeOutUpgrade;
	}
	public static void setiTimeOutUpgrade(int iTimeOutUpgrade) {
		ServerParamConfiger.iTimeOutUpgrade = iTimeOutUpgrade;
	}
	public static int getiTimeOutHeartBeat() {
		return iTimeOutHeartBeat;
	}
	public static void setiTimeOutHeartBeat(int iTimeOutHeartBeat) {
		ServerParamConfiger.iTimeOutHeartBeat = iTimeOutHeartBeat;
	}
	public static int getiModuleRgbFreq() {
		return iModuleRgbFreq;
	}
	public static void setiModuleRgbFreq(int iModuleRgbFreq) {
		ServerParamConfiger.iModuleRgbFreq = iModuleRgbFreq;
	}

	public static String getRecvFilePath() {
		return strRecvFilePath;
	}
	public static void setRecvFilePath(String strRecvFilePath) {
		ServerParamConfiger.strRecvFilePath = strRecvFilePath;
	}

	public static String getLogFilePath() {
		return strLogFilePath;
	}
	public static void setLogFilePath(String strLogFilePath) {
		ServerParamConfiger.strLogFilePath = strLogFilePath;
	}

	public static String getUpgradeBinPath() {
		return strUpgradeBinPath;
	}
	public static void setUpgradeBinPath(String strFilePath) {
		ServerParamConfiger.strUpgradeBinPath = strFilePath;
	}
	
	public static String getUpgradeBinFileName() {
		return strUpgradeBinFileName;
	}
	public static void setUpgradeBinFileName(String strFilePath) {
		ServerParamConfiger.strUpgradeBinFileName = strFilePath;
	}
	
	public static String getUserBinPath(int iUserBinNo)
	{
		if(iUserBinNo > 4)
			return null;
		return strUserBinPath[iUserBinNo];
	}
	public static int getMaxConnectNum() {
		return iMaxConnectNum;
	}
	public static void setMaxConnectNum(int iMaxConnectNum) {
		ServerParamConfiger.iMaxConnectNum = iMaxConnectNum;
	}
	public static boolean getRecordModuleData() {
		return iRecordModuleData;
	}
	public static void setRecordModuleData(boolean iRecordModuleData) {
		ServerParamConfiger.iRecordModuleData = iRecordModuleData;
	}
	
}
