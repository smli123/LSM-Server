package com.thingzdo.smartplug.udpserver.Function;

import java.sql.SQLException;
import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;

public class AppLogOutMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name LogoutMsgHandle 注销
	 * @param 	strMsg: 命令字符串 格式：<cookie>,LOGOUT,<username>
	 *                                                 返回：<new_cookie>,LOGOUT, <username>,<module id>,< code>
	 * @return  boolean 是否注销成功
	 * @author zxluan
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @date 2015/03/24
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg) 
	{
		String strRet[] = strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strMsgHeader	= strRet[1].trim();
		String strUserName	= strRet[2].trim();
				
		ServerWorkThread app_thread = (ServerWorkThread)thread_base;

		//如果未登录，直接返成功
		ResponseToAPPWithDefaultCookie(app_thread, strMsgHeader,  strUserName, 
				ServerCommDefine.DEFAULT_MODULE_ID,
				ServerRetCodeMgr.SUCCESS_CODE);
		
		LogWriter.WriteTraceLog(LogWriter.SELF, 
				String.format("(%s:%d)\t App(%s) Succeed to Logout. ", 
						app_thread.getSrcIP(),app_thread.getSrcPort(),strUserName));
		
		ServerWorkThread.UnRegisterUserIP(strUserName);
		
		return ServerRetCodeMgr.APP_QUIT;
	}

	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
}
