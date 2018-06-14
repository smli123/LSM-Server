package com.thingzdo.smartplug.udpserver.Function;

import java.util.Vector;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.PWDTool.PWDManagerTool;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_IRSCENE;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.TIMER_INFO;
import com.thingzdo.smartplug.udpserver.db.USER_INFO;


/*
 * 《红外数据定时任务服务器与模块交付和返回流程》

1. 增加红外遥控定时任务
服务器发送：
<new_cookie>,APPADDSCENE,<username>,<moduleid>,0，开关，模式，风向，风力，温度，时间，周期，空调名字，使能#
模块返回：
<new_cookie>,APPADDSCENE,<username>,<moduleid>,iRetCode,1，开关，模式，风向，风力，温度，时间，周期，空调名字，使能#

2. 删除红外遥控定时任务
服务器发送：
<new_cookie>,APPDELSCENE,<username>,<moduleid>,1，开关，模式，风向，风力，温度，时间，周期，空调名字，使能#
模块返回：
<new_cookie>,APPDELSCENE,<username>,<moduleid>,iRetCode,1，开关，模式，风向，风力，温度，时间，周期，空调名字，使能#

3. 使能红外遥控定时任务
服务器发送：
<new_cookie>,APPENABLESCENE,<username>,<moduleid>,1，开关，模式，风向，风力，温度，时间，周期，空调名字，使能#
模块返回：
<new_cookie>,APPENABLESCENE,<username>,<moduleid>,iRetCode,1，开关，模式，风向，风力，温度，时间，周期，空调名字，使能#

4. 修改红外遥控定时任务
服务器发送：
<new_cookie>,APPMODIFYSCENE,<username>,<moduleid>,1，开关，模式，风向，风力，温度，时间，周期，空调名字，使能#
模块返回：
<new_cookie>,APPMODIFYSCENE,<username>,<moduleid>,iRetCode,1，开关，模式，风向，风力，温度，时间，周期，空调名字，使能#

 * 
 */


public class IRAddSceneMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name APPADDSCENE 修改用户密码
	 * @param 	strMsg: 命令字符串 格式：<cookie>,APPADDSCENE,<username>,
	 * @RET 		<new_cookie>,APPADDSCENE,<username>,<moduleid>,0，开关，模式，风向，风力，温度，时间，周期，空调名字，使能#
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
		int iPower 			= Integer.valueOf(strRet[5].trim());
		int iMode 			= Integer.valueOf(strRet[6].trim());
		int iDir 			= Integer.valueOf(strRet[7].trim());
		int iScale 			= Integer.valueOf(strRet[8].trim());
		int iTemperature 	= Integer.valueOf(strRet[9].trim());
		String strTime 		= strRet[10].trim();
		String strPeriod 	= strRet[11].trim();
		String strIRName 	= strRet[12].trim();
		
		int iEnable 		= Integer.valueOf(strRet[13].trim());
//		int iEnable = 0;
		
		
		/* 校验参数合法性 */
		int iRet = CheckAppCmdValid(strUserName, strCookie);
		if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			ResponseToAPP(strCmd, strUserName, strModuleID, iRet);
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
			return ServerRetCodeMgr.ERROR_COMMON;
		}

		return ServerRetCodeMgr.SUCCESS_CODE;
	}

	/* 
	 * @RET <new_cookie>,APPADDSCENE,<username>,<moduleid>,iRetCode,
	 * 					1，开关，模式，风向，风力，温度，时间，周期，空调名字，使能
	 * */	
	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strNewCookie	= strRet[0].trim();
		int iRetCode 		= Integer.valueOf(strRet[4].trim());
		
		String strAppMsgHeader	= strRet[6].trim();
		String strAppUserName 	= strRet[7].trim();
		String strModuleId		= strRet[8].trim();
		
		int iID 			= 0; 
		int irscene_id		= Integer.valueOf(strRet[9].trim());  //保留，不适用
		int iPower 			= Integer.valueOf(strRet[10].trim());
		int iMode 			= Integer.valueOf(strRet[11].trim());
		int iDir 			= Integer.valueOf(strRet[12].trim());
		int iScale 			= Integer.valueOf(strRet[13].trim());
		int iTemperature 	= Integer.valueOf(strRet[14].trim());
		String strTime 		= strRet[15].trim();
		String strPeriod 	= strRet[16].trim();
		String strIRName 	= strRet[17].trim();
		
		int iEnable 		= Integer.valueOf(strRet[18].trim());
		
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
			MODULE_IRSCENE info = new MODULE_IRSCENE(irscene_id, strModuleId, iPower, iMode, iDir, iScale, iTemperature, strTime, strPeriod, strIRName, iEnable);
			boolean bRet = dbMgr.InsertIRSceneInfo(info);
			if(!bRet)
			{
				//回滚
				dbMgr.Rollback();
				//关闭事务机制
				dbMgr.EndTansacion();
				
				LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t App(%s) [Database] Fail to add IRScene of module(%s). ", 
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
			
			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t App(%s) [Database] Succeed to add IRScene of module(%s). ", 
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
