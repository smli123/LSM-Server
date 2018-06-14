package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerParamConfiger;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_DATA;
import com.thingzdo.smartplug.udpserver.db.MODULE_INFO;
import com.thingzdo.smartplug.udpserver.db.MODULE_IRSCENE;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.TIMER_INFO;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;


public class ModuleLoginHandle implements ICallFunction {
	/**********************************************************************************************************
	 * @name LoginHandle 处理模块登录
	 * @param 	strMsg: 命令字符串 
	 * 					格式：cookie, LOGIN,<username>,<moduleid>,<modluename>,<mac>,<version>,<moduletype>,<protocol type>,
	 *                                    <pwr_status>,<flash_mode>,<red>,<green>,<blue>,
	 *                                    <Timer_cnt>,< timer_id 1>,< timer_type1 >,< period 1>,< on time1 >,< offtime1 >,<enable>,<…>#
	 *                  20150101000101,LOGIN,20160723001310#                  
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @date 2015/04/13
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg)
	{
		String strRet[] 		= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie		= strRet[0].trim();
		String strMsgHeader 	= strRet[1].trim();
		String strAppUserName 	= strRet[2].trim();
		String strDevId			= strRet[3].trim();
		String strModuleName	= strRet[4].trim();
		String strMac			= strRet[5].trim();
		String strModuleVer		= strRet[6].trim();
		String strModuleType	= strRet[7].trim();
		String strProType		= strRet[8].trim();
		String strPwrStatus    	= strRet[9].trim();
		String strMode			= strRet[10].trim();
		String strRed 			= strRet[11].trim();
		String strGreen 		= strRet[12].trim();
		String strBlue 			= strRet[13].trim();
		int iTimerNum		    = Integer.valueOf(strRet[14].trim());
		
		/* 获取DB管理器 */
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		
		// Debug for new ServerDBMgr()
//		LogWriter.WriteDebugLog(LogWriter.SELF, 
//				String.format("(Before ModuleLogin new ServerDBMgr(), strDevId=(%s)", strDevId));
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		
//		LogWriter.WriteDebugLog(LogWriter.SELF, 
//				String.format("(After ModuleLogin new ServerDBMgr(), strDevId=(%s)", strDevId));
	
