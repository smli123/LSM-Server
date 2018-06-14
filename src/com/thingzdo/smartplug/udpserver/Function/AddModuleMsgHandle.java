package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_INFO;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class AddModuleMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name AddModuleHandle 填加新模块
	 * @param 	strMsg: 命令字符串 格式：<cookie>,ADDPLUG,< username>,<devname>,<module_id>,<module_mac>
	 *                                                 返回：<new_cookie>,ADDPLUG, <username>,<module_id>,<code>
	 * @return  boolean 是否填加成功
	 * @author zxluan
	 * @date 2015/03/24
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg)
	{
		String strRet[] = strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strMsgHeader = strRet[1].trim();
		String strUserName 	= strRet[2].trim();
		String strDevName 	= strRet[3].trim();
		String strDevId		= strRet[4].trim();
		String strMac		= strRet[5].trim();

		ServerWorkThread app_thread = (ServerWorkThread)thread_base;
		
		/* 校验参数合法性 */
		int iRet = CheckAppCmdValid(strUserName, strCookie);
		if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			ResponseToAPPWithDefaultCookie(strMsgHeader, strUserName, strDevId, iRet);
			return iRet;
		}

		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
			
			//开启事务机制
			dbMgr.BeginTansacion();
			
			//待填加的模块ID未填加过
			USER_MODULE info = dbMgr.QueryUserModuleByDevId(strDevId);
			if(null != info)
			{
//				ResponseToAPP(strMsgHeader, strUserName, strDevId, ServerRetCodeMgr.ERROR_CODE_MODULE_ID_REGISTERED);
//				return ServerRetCodeMgr.ERROR_CODE_MODULE_ID_REGISTERED;
				
				//删除用户与模块关联关系 user_module
				if(!dbMgr.DeleteUserModule(info.getUserName(), strDevId))
				{
					dbMgr.Rollback();
					//关闭事务机制
					dbMgr.EndTansacion();
					ResponseToAPP(strMsgHeader, strUserName, strDevId, ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION);
					LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t App(%s) [Database] AddModule:Failed to del relation of APP and module(%s). ", 
							app_thread.getSrcIP(),app_thread.getSrcPort(),strUserName,strDevId));
					return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
				}
			}
			
			//将模块信息写入数据库
			//如果不存在该模块,则增加；
			if(null == dbMgr.QueryModuleInfo(strDevId))
			{
				if(!dbMgr.InsertModuleInfo(new MODULE_INFO(strDevId,strDevName,strMac)))
				{
					dbMgr.Rollback();
					dbMgr.EndTansacion();
					ResponseToAPP(strMsgHeader, strUserName, strDevId, ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION);
					return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;		
				}
			}
			
			//将模块与用户关联关系写入数据库
			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("InsertUserModule[AddModuleMsg](user:%s, devid:%s)", strUserName,strDevId));
			
			if(!dbMgr.InsertUserModule(new USER_MODULE(strUserName,strDevId,USER_MODULE.PRIMARY)))
			{
				dbMgr.Rollback();
				dbMgr.EndTansacion();
				ResponseToAPP(strMsgHeader, strUserName, strDevId, ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION);
				return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
			}
			//提交事务
			dbMgr.Commit();
			dbMgr.EndTansacion();
			
			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s)\t App(%s) Succeed to add smart plug(%s,%s). ", 
					app_thread.getSrcIP(),
					strUserName,strDevName,strDevId));
			
			//通知 APP，加模块成功
			ResponseToAPP(strMsgHeader, strUserName, strDevId, ServerRetCodeMgr.SUCCESS_CODE);
			
			//通知APP模块已上线
			if(ServerWorkThread.IsModuleLogin(strDevId)) {
				USER_MODULE user_info = dbMgr.QueryUserModuleByDevId(strDevId);
				if (null != user_info)
				{
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					NotifyToAPP(user_info.getUserName(), strDevId, 
							ServerCommDefine.APP_NOTIFY_ONLINE_MSG_HEADER, 
							ServerRetCodeMgr.SUCCESS_CODE,
							String.valueOf(ServerCommDefine.MODULE_ON_LINE)) ;		
				}
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

	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
}
