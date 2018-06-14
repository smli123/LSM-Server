package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.PWDTool.PWDManagerTool;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_INFO;
import com.thingzdo.smartplug.udpserver.db.MODULE_IRSCENE;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_INFO;

public class IRDelSceneMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name UpdatePWDHandle 修改用户密码
	 * @param 	strMsg: 命令字符串 格式：<cookie>,MODPWD,<username>,<oldpwd>,<newpwd>
	 * @RET 		<new_cookie>,MODPWD,<username>,<0>,<code>
	 *                  其中return code: 0表示成功，其它：错误码
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg) 
	{
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strCmd		= strRet[1].trim();
		String strUserName	= strRet[2].trim();
		String strModuleID 	= strRet[3].trim();
		
		int irscene_id		= Integer.valueOf(strRet[9].trim());
//		int iPower 			= Integer.valueOf(strRet[5].trim());
//		int iMode 			= Integer.valueOf(strRet[6].trim());
//		int iDir 			= Integer.valueOf(strRet[7].trim());
//		int iScale 			= Integer.valueOf(strRet[8].trim());
//		int iTemperature 	= Integer.valueOf(strRet[9].trim());
//		String strTime 		= strRet[10].trim();
//		String strPeriod 	= strRet[11].trim();
//		String strIRName 	= strRet[12].trim();
		
//		int iEnable 		= Integer.valueOf(strRet[13].trim());
//		int iEnable = 0;
		
		/* 校验参数合法性 */
		int iRet = CheckAppCmdValid(strUserName, strCookie);
		if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			ResponseToAPP(strCmd, strUserName, ServerCommDefine.DEFAULT_MODULE_ID, iRet);
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

	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strNewCookie	= strRet[0].trim();
		int iRetCode = Integer.valueOf(strRet[4].trim());
		
		String strMsgHeader		= strRet[6].trim();
		String strUserName 		= strRet[7].trim();
		String strModuleId		= strRet[8].trim();
		int	 iID				= Integer.valueOf(strRet[9].trim());
		
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
			if(!dbMgr.DeleteIRSceneInfo(iID, strModuleId))
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