		try
		{
			/* 模块已登录*/
			if(ServerWorkThread.IsModuleLogin(strDevId))
			{
				/* 模块已登录的情况下，重复登录，取消原来的心跳定时器处理；其 它流程按新登录处理 */
				//ServerWorkThread.getModuleConnectInfo(strDevId).getHeartTimer().cancel();
				thread.StopHeartTimer(strDevId);
			} 
		
			dbMgr.BeginTansacion();

			boolean bRet = false;
			//如果不存在该模块
			if(null == dbMgr.QueryModuleInfo(strDevId))
			{
				//更新模块信息
				MODULE_INFO info = new MODULE_INFO(strDevId,strModuleName,strMac,strModuleVer,strModuleType,
						Integer.valueOf(strProType),
						Integer.valueOf(strPwrStatus),
						Integer.valueOf(strRed),
						Integer.valueOf(strGreen),
						Integer.valueOf(strBlue),
						Integer.valueOf(strMode),
						strCookie,
						"",
						"");
				bRet = dbMgr.InsertModuleInfo(info);
				if(!bRet)
				{
					LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s)\t Failed to InsertModuleInfo:%s", 
							strDevId,strMsgHeader));
					dbMgr.Rollback();
					dbMgr.EndTansacion();
					return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
				}
			}
			
			//此时必定存在该模块
			{
				/* 删除定时器 */
				bRet = dbMgr.DeleteTimerInfo(strDevId);
				if(!bRet)
				{
					LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s)\t Failed to DeleteTimerInfo:%s", 
							strDevId,strMsgHeader));
					dbMgr.Rollback();
					dbMgr.EndTansacion();
					return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
				}
				
				/* 填加定时器信息 */
				int idx = 15;
				int i = 0;
				for(i = 0; i < iTimerNum; i++)
				{
					TIMER_INFO info = new TIMER_INFO(
							Byte.valueOf(strRet[idx++].trim()),		//timer id
						    Byte.valueOf(strRet[idx++].trim()),		//timer type
						    strDevId,
						    strRet[idx++].trim(),	//period
						    strRet[idx++].trim(),	//timer on
						    strRet[idx++].trim(),	//timer off
						    Byte.valueOf(strRet[idx++].trim()) //enable
							);
					bRet = dbMgr.InsertTimerInfo(info);  
					if(!bRet)
					{
						LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s)\t Failed to InsertTimerInfo:%s", 
								strDevId,strMsgHeader));
						dbMgr.Rollback();
						dbMgr.EndTansacion();
						return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
					}
				}
				
				/* 删除红外数据定时器 */
				bRet = dbMgr.DeleteIRSceneInfo(strDevId);
				if(!bRet)
				{
					LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s)\t Failed to DeleteIRSceneInfo:%s", 
							strDevId,strMsgHeader));
					dbMgr.Rollback();
					dbMgr.EndTansacion();
					return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
				}
				
				/* 填加红外数据定时器 */
				if (idx < strRet.length) {
					int iIRSceneNum = Integer.valueOf(strRet[idx++].trim());
					for(i = 0; i < iIRSceneNum; i++)
					{
						MODULE_IRSCENE info = new MODULE_IRSCENE(
								Integer.valueOf(strRet[idx++].trim()),	// irsceneid
								strDevId,								// ModuleID
								Integer.valueOf(strRet[idx++].trim()),	// iPower
								Integer.valueOf(strRet[idx++].trim()),	// iMode
								Integer.valueOf(strRet[idx++].trim()),	// iDir
								Integer.valueOf(strRet[idx++].trim()),	// iScale
								Integer.valueOf(strRet[idx++].trim()),	// iTemperature
								strRet[idx++].trim(),					// strTime
								strRet[idx++].trim(),					// strPeriod
								strRet[idx++].trim(),					// strIRName
								Integer.valueOf(strRet[idx++].trim()));	// iEnable
						
						bRet = dbMgr.InsertIRSceneInfo(info);
						
						if(!bRet)
						{
							LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s)\t Failed to InsertTimerInfo:%s", 
									strDevId,strMsgHeader));
							dbMgr.Rollback();
							dbMgr.EndTansacion();
							return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
						}
					}
				}
				
				
				//更新模块信息
				MODULE_INFO info = new MODULE_INFO(strDevId,strModuleName,strMac,strModuleVer,strModuleType,
						Integer.valueOf(strProType),
						Integer.valueOf(strPwrStatus),
						Integer.valueOf(strRed),
						Integer.valueOf(strGreen),
						Integer.valueOf(strBlue),
						Integer.valueOf(strMode),
						strCookie,
						"",
						"");
				bRet = dbMgr.UpdateModuleInfo(info);
				if(!bRet)
				{
					LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s)\t Failed to UpdateModuleInfo:%s", 
							strDevId,strMsgHeader));
					dbMgr.Rollback();
					dbMgr.EndTansacion();
					return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
				}
				
				// 增加模块上线日志信息 lishimin -- MODULE_DATA
				if (ServerParamConfiger.getRecordModuleData() == true) {
					MODULE_DATA data = dbMgr.QueryModuleDataByModuleId(strDevId);
					if (data == null) {
						data = new MODULE_DATA(strDevId, strModuleName, dbMgr.getCurrentTime(), new Timestamp(0), 0, "");
						bRet = dbMgr.InsertModuleData(data);
						if(!bRet)
						{
							LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s)\t Failed to InsertModuleData:%s", 
									strDevId,""));
							dbMgr.Rollback();
							dbMgr.EndTansacion();
							return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
						}
					}
				}
			}
			
			// Update USRE_MODULE module.
			if (strAppUserName.equalsIgnoreCase("thingzdo") == false && 
					strAppUserName.equalsIgnoreCase("usrname") == false &&
					strAppUserName.equalsIgnoreCase("username") == false) {
				USER_MODULE user_module = dbMgr.QueryUserModuleByDevId(strDevId);
				if (user_module == null) {
					LogWriter.WriteTraceLog(LogWriter.SELF, String.format("InsertUserModule[ModuleLoginMsg_Insert](user:%s, devid:%s)", strAppUserName,strDevId));
					dbMgr.InsertUserModule(new USER_MODULE(strAppUserName,strDevId,USER_MODULE.PRIMARY));
				} else {
					LogWriter.WriteTraceLog(LogWriter.SELF, String.format("InsertUserModule[ModuleLoginMsg_Update](user:%s, devid:%s)", strAppUserName,strDevId));
					dbMgr.UpdateUserModule(new USER_MODULE(strAppUserName,strDevId,USER_MODULE.PRIMARY));
				}
			}
			
			dbMgr.Commit();
			dbMgr.EndTansacion();
			
			//注册模块信息
			ServerWorkThread.RegisterModuleIP(strDevId, strCookie, thread.getSrcIP(), thread.getSrcPort());
			
			//注册模块日志信息
			ServerWorkThread.RegisterModuleLogFileMgr(strDevId);

			//开启心跳包定时器
			thread.StartHeartTimer(strDevId);
			
			/* 刷新心跳状态 */
			ServerWorkThread.RefreshModuleAliveFlag(strDevId, true);
				
			//通知模块登录成功
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
			ResponseToModule(strDevId, String.format("%s,%s,%s#", strCookie, ServerCommDefine.MODULE_LOGIN_MSG_HEADER, df.format(new Date())));
			
			//通知APP模块已上线
			USER_MODULE user_info = dbMgr.QueryUserModuleByDevId(strDevId);
			if (null != user_info)
			{
				NotifyToAPP(user_info.getUserName(), strDevId, 
						ServerCommDefine.APP_NOTIFY_ONLINE_MSG_HEADER, 
						ServerRetCodeMgr.SUCCESS_CODE,
						String.valueOf(ServerCommDefine.MODULE_ON_LINE)) ;		
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
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
}
