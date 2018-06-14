package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_BATTERYCHARGE;
import com.thingzdo.smartplug.udpserver.db.MODULE_BATTERYLOCATION;
import com.thingzdo.smartplug.udpserver.db.MODULE_CHARGE;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class QueryBatteryEnergeHandle implements ICallFunction{
	
	//20180104163806,APPQRYBATTERYENERGE,hexiaoxu,648044,0#
	@SuppressWarnings("finally")
	@Override
	public int call(Runnable thread_base, String strMsg) {
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strMsgHeader = strRet[1].trim();
		String strUserName 	= strRet[2].trim();
		String strModuleId 	= strRet[3].trim();

		ServerDBMgr dbMgr = new ServerDBMgr();
		try
		{
			int iRet = CheckAppCmdValid(strUserName, strCookie);
			if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
			{
				ResponseToAPP(strMsgHeader, strUserName, ServerCommDefine.DEFAULT_MODULE_ID, iRet);
				return iRet;
			}

			Vector<MODULE_BATTERYCHARGE> vecs = dbMgr.QueryModuleBatteryChargeByModuleId(strModuleId);
			String strCharge = "1970-01-01 00:00:00,-1.0"; 
			if (vecs.size() > 0) {
				strCharge = vecs.get(0).getOperDate() + "," + vecs.get(0).getCharge();
			}
			ResponseToAPP(strMsgHeader, strUserName, 0, strCharge);

			return ServerRetCodeMgr.SUCCESS_CODE;
			
		} catch (Exception e) {
			e.printStackTrace();
			return ServerRetCodeMgr.ERROR_COMMON;
		}
		finally
		{
			dbMgr.Destroy();
			return 0;
		}
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
		return 0;
	}
}
