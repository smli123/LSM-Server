package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.TIMER_INFO;

public class ModuleModTimerMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name UpdateTimerHandle 更新定时器
	 * @param 	strMsg: 命令字符串 
	 * 					格式：<cookie>,MODTIMER,<username>,<module_id>,<timer_id>,<timer_type>,<period>,<powerontime>,<powerofftime>
	 * @RET 		<new cookie>,MODTIMER, <username>,<module_id>,<code>
	 * @return  boolean 是否成功
	 * @author zxluan
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
		
		/* 透传给模块 */
		try {
			/* 待模块返回 */
			iRet = NotifyToModule(strMsg);
			if (ServerRetCodeMgr.SUCCESS_CODE != iRet)
			{
				return iRet;
			}
			
			return ServerRetCodeMgr.SUCCESS_CODE;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e);
		}

		return ServerRetCodeMgr.ERROR_COMMON;	
	}
	
	/**********************************************************************************************************
	 * @name UpdateTimerHandle 更新定时器
	 * @param 	strMsg: 命令字符串 
	 * 					格式：<cookie>,MODTIMER,<username>,<module_id>,<timer_id>,<timer_type>,<period>,<powerontime>,<powerofftime>
	 * @RET 		<new cookie>,MODTIMER, <username>,<module_id>,<code>
	 * @return  boolean 是否成功
	 * @author zxluan
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
		String strTimerId	= strRet[9].trim();
		String strTimerType	= strRet[10].trim();
		String strPeroid	= strRet[11].trim();
		String strTimeOn	= strRet[12].trim();
		String strTimeOff	= strRet[13].trim();
		
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
			boolean bRet = dbMgr.UpdateTimerInfo(new TIMER_INFO(Byte.valueOf(strTimerId),Byte.valueOf(strTimerType),strModuleId,strPeroid,strTimeOn,strTimeOff,TIMER_INFO.ENABLE));
			if(!bRet)
			{
				dbMgr.Rollback();
				dbMgr.EndTansacion();
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION);
				return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
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
