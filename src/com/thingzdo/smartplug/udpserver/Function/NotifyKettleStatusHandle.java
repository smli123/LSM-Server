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

public class NotifyKettleStatusHandle implements ICallFunction{
	
	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**********************************************************************************************************
	 * @name NotifyKettleStatusHandle 处理模块主动上报继电器状的处理流程
	 * @param 	strMsg: 响应字符串 
	 * 					格式：  cookie, NOTIFYKETTLE,<username>,< moduleid >，<returncode>, <status01>, <status02>, <status03>, ..., <status20>
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
		String strRet[] 		= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strNewCookie		= strRet[0].trim();
		String strUserName		= strRet[2].trim();
		String strDevId			= strRet[3].trim();
		String strStatus		= strRet[5].trim();
		
		for (int i = 0; i < strRet.length; i++)
		{
			if (i < 6)
				continue;
			strStatus = strStatus + "," + strRet[i].trim();
		}
		
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		
		/* 校验参数合法性 */
		int iRet = CheckModuleCmdValid(strDevId, strNewCookie);
		if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			//ResponseToModule(strNewCookie, strMsgHeader, strUserName, strDevId, iRet);
			//return iRet;
		}
		
		/* 更新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);
		/* 刷新心跳状态 */
		ServerWorkThread.RefreshModuleAliveFlag(strDevId, true);
		ServerWorkThread.RefreshModuleIP(strDevId, thread.getSrcIP(), thread.getSrcPort());
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
//			//不需要存库
//			boolean bRet = dbMgr.UpdateModuleInfo_PwrStatus(strDevId, Integer.valueOf(strStatus));
//			if(!bRet)
//			{
//				LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s)db operation failed. ", strDevId));
//				return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
//			}
			
			USER_MODULE info = dbMgr.QueryUserModuleByDevId(strDevId);
			
			NotifyToAPP(info.getUserName(),strDevId, ServerCommDefine.NOTIFY_KETTLE_STATUS, 
					ServerRetCodeMgr.SUCCESS_CODE,  strStatus);
			
			//通知模块通知已收到
			ResponseToModule(strDevId, String.format("%s#", ServerCommDefine.NOTIFY_KETTLE_STATUS));
			return ServerRetCodeMgr.SUCCESS_CODE;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ServerRetCodeMgr.ERROR_COMMON;
		}
		finally
		{
			dbMgr.Destroy();
		}
	}
}
