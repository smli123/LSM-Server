package com.thingzdo.smartplug.udpserver.commdef;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.commdefine.ServerPortDefine;
import com.thingzdo.smartplug.udpserver.ServerDefine;
import com.thingzdo.smartplug.udpserver.ServerMainThread;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public interface ICallFunction {
	public int call(Runnable thread_base, String strMsg);
	public int resp(Runnable thread_base, String strMsg);

	public default int call(Runnable thread_base, String strMsg, byte[] strMsgBin)
	{
		return 0;
	}
	
	/**********************************************************************************************************
	 * @name IsCheckCookie 是否需要校验COOKIE
	 * @param 	命令字
	 * @RET 		
	 * @return  boolean 是否有效
	 * @author zxluan
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	public default boolean IsCheckCookie(String strCmd)
	{
		if(strCmd.equalsIgnoreCase(ServerCommDefine.APP_LOGIN_MSG_HEADER)
			|| strCmd.equalsIgnoreCase(ServerCommDefine.APP_REGUSER_MSG_HEADER))
		{
			return false;
		}
		return true;
	}
	
	public default int CheckAppCmdValid(String strUserName, String strCookie)
	{
		/* 校验用户是否登录 */
		if(!ServerWorkThread.IsUserLogin(strUserName))
		{
			return ServerRetCodeMgr.ERROR_CODE_APP_NOT_LOGIN;
		}
		
		/* COOKIE检验 */
		int iRet = ServerWorkThread.CheckCookie(ServerWorkThread.getAppCookie(strUserName), strCookie);
		if ( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			return iRet;
		}

		return ServerRetCodeMgr.SUCCESS_CODE;
	};
	
	public default int CheckModuleCmdValid(String strModuleID, String strCookie)
	{
		/* 校验模块是否登录 */

		/* COOKIE检验 */
		int iRet = ServerWorkThread.CheckCookie(ServerWorkThread.getModuleCookie(strModuleID), strCookie);
		if ( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			return iRet;
		}

		return ServerRetCodeMgr.SUCCESS_CODE;
	};
	
	public default void Response(String strIp, int port, String strRet) throws IOException
	{
		String str_temp = strRet;
		if (ServerDefine.SRV_DEBUG == true) {
			str_temp = str_temp.substring(0, str_temp.lastIndexOf('#'));
			str_temp += ",192.168.0.103#";
		}
		
		byte[] buffer = new byte[1024];
		DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(strIp), port);    
		dataPacket.setData(str_temp.getBytes());  
		ServerMainThread.dataSocket.send(dataPacket);	
	}
	
	public default void Response(String strIp, int port, byte[] info) throws IOException
	{
		byte[] buffer = new byte[1024];
		DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(strIp), port);    
		dataPacket.setData(info);
		ServerMainThread.dataSocket.send(dataPacket);
	}
	
	/**
	 * ResponseToAPP(String strCmd, String strUserName, int ret_code) 
	 * */
	public default void ResponseToAPP(String strCmd, String strUserName, int ret_code) 
	{
		/* 创建新的COOKIE */
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
		String strNewCookie = df.format(new Date());
		
		//存储新COOKIE
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);
		
		String strRet = String.format("%s,%s,%s,%s,%08x#", strNewCookie, strCmd, strUserName, 
				ServerCommDefine.DEFAULT_MODULE_ID, ret_code);
		
		String strIP = ServerWorkThread.GetCurAppIP(strUserName);
		int app_port = ServerWorkThread.GetCurAppPort(strUserName);
		try {
			Response(strIP, app_port, strRet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e, strIP + ":" + app_port);
		}
		
		if(ret_code == ServerRetCodeMgr.SUCCESS_CODE)
		{
			LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
					strIP,
					app_port,
					strRet,
					ServerRetCodeMgr.getRetCodeDescription(ret_code),
					strUserName));
			return;
		}
		
		LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
				strIP,
				app_port,
				strRet,
				ServerRetCodeMgr.getRetCodeDescription(ret_code),
				strUserName));
		return;
	}
	 
	/**
	 * ResponseToAPP(String strCmd, String strUserName, String strModuleId, int error_code)
	 * @throws SQLException 
	 * */
	public default void ResponseToAPP(String strCmd, String strUserName, String strModuleId, int ret_code) 
	{
		/* 创建新的COOKIE */
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
		String strNewCookie = df.format(new Date());
		
		//存储新COOKIE
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);
		
		String strRet = String.format("%s,%s,%s,%s,%08x#", strNewCookie, strCmd, strUserName, strModuleId, ret_code);
		String strIP = ServerWorkThread.GetCurAppIP(strUserName);
		int app_port = ServerWorkThread.GetCurAppPort(strUserName);
		try {
			Response(strIP, app_port, strRet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e, strIP + ":" + app_port);
		}
		
		if(ret_code == ServerRetCodeMgr.SUCCESS_CODE)
		{
			LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
					strIP,
					app_port,
					strRet,
					ServerRetCodeMgr.getRetCodeDescription(ret_code),
					strUserName));
			return;
		}
		
		LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
				strIP,
				app_port,
				strRet,
				ServerRetCodeMgr.getRetCodeDescription(ret_code),
				strUserName));
		return;
	}
	
	/**
	 * void ResponseToAPP(String strCmd, String strUserName, int ret_code, String strContent) 
	 * */
	public default void ResponseToAPP(String strCmd, String strUserName, int ret_code, String strContent) 
	{
		/* 创建新的COOKIE */
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
		String strNewCookie = df.format(new Date());
	
		if(!ServerWorkThread.IsUserLogin(strUserName))
		{
			LogWriter.WriteErrorLog(LogWriter.SEND, String.format("user(%s) is offline.", strUserName));  
			return;
		}
		
		/* 存储新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);
		
		//获取相关管理实体
		String strRet = String.format("%s,%s,%s,%s,%08x,%s#", strNewCookie, strCmd, strUserName, 
				ServerCommDefine.DEFAULT_MODULE_ID, ret_code, strContent);
		String strIP = ServerWorkThread.GetCurAppIP(strUserName);
		int app_port = ServerWorkThread.GetCurAppPort(strUserName);
		try {
			Response(strIP, app_port, strRet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e, strIP + ":" + app_port);
		}
		
		if(ret_code == ServerRetCodeMgr.SUCCESS_CODE)
		{
			LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
					strIP,
					app_port,
					strRet,
					ServerRetCodeMgr.getRetCodeDescription(ret_code),
					strUserName));		
			return;
		}
		LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
				strIP,
				app_port,
				strRet,
				ServerRetCodeMgr.getRetCodeDescription(ret_code),
				strUserName));
		return;
	}
	
	/**
	 * void ResponseToAPP(String strCmd, String strUserName, String strModuleId, int ret_code, String strContent) 
	 * */
	public default void ResponseToAPP(String strCmd, String strUserName, 
			String strModuleId, int ret_code, String strContent) 
	{		
		/* 创建新的COOKIE */
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
		String strNewCookie = df.format(new Date());
		
		/* 存储新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);
		
		String strIP = ServerWorkThread.GetCurAppIP(strUserName);
		int app_port = ServerWorkThread.GetCurAppPort(strUserName);
		//获取相关管理实体
		LogWriter.WriteDebugLog(LogWriter.SEND, String.format("APP username:%s , socket:%s:%d", 
				strUserName, strIP, app_port));
		
		String strRet = String.format("%s,%s,%s,%s,%08x,%s#", strNewCookie, strCmd, strUserName, strModuleId, ret_code, strContent);
		
		try {
			Response(strIP, app_port, strRet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e, strIP + ":" + app_port);
		}
		
		if(ret_code == ServerRetCodeMgr.SUCCESS_CODE)
		{
			LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
					strIP,
					app_port,
					strRet,
					ServerRetCodeMgr.getRetCodeDescription(ret_code),
					strUserName));		
			return;
		}
		LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
				strIP,
				app_port,
				strRet,
				ServerRetCodeMgr.getRetCodeDescription(ret_code),
				strUserName));
		return;
	}
	
	/**
	 * ResponseToAPP(String strCmd, String strUserName, String strModuleId, int error_code)
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * */
	public default void ResponseToAPPWithDefaultCookie(ServerWorkThread thread, String strCmd, String strUserName, String strModuleId, int ret_code) 
	{
		String strRet = String.format("%s,%s,%s,%s,%08x#", ServerCommDefine.DEFAULT_COOKIE, strCmd, strUserName, strModuleId, ret_code);
		String strIP = thread.getSrcIP();
		int app_port = thread.getSrcPort();
		
		try {
			Response(strIP, app_port, strRet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF,e, strIP + ":" + app_port);
		}
		
		if(ret_code == ServerRetCodeMgr.SUCCESS_CODE)
		{
			LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
					strIP,
					app_port,
					strRet,
					ServerRetCodeMgr.getRetCodeDescription(ret_code),
					strUserName));
			return;
		}
		
		LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
				strIP,
				app_port,
				strRet,
				ServerRetCodeMgr.getRetCodeDescription(ret_code),
				strUserName));
		return;
	}
	
	
	/**
	 * ResponseToAPP(String strCmd, String strUserName, String strModuleId, int error_code)
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * */
	public default void ResponseToAPPWithDefaultCookie(String strCmd, String strUserName, String strModuleId, int ret_code) 
	{
		String strRet = String.format("%s,%s,%s,%s,%08x#", ServerCommDefine.DEFAULT_COOKIE, strCmd, strUserName, strModuleId, ret_code);
		String strIP = ServerWorkThread.GetCurAppIP(strUserName);
		int app_port = ServerWorkThread.GetCurAppPort(strUserName);
		try {
			Response(strIP, app_port, strRet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF,e, strIP + ":" + app_port);
		}
		
		if(ret_code == ServerRetCodeMgr.SUCCESS_CODE)
		{
			LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
					strIP,
					app_port,
					strRet,
					ServerRetCodeMgr.getRetCodeDescription(ret_code),
					strUserName));
			return;
		}
		LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
				strIP,
				app_port,
				strRet,
				ServerRetCodeMgr.getRetCodeDescription(ret_code),
				strUserName));
		return;
	}
	
	/**
	 * void ResponseToAPP(String strCmd, String strUserName, int ret_code, String strContent) 
	 * */
	public default boolean NotifyToAPP(String strUserName, String strModuleID, String strCmd, int ret_code, String strContent) 
	{
		/* 创建新的COOKIE */
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
		String strNewCookie = df.format(new Date());
		
		if(!ServerWorkThread.IsUserLogin(strUserName))
		{
			LogWriter.WriteErrorLog(LogWriter.SEND, String.format("user(%s) is offline.", strUserName));  
			return false;
		}
		
		/* 存储新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);
		
		String strCurIP = ServerWorkThread.GetCurAppIP(strUserName);
		int intCurPort = ServerWorkThread.GetCurAppPort(strUserName);
		
		String strRet = String.format("%s,%s,%s,%s,%08x,%s#", strNewCookie, strCmd, strUserName, 
				strModuleID, ret_code, strContent);
		try {
			DatagramPacket dp = new DatagramPacket(strRet.getBytes(), strRet.getBytes().length, 
					InetAddress.getByName(strCurIP), intCurPort); 
			ServerMainThread.dataSocket.send(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(ret_code == ServerRetCodeMgr.SUCCESS_CODE)
		{
			LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] ret_code:%s APP:(%s) ModuleID:(%s)",
					strCurIP,
					intCurPort,
					strRet,
					ServerRetCodeMgr.getRetCodeDescription(ret_code),
					strUserName,
					strModuleID));
			return true;
		}
		LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] ret_code:%s APP:(%s) ModuleID:(%s)",
				strCurIP,
				intCurPort,
				strRet,
				ServerRetCodeMgr.getRetCodeDescription(ret_code),
				strUserName,
				strModuleID));
		return false;
	}
	
	public default int NotifyToModule(String strMsg) throws Exception
	{
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strModuleId	= strRet[3].trim();
		
		if (!ServerWorkThread.IsModuleLogin(strModuleId))
		{
			LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(ModuleID:%s)\t %s", 
					strModuleId, ServerRetCodeMgr.getRetCodeDescription(ServerRetCodeMgr.ERROR_CODE_MODULE_IS_OFFLINE)));
			return ServerRetCodeMgr.ERROR_CODE_MODULE_IS_OFFLINE;
		}
		
		String strCurIP = ServerWorkThread.getModuleIp(strModuleId);
		int intCurPort = ServerWorkThread.getModulePort(strModuleId);
		
		/* 替换COOKIE */
		String strCmd = ServerWorkThread.getModuleCookie(strModuleId);
		for(int i = 1; i < strRet.length; i++)
		{
			strCmd += ",";
			strCmd += strRet[i];
		}
		strCmd += "#";
		
		try
		{
			/* 发送数据报文 */
			DatagramPacket dp = new DatagramPacket(strCmd.getBytes(), strCmd.getBytes().length, 
					InetAddress.getByName(strCurIP), intCurPort);  
			ServerMainThread.dataSocket.send(dp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
					strCurIP,
					intCurPort,
					strCmd,
					"Notify Module Error",
					strModuleId));
			throw e;
		}
		
		LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
				strCurIP,
				intCurPort,
				strCmd,
				"Notify Module Success",
				strModuleId));		
		
		return ServerRetCodeMgr.SUCCESS_CODE;
	}
	
	public default void ResponseToModule(String strModuleID, String strInfo) 
	{
		String strCurIP = ServerWorkThread.getModuleIp(strModuleID);
		int int_port = ServerWorkThread.getModulePort(strModuleID);
		try {
			Response(strCurIP, int_port, strInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SEND, e, "Send Msg To Module Fail." + strCurIP + ":" + int_port + " ModuleID:" + strModuleID + " Info:" + strInfo);
			return;
		}
		LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t\t Send Msg To Module Succeed. [%s].", 
				strCurIP, int_port, strInfo));
	}
	
	public default void ResponseToModule(String strCookie, String strCmd, String strUserName, String strModuleID, int ret_code) 
	{
		//存储新COOKIE
		ServerWorkThread.RefreshModuleCookie(strModuleID, strCookie);
		
		String strRet = String.format("%s,%s,%s,%s,%08x#", strCookie, strCmd, strUserName, strModuleID, ret_code);
		String strCurIP = ServerWorkThread.getModuleIp(strModuleID);
		int int_port = ServerWorkThread.getModulePort(strModuleID);
		try {
			Response(strCurIP, int_port, strRet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e, strCurIP + ":" + int_port);
		}
		
		if(ret_code == ServerRetCodeMgr.SUCCESS_CODE)
		{
			LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
					strCurIP,
					int_port,
					strRet,
					ServerRetCodeMgr.getRetCodeDescription(ret_code),
					strUserName));
			return;
		}
		
		LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] %s (%s)",
				strCurIP,
				int_port,
				strRet,
				ServerRetCodeMgr.getRetCodeDescription(ret_code),
				strUserName));
		return;
	}
	
	public default int ResponseToModule(String strModuleID, byte[] info)
	{
		String strCurIP = ServerWorkThread.getModuleIp(strModuleID);
		int int_port = ServerWorkThread.getModulePort(strModuleID);
		try {
			Response(strCurIP, int_port, info);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e, strCurIP + ":" + int_port);
			return ServerRetCodeMgr.ERROR_COMMON;
		}
		LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t [%s]Reponsed to module.", 
				strCurIP, int_port, String.valueOf(info)));
		
		return ServerRetCodeMgr.SUCCESS_CODE;
	}
}
