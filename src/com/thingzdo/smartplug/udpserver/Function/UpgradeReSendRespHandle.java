package com.thingzdo.smartplug.udpserver.Function;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.ModuleUpgradeOnLineMgr;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;

public class UpgradeReSendRespHandle implements ICallFunction{
	
	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int resp(Runnable thread_base, String strMsg) 
	{
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		String strRet[] = strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strNewCookie	= strRet[0].trim();
		String strMsgHeader = strRet[1].trim();
		String strUserName	= strRet[2].trim();
		String strDevId		= strRet[3].trim();
		int iRetCode		= Integer.valueOf(strRet[4].trim());
		int iLastBlockIdx	= Integer.valueOf(strRet[5].trim()) ;
		int iLastBlockSizes	= Integer.valueOf(strRet[6].trim()) ;
		int iTotalSendSizes	= Integer.valueOf(strRet[7].trim()) ;
		int iTotalRecvBytes	= Integer.valueOf(strRet[8].trim()) ;
		
		if(iRetCode != 0)	// 对于重发的消息，判断都是无效的。
		{
			LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s) resend: SEND module return error. %d", strDevId, iRetCode));
			return ServerRetCodeMgr.ERROR_COMMON;
		}
		
		/* 更新  Upgrade send 定时器，保存上次的下发命令 */
		ModuleUpgradeOnLineMgr upgradeMgr = thread.GetModuleUpgradeMgr(strDevId);
		
		/* upgradeMgr 为空，说明 已经升级已经完成了； */ 
		if (upgradeMgr == null) {
			return ServerRetCodeMgr.SUCCESS_CODE;
		}
		
		byte[] byResp = upgradeMgr.GetUpgradeStartCommand_ToModule();
		ResponseToModule(strDevId, byResp);

		return ServerRetCodeMgr.SUCCESS_CODE;
	}
}
