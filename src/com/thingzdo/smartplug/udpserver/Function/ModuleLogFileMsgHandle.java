package com.thingzdo.smartplug.udpserver.Function;

import java.io.UnsupportedEncodingException;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ModuleLogFileMgr;
import com.thingzdo.smartplug.udpserver.ModuleRecvFileMgr;
import com.thingzdo.smartplug.udpserver.ModuleUpgradeOnLineMgr;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;

public class ModuleLogFileMsgHandle implements ICallFunction{
	
	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**********************************************************************************************************
	 * @name ModuleLogFileMsgHandle 模块通知服务器其已收到END消息
	 * @param 模块接收：20160802163030,JPG_SEND,test,module_id,buf_no,readsizes,errcheck,data#
	 * @param 模块回复：20160802163030,JPG_SEND,test,module_id,0,index,buf_size,total_size,total_err#
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg) 
	{
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strMsgHeader	= strRet[1].trim();
		String strUserName	= strRet[2].trim();
		String strDevId		= strRet[3].trim();
		String strModuleLog = strRet[4].trim();
				
		/* 更新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strCookie);

		ServerWorkThread.RefreshModuleIP(strDevId, thread.getSrcIP(), thread.getSrcPort());

		ModuleLogFileMgr mgr = thread.GetModuleLogFileMgr(strDevId);
		if (mgr == null) {
			return ServerRetCodeMgr.ERROR_COMMON;
		} else {
			 mgr.WriteToFile(strModuleLog);
		}
		
		return ServerRetCodeMgr.SUCCESS_CODE;
	}

}
