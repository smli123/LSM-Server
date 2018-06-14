package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class DeleteModuleMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name DeleteModuleHandle 删除指定模块
	 * @param 	strMsg: 命令字符串 格式：<cookie>,DELPLUG,<username>,<module_id>
	 *                                                 返回：<new_cookie>,DELPLUG, <username>,<module_id>,<code>
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/08
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg)
	{
		String strRet[] 			= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie			= strRet[0].trim();
		String strMsgHeader			= strRet[1].trim();
		String strUserName 			= strRet[2].trim();
		String strModuleId			= strRet[3].trim();
		
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		
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
			//判断用户是否拥有该插座
			USER_MODULE user_module = dbMgr.QueryUserModule(strUserName, strModuleId);
			if(null == user_module)
			{
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_USER_NOT_OWN_MODULE);
				return ServerRetCodeMgr.ERROR_CODE_USER_NOT_OWN_MODULE;
			}
			if(user_module.getCtrlMode() == USER_MODULE.SLAVE)
			{
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_USER_HAS_NO_AUTHORITY);
				return ServerRetCodeMgr.ERROR_CODE_USER_HAS_NO_AUTHORITY;
			}
		
			//开启事务机制
			dbMgr.BeginTansacion();
			
			//删除用户与模块关联关系 user_module
			if(!dbMgr.DeleteUserModule(strUserName, strModuleId))
			{
				dbMgr.Rollback();
				//关闭事务机制
				dbMgr.EndTansacion();
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION);
				LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t App(%s) [Database] Failed to del relation of APP and module(%s). ", 
						thread.getSrcIP(),thread.getSrcPort(),strUserName,strModuleId));
				return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
			}
			
			//删除模板对应的定时器信息 timer_info
			if(!dbMgr.DeleteTimerInfo(strModuleId))
			{
				dbMgr.Rollback();
				//关闭事务机制
				dbMgr.EndTansacion();
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION);
				
				LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t App(%s) [Database] Failed to del timer info(%s). ", 
						thread.getSrcIP(),thread.getSrcPort(),strUserName,strModuleId));
				return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
			}
			//删除模块信息 module_info
			if(!dbMgr.DeleteModuleInfo(strModuleId))
			{
				dbMgr.Rollback();
				//关闭事务机制
				dbMgr.EndTansacion();
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION);
				
				LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t App(%s) [Database] Failed to del module.(%s). ", 
						thread.getSrcIP(),thread.getSrcPort(),strUserName,strModuleId));
				return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
			}
			//提交事务
			dbMgr.Commit();
			
			//关闭事务机制
			dbMgr.EndTansacion();
			
			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t App(%s) [Database] Succeed to delete module(%s). ", 
					thread.getSrcIP(),thread.getSrcPort(),strUserName,strModuleId));
			
			//通知 APP，删车成功
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

	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
}
