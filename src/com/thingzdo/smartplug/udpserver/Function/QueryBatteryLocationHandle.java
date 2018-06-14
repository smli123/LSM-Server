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
import com.thingzdo.smartplug.udpserver.db.MODULE_BATTERYLOCATION;
import com.thingzdo.smartplug.udpserver.db.MODULE_CHARGE;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class QueryBatteryLocationHandle implements ICallFunction{
	
	//20180104163806,APPQRYBATTERYLOCATION,hexiaoxu,648044,0#
	@SuppressWarnings("finally")
	@Override
	public int call(Runnable thread_base, String strMsg) {
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strMsgHeader = strRet[1].trim();
		String strUserName 	= strRet[2].trim();
		String strModuleId 	= strRet[3].trim();
		int iType 			= Integer.parseInt(strRet[4].trim());

		ServerDBMgr dbMgr = new ServerDBMgr();
		try
		{
			int iRet = CheckAppCmdValid(strUserName, strCookie);
			if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
			{
				ResponseToAPP(strMsgHeader, strUserName, ServerCommDefine.DEFAULT_MODULE_ID, iRet);
				return iRet;
			}

			String hour_begin 	= getPreHour(true);
			String hour_end 	= getPreHour(false);
			String day_begin 	= getPreDay(true);
			String day_end 		= getPreDay(false);
			String week_begin 	= getPreWeek(true);
			String week_end 	= getPreWeek(false);
			String month_begin 	= getPreMonth(true);
			String month_end 	= getPreMonth(false);
			
			Vector<MODULE_BATTERYLOCATION> vecs = dbMgr.QueryModuleBatteryLocationByModuleId(strModuleId, hour_begin, hour_end);
			int    i_hour = vecs.size();
			String hour_loc = calculate_charge(vecs);
			
			vecs = dbMgr.QueryModuleBatteryLocationByModuleId(strModuleId, day_begin, day_end);
			int    i_day = vecs.size();
			String day_loc = calculate_charge(vecs);
			
			vecs = dbMgr.QueryModuleBatteryLocationByModuleId(strModuleId, week_begin, week_end);
			int    i_week = vecs.size();			
			String week_loc = calculate_charge(vecs);
			
			vecs = dbMgr.QueryModuleBatteryLocationByModuleId(strModuleId, month_begin, month_end);
			int    i_month = vecs.size();
			String month_loc = calculate_charge(vecs);
			
			if (iType == 0) {			// all Type
				int count = Integer.parseInt(strRet[5].trim());
				vecs = dbMgr.QueryModuleBatteryLocationByModuleId(strModuleId, count);
				int i_type_count = vecs.size();
				String count_loc = calculate_charge(vecs);
				
				String strContent = String.format("%d,%s", i_type_count, count_loc);
				ResponseToAPP(strMsgHeader, strUserName, 0, strContent);
			} else if (iType == 1) {	// Hour
				String strContent = String.format("%d,%s", i_hour, hour_loc);
				ResponseToAPP(strMsgHeader, strUserName, 0, strContent);
			} else if (iType == 2) {	// Day
				String strContent = String.format("%d,%s", i_day, day_loc);
				ResponseToAPP(strMsgHeader, strUserName, 0, strContent);
			} else if (iType == 3) {	// Week
				String strContent = String.format("%d,%s", i_week, week_loc);
				ResponseToAPP(strMsgHeader, strUserName, 0, strContent);
			} else if (iType == 4) {	// Month
				String strContent = String.format("%d,%s", i_month, month_loc);
				ResponseToAPP(strMsgHeader, strUserName, 0, strContent);
			} else {
				// do nothing...
			}
			
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
	
	private String calculate_charge(Vector<MODULE_BATTERYLOCATION> vecs) {
		String strOut = "";
		for (int i = 0; i < vecs.size(); i++) {
			strOut += getTimeString(vecs.get(i).getOperDate()) + ",";
			strOut += vecs.get(i).getLongitude() + ",";
			strOut += vecs.get(i).getDimension();
			if (i < vecs.size() - 1) {
				strOut += ",";
			}
		}
		return strOut;
	}
	
	private String getTimeString(Timestamp ts) {
    	SimpleDateFormat  formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");     
		String   str   =   formatter.format(ts); 
		return str;
	}
	 
	private String getPreHour(boolean begin) {
		String strDate = "";
		Calendar calendar = Calendar.getInstance();
		/* HOUR_OF_DAY 指示一天中的小时 */
		calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
		
		if (begin == true) {
			strDate = new SimpleDateFormat("yyyy-MM-dd HH:00:00").format(calendar.getTime());
		} else {
			strDate = new SimpleDateFormat("yyyy-MM-dd HH:59:59").format(calendar.getTime());
		}
		return strDate;
	}

	private String getPreDay(boolean begin) {
		String strDate = "";
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
		
		if (begin == true) {
			strDate = new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(calendar.getTime());
		} else {
			strDate = new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(calendar.getTime());
		}
		return strDate;
	}
	
	private String getPreWeek(boolean begin) {
		String strDate = "";
		Calendar calendar = Calendar.getInstance();
		int dayofweek = calendar.get(Calendar.DAY_OF_WEEK);
		
		if (begin == true) {
			calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - dayofweek -6);
			strDate = new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(calendar.getTime());
		} else {
			calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - dayofweek);
			strDate = new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(calendar.getTime());
		}
		return strDate;
	}
	

	private String getPreMonth(boolean begin) {
		String strDate = "";
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
		
		if (begin == true) {
			strDate = new SimpleDateFormat("yyyy-MM-01 00:00:00").format(calendar.getTime());
		} else {
			strDate = new SimpleDateFormat("yyyy-MM-31 23:59:59").format(calendar.getTime());
		}
		return strDate;
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
