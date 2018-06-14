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

public class ModuleDelTimerMsgHandle implements ICallFunction{
	
	/**********************************************************************************************************
	 * @name ModuleDelTimerMsgHandle 删除定时器
	 * @param 	strMsg: 命令字符串 
	 * 					格式：<cookie>,DELTIMER,<username>,<module_id>,<timer_id>
	 * @RET 		<new_cookie>,DELTIMER, <username>,<module_id>,<code>
	 * @return  boolean 是否成功
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws NumberFormatException 
	 * @author zxluan
	 * @throws ClassNotFoundException 
	 * @throws  
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg) 
	{
		String strTimer[] 		= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie		= strTimer[0].trim();
		String strMsgHeader		= strTimer[1].trim();
		String strUserName 		= strTimer[2].trim();
		String strModuleId		= strTimer[3].trim();
		
		/* 校验参数合法性 */
		int iRet = CheckAppCmdValid(strUserName, strCookie);
		if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			ResponseToAPP(strMsgHeader, strUserName, strModuleId, iRet);
			return iRet;
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
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG, e);
		}
		
		return ServerRetCodeMgr.ERROR_COMMON;
	}
	
	/**********************************************************************************************************
	 * @name ModuleDelTimerMsgHandle 删除定时器
	 * @param 	strMsg: 命令字符串 
	 * 					格式：<cookie>,DELTIMER,<username>,<module_id>,<timer_id>
	 * @RET 		<new_cookie>,DELTIMER, <username>,<module_id>,<code>
	 * @return  boolean 是否成功
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws NumberFormatException 
	 * @author zxluan
	 * @throws ClassNotFoundException 
	 * @throws  
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		String strTimer[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strNewCookie	= strTimer[0].trim();
		int iRetCode = Integer.valueOf(strTimer[4].trim());
		
		String strMsgHeader		= strTimer[6].trim();
		String strUserName 		= strTimer[7].trim();
		String strModuleId		= strTimer[8].trim();
		String strTimerId		= strTimer[9].trim();
		
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
			//开启事务机制
			dbMgr.BeginTansacion();
			
			//写数据库 update时实际并不更新定时器的使能状态，所以此处随便填一个值就可以
			if(!dbMgr.DeleteTimerInfo(Byte.valueOf(strTimerId), strModuleId))
			{
				//回滚
				dbMgr.Rollback();
				//结束事务机制
				dbMgr.EndTansacion();
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION);
				return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
			}
			
			//查询模块信息
			MODULE_INFO module_info = dbMgr.QueryModuleInfo(strModuleId);
			if(null == module_info)
			{
				//回滚
				dbMgr.Rollback();
				//结束事务机制
				dbMgr.EndTansacion();
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_MODULE_ID_UNREGISTERED);
				return ServerRetCodeMgr.ERROR_CODE_MODULE_ID_UNREGISTERED;
			}
			
			//给APP回复成功
			ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.SUCCESS_CODE);
			dbMgr.Commit();
			dbMgr.EndTansacion();

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
