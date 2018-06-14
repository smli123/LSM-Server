package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.sql.SQLException;
import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_INFO;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class ModuleLightRGBMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name LightCtrlHandle 小夜灯开/关控制
	 * @param 	strMsg: 命令字符串 
	 * 					格式：<cookie>,RGB,<username>,<module_id>,<mode>,<red>,<green>,<blue>
	 * @RET 		<new_cookie>,RGB, <username>,<module_id>,<code>
	 * @return  boolean 是否成功
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws NumberFormatException 
	 * @author zxluan
	 * @throws InterruptedException 
	 * @throws ClassNotFoundException 
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg) 
	{
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strMsgHeader	= strRet[1].trim();
		String strUserName 	= strRet[2].trim();
		String strModuleId	= strRet[3].trim();
		/* 校验参数合法性 */
		int iRet = CheckAppCmdValid(strUserName, strCookie);
		if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			ResponseToAPP(strMsgHeader, strUserName, strModuleId, iRet);
			return iRet;
		}
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
			USER_MODULE info = dbMgr.QueryUserModuleByDevId(strModuleId);
			if(null == info)
			{
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_USER_NOT_OWN_MODULE);
				return ServerRetCodeMgr.ERROR_CODE_USER_NOT_OWN_MODULE;
			}
			
			//被修改模块必须是已填加的模块
			MODULE_INFO module_info = dbMgr.QueryModuleInfo(strModuleId);
			if(null == module_info)
			{
				ResponseToAPP(strMsgHeader, info.getUserName(), ServerRetCodeMgr.ERROR_CODE_MODULE_ID_UNREGISTERED);
				return ServerRetCodeMgr.ERROR_CODE_MODULE_ID_UNREGISTERED;			
			}
			
			try {
				/* 待模块返回 */
				iRet = NotifyToModule(strMsg);
				if (ServerRetCodeMgr.SUCCESS_CODE != iRet)
				{
					return iRet;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SELF, e);
				return ServerRetCodeMgr.ERROR_COMMON;
			}
			
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
	
	/**********************************************************************************************************
	 * @name LightCtrlHandle 小夜灯开/关控制
	 * @param 	strMsg: 命令字符串 
	 * 					格式：<cookie>,RGB,<username>,<module_id>,<mode>,<red>,<green>,<blue>
	 * @RET 		<new_cookie>,RGB, <username>,<module_id>,<code>
	 * @return  boolean 是否成功
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws NumberFormatException 
	 * @author zxluan
	 * @throws InterruptedException 
	 * @throws ClassNotFoundException 
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub

		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strNewCookie	= strRet[0].trim();
		int iRetCode = Integer.valueOf(strRet[4].trim());
		
		String strMsgHeader	= strRet[6].trim();
		String strUserName 	= strRet[7].trim();
		String strModuleId	= strRet[8].trim();
		int  iMode			= Integer.valueOf(strRet[9].trim());
		int  iRed			= Integer.valueOf(strRet[10].trim());
		int  iGreen			= Integer.valueOf(strRet[11].trim());
		int  iBlue			= Integer.valueOf(strRet[12].trim());
		
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		
		/* 更新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);
		/* 刷新心跳状态 */
		ServerWorkThread.RefreshModuleAliveFlag(strModuleId, true);
		ServerWorkThread.RefreshModuleIP(strModuleId, thread.getSrcIP(), thread.getSrcPort());
		
		//获取模块返回的返回码
		if(0 != iRetCode)
		{
			ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_MODULE_RET_ERROR);
			return ServerRetCodeMgr.ERROR_CODE_MODULE_RET_ERROR;
		}
		
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
			//更新数据库
			if(!dbMgr.UpdateModuleInfo_RGB(strModuleId, iRed, iGreen, iBlue, iMode))
			{
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_UNDOWN_ERROR);
				return ServerRetCodeMgr.ERROR_CODE_UNDOWN_ERROR;
			}
			
			//给APP回复成功
			ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.SUCCESS_CODE);
			
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
