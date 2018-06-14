package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.TIMER_INFO;

public class ModuleAddTimerMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name ModuleAddTimerMsgHandle 增加定时器
	 * @param 	strMsg: 命令字符串 
	 * 					格式：<new_cookie>,ADDTIMER, <username>,<module_id>
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/12/20
	 * **********************************************************************************************************/
	public int call(Runnable runThread, String strMsg)
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e);
		}

		return ServerRetCodeMgr.ERROR_COMMON;
	}
	
	/**********************************************************************************************************
	 * @name ModuleAddTimerMsgHandle 增加定时器
	 * @param 	strMsg: 命令字符串 
	 * 					格式：<new_cookie>,ADDTIMER, <username>,<module_id>
	 *          RESP    格式：<new_cookie>,ADDTIMER, <username>,<module_id>,<retcode>,<原命令字符串>
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/12/20
	 * **********************************************************************************************************/
	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strNewCookie	= strRet[0].trim();
		int iRetCode = Integer.valueOf(strRet[4].trim());
		
		String strAppMsgHeader	= strRet[6].trim();
		String strAppUserName 	= strRet[7].trim();
		String strModuleId	= strRet[8].trim();
		String strTimerId	= strRet[9].trim();
		String strTimerType	= strRet[10].trim();
		String strPeroid	= strRet[11].trim();
		String strTimeOn	= strRet[12].trim();
		String strTimeOff	= strRet[13].trim();
		
		/* 获取用户线程 */
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		
		/* 更新COOKIE */
		ServerWorkThread.RefreshAppCookie(strAppUserName, strNewCookie);
		
		/* 刷新心跳状态 */
		ServerWorkThread.RefreshModuleAliveFlag(strModuleId, true);
		
		ServerWorkThread.RefreshModuleIP(strModuleId, thread.getSrcIP(), thread.getSrcPort());
		
		//获取模块返回的返回码
		if(0 != iRetCode)
		{
			ResponseToAPP(strAppMsgHeader, strAppUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_MODULE_RET_ERROR);
			return ServerRetCodeMgr.ERROR_CODE_MODULE_RET_ERROR;
		}
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
			/* 开启事务机制 */
			dbMgr.BeginTansacion();
			
			//APP已校验过，可以保证相同模块ID下不会有相同的TIMERID，所以此处服务器不再进行校验
			boolean bRet = dbMgr.InsertTimerInfo(new TIMER_INFO(Byte.valueOf(strTimerId),Byte.valueOf(strTimerType),strModuleId,strPeroid,
					strTimeOn,strTimeOff,TIMER_INFO.ENABLE));
			if(!bRet)
			{
				//回滚
				dbMgr.Rollback();
				//关闭事务机制
				dbMgr.EndTansacion();
				
				LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t App(%s) [Database] Fail to add timer of module(%s). ", 
						thread.getSrcIP(),thread.getSrcPort(),strAppUserName,strModuleId));
				
				ResponseToAPP(strAppMsgHeader, strAppUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION);
				return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
			}
			
			//如果命令处理成功
			ResponseToAPP(strAppMsgHeader, strAppUserName, strModuleId, ServerRetCodeMgr.SUCCESS_CODE);
		
			//提交事务
			dbMgr.Commit();
			
			//关闭事务机制
			dbMgr.EndTansacion();	
			
			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t App(%s) [Database] Succeed to add timer of module(%s). ", 
					thread.getSrcIP(),thread.getSrcPort(),strAppUserName,strModuleId));
			
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
