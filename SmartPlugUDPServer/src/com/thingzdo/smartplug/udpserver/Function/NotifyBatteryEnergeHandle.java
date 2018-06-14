package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_BATTERYCHARGE;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class NotifyBatteryEnergeHandle implements ICallFunction{
	
	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**********************************************************************************************************
	 * @name NotifyPowerStatusHandle 处理模块主动上报继电器状的处理流程
	 * @param 	strMsg: 响应字符串 
	 * 					格式：  cookie, NOTIFYPWRSTA,<username>,< moduleid >，<returncode>, <status>
	 * 								< moduleid >：模块ID
									<status>：0BIT:继电器，1BIT:小夜灯
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg)
	{
		String strRet[] 		= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strNewCookie		= strRet[0].trim();
		String strUserName		= strRet[2].trim();
		String strDevId			= strRet[3].trim();
		String strOperDate		= strRet[4].trim();
		String strCharge		= strRet[5].trim();
		
		float fCharge = Float.parseFloat(strCharge);
		String strCharge_After = String.format("%.2f", fCharge);
		
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		
		/* 校验参数合法性 */
		int iRet = CheckModuleCmdValid(strDevId, strNewCookie);
		if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			//ResponseToModule(strNewCookie, strMsgHeader, strUserName, strDevId, iRet);
			//return iRet;
		}
		
		/* 更新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);
		/* 刷新心跳状态 */
		ServerWorkThread.RefreshModuleAliveFlag(strDevId, true);
		ServerWorkThread.RefreshModuleIP(strDevId, thread.getSrcIP(), thread.getSrcPort());
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
			//更新数据库
			// strOperDate = "";
			Vector<MODULE_BATTERYCHARGE> vecCharge = dbMgr.QueryModuleBatteryChargeByModuleId(strDevId);
			if (vecCharge.size() == 0) {
				boolean bRet = dbMgr.InsertModuleBatteryCharge(new MODULE_BATTERYCHARGE(strDevId, dbMgr.getCurrentTime(), Double.parseDouble(strCharge_After)));
				if(!bRet)
				{
					LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s)db operation failed. ", strDevId));
					return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
				}
			} else {
				boolean bRet = dbMgr.UpdateModuleBatteryChargeData(new MODULE_BATTERYCHARGE(strDevId, dbMgr.getCurrentTime(), Double.parseDouble(strCharge_After)));
				if(!bRet)
				{
					LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s)db operation failed. ", strDevId));
					return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
				}
			}
			
			USER_MODULE info = dbMgr.QueryUserModuleByDevId(strDevId);
			
			NotifyToAPP(info.getUserName(),strDevId, ServerCommDefine.NOTIFY_BATTERYENERGE, 
					ServerRetCodeMgr.SUCCESS_CODE,  strCharge); 
			
			//通知模块通知已收到
			ResponseToModule(strDevId, String.format("%s#", ServerCommDefine.NOTIFY_BATTERYENERGE));
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
