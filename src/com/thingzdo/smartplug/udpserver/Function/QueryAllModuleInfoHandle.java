package com.thingzdo.smartplug.udpserver.Function;

import java.sql.SQLException;
import java.util.Vector;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_INFO;
import com.thingzdo.smartplug.udpserver.db.MODULE_IRSCENE;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.TIMER_INFO;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class QueryAllModuleInfoHandle implements ICallFunction{
	private String GenModuleInfo(MODULE_INFO info, String strDevId)
	{
		if(null == info)
		{
			/* 查不到的模块回默认值 */
			String strModuleInfo = strDevId + ",";
			strModuleInfo += "thingzdo_" + strDevId + ",";
			strModuleInfo += "00:00:00:00:00:00" + ",";	//MAC地址
			strModuleInfo += "20161017A1V1.4" + ",";	// Module Version
			strModuleInfo += "0001_0001" + ",";			// Module Type
			strModuleInfo += 0 + ",";
			strModuleInfo += MODULE_INFO.OFFLINE + ",";
			strModuleInfo += 0 + ",";
			strModuleInfo += 0 + ",";
			strModuleInfo += 0 + ",";
			strModuleInfo += 0 + ",";
			strModuleInfo += 0 + ",";
			strModuleInfo += 0 + ",";
			
			
			return strModuleInfo;
		}
		
		//查询模块在线状态
		String strOnLine 		= String.valueOf(MODULE_INFO.OFFLINE);
		if (ServerWorkThread.IsModuleLogin(strDevId))
		{
			strOnLine 		= String.valueOf(MODULE_INFO.ONLINE);
		}
		
		String strPower			= String.valueOf(info.getPwrStatus());
		
		String strModuleIp	 	= "192.168.1.1";//APP暂时没想好，暂时写死，后续再处理 
		//模块信息
		String strModuleInfo = strDevId + ",";
		strModuleInfo += info.getModuleName() + ",";
		strModuleInfo += info.getMac() + ",";
		strModuleInfo += info.getModuleVer() + ",";
		strModuleInfo += info.getModuleType() + ",";
		strModuleInfo += info.getProType() + ",";
		strModuleInfo += strOnLine + ",";
		strModuleInfo += strPower + ",";
		strModuleInfo += strModuleIp + ",";
		strModuleInfo += info.getMode() + ",";
		strModuleInfo += info.getRed() + ",";
		strModuleInfo += info.getGreen() + ",";
		strModuleInfo += info.getBlue() + ",";
		
		return strModuleInfo;
	}
	/**********************************************************************************************************
	 * @name QueryAllModuleInfoHandle 查询指定用户名下的所有模块信息
	 * @param 	strMsg: 命令字符串 格式：cookie,QRYPLUG,<username>
	 * @RET 		<new cookie>,QRYPLUG, <username>,<0xFFFF>,<code>,
	 *                                   <count>,<ID1>,<devname1>,<mac1>,<protocol type>,<online1>,<poweron1>,<ip1>,<red>,<green>,<blue>,
	 * 																<timer_count>,	<timer_id1>,<timer_enable>,<period1>,<powerontime1>,<powerofftime1>@...@
	 * 																							<timer_id_n>,<timer_enable>,<period_n>,<powerontime_n>,<powerofftime_n>#…#
	 * 											<ID_n>,<mac2>,< devname _n>,<protocol type>,<online_n>,<poweron_n>,<ip_n>,<red>,<green>,<blue>,
	 * 																<timer_count>,<timer_id1>,<timer_enable>,<period1>,<powerontime1>,<powerofftime1>@...@
	 * 																						  <timer_id_n>,<timer_enable>,<period_n>,<powerontime_n>,<powerofftime_n>
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/12
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg) 
	{	
		//取值
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strMsgHeader = strRet[1].trim();
		String strUserName 	= strRet[2].trim();
		
		/* 校验参数合法性 */
		int iRet = CheckAppCmdValid(strUserName, strCookie);
		if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			ResponseToAPP(strMsgHeader, strUserName, ServerCommDefine.DEFAULT_MODULE_ID, iRet);
			return iRet;
		}
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
			//查询用户名下所有模块
			Vector<USER_MODULE> vecUserModule = dbMgr.QueryUserModuleByUserName(strUserName);
			
			//组建返回字符串
			String strModuleInfoList = String.valueOf(vecUserModule.size());
			
			//组合模块信息列表
			for(USER_MODULE user_module:vecUserModule)
			{
				String strDevId = user_module.getModuleId() ;
				MODULE_INFO info = dbMgr.QueryModuleInfo(strDevId);
				
				//模块信息
				String strModuleInfo = GenModuleInfo(info, strDevId);
				
				//组合定时器列表
				Vector<TIMER_INFO> vecTimerInfo = dbMgr.QueryTimerInfoList(strDevId);
				strModuleInfo += StructTimerInfoList(vecTimerInfo);
				
				//红外定时任务
				Vector<MODULE_IRSCENE> vecIRSceneInfo = dbMgr.QueryIRSceneInfoList(strDevId);
				strModuleInfo += StructIRSceneInfoList(vecIRSceneInfo);

				//将该模块信息加入模块信息列表中
				strModuleInfoList += "," + strModuleInfo;
			}
			
			//返回信息给APP
			ResponseToAPP(strMsgHeader, strUserName, ServerRetCodeMgr.SUCCESS_CODE, strModuleInfoList);
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
	 * @name StructTimerInfoList 构造指定模块下的定时器信息列表字符串
	 * @param 	strMsg: 命令字符串 格式：QRYPLUG,<username>
	 * @RET 		<timer_count>,	<timer_id1>,<timer1>,<period1>,<powerontime1>,<powerofftime1>@...@
	 * 												<timer_id_n>,<timer_n>,<period_n>,<powerontime_n>,<powerofftime_n>#…#
	 * 													
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @date 2015/04/12
	 * **********************************************************************************************************/
	private String StructTimerInfoList(Vector<TIMER_INFO> vecTimerInfo) 
	{	
		//构造定时器信息列表
		String strTimerInfoList = String.valueOf(vecTimerInfo.size());
		// TimerList:1,1,0,1,1111111,00:00,00:04

		for(TIMER_INFO info:vecTimerInfo)
		{
			String strTimerInfo = info.getTimerId() + ",";
			strTimerInfo += info.getTimerType() + ",";
			strTimerInfo += info.getEnableFlag() + ",";
			strTimerInfo += info.getPeroid() + ",";
			strTimerInfo += info.getTimeOn() + ",";
			strTimerInfo += info.getTimeOff();
			
			//将该定时器信息加入定时器信息列表中
			strTimerInfoList += "," + strTimerInfo;
		}
		return strTimerInfoList + ",";
	}
	
	private String StructIRSceneInfoList(Vector<MODULE_IRSCENE> vecInfo) 
	{	
		//构造定时器信息列表
		String strInfoList = String.valueOf(vecInfo.size());
		// TimerList:1,1,0,1,1111111,00:00,00:04

		for(MODULE_IRSCENE info:vecInfo)
		{
			String strInfo = info.getIRSceneId() + ",";
			strInfo += info.getPower() + ",";
			strInfo += info.getMode() + ",";
			strInfo += info.getDirection() + ",";
			strInfo += info.getScale() + ",";
			strInfo += info.getTemperature() + ",";
			strInfo += info.getTime() + ",";
			strInfo += info.getPeriod() + ",";
			strInfo += info.getIRName() + ",";
			strInfo += info.getEnable();
			
			//将该定时器信息加入定时器信息列表中
			strInfoList += "," + strInfo;
		}
		return strInfoList;
	}
	
	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
}
