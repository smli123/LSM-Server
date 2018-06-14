package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.sql.SQLException;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class QueryNetIPHandle implements ICallFunction{
	
	@Override
	public int call(Runnable thread_base, String strMsg) {
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strMsgHeader = strRet[1].trim();
		String strUserName 	= strRet[2].trim();
		String strModuleId 	= strRet[3].trim();

		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("QueryNetIPHandle\t [%s]", strMsg));
		try
		{
//			int iRet = CheckAppCmdValid(strUserName, strCookie);
//			if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
//			{
//				ResponseToAPP(strMsgHeader, strUserName, ServerCommDefine.DEFAULT_MODULE_ID, iRet);
//				return iRet;
//			}
			
			String strCurIP = ServerWorkThread.getModuleIp(strModuleId);
			ResponseToAPP(strMsgHeader, strUserName, ServerRetCodeMgr.SUCCESS_CODE, strCurIP);
			
			return ServerRetCodeMgr.SUCCESS_CODE;
			
		} catch (Exception e) {
			e.printStackTrace();
			return ServerRetCodeMgr.ERROR_COMMON;
		}
	}
	
	/**********************************************************************************************************
	 * @name NotifyPowerStatusHandle 处理模块主动上报继电器状的处理流程
	 * @param 	strMsg: 响应字符串 
	 * 					格式：  cookie, NOTIFYPWRSTA,<username>,< moduleid >，<returncode>, <status>
	 * 								< moduleid >：模块ID
									<status>：0BIT:继电器，1BIT:小夜灯
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg)
	{
		return 0;
	}
}
