package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ModuleUpgradeOnLineMgr;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class UpgradeEndRspHandle implements ICallFunction{
	
	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**********************************************************************************************************
	 * @name UpgradeModuleEndRspHandle 模块通知服务器其已收到END消息
	 * @param 模块回复：cookiID,UPGRADEEND,usrname,moduleID,0#
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg) 
	{
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strNewCookie	= strRet[0].trim();
		String strUserName	= strRet[2].trim();
		String strDevId		= strRet[3].trim();
		int iRetCode		= Integer.valueOf(strRet[4].trim());
		
		/* 更新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);
		/* 刷新心跳状态 , 由于存在Resend机制，此处不能设置Alive为true*/
		//ServerWorkThread.RefreshModuleAliveFlag(strDevId, true);
		ServerWorkThread.RefreshModuleIP(strDevId, thread.getSrcIP(), thread.getSrcPort());
		
		if(iRetCode != 0)
		{
			LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s) module return error. %d", strDevId, iRetCode));
			return ServerRetCodeMgr.ERROR_CODE_MODULE_RET_ERROR;
		}
		
		/* 关闭 Upgrade send 定时器 */
		ModuleUpgradeOnLineMgr upgradeMgr = thread.GetModuleUpgradeMgr(strDevId);
		if (upgradeMgr == null) {
			return ServerRetCodeMgr.SUCCESS_CODE;
			
		} else {
			upgradeMgr.SetUpgradeSendCommand("");
			upgradeMgr.StopUpgradeSendTimer();
		}
		
		thread.UnRegisterUpgradeMgr(strDevId);
		
		/* 通知模块，服务器收到END响应 */
		String strResp = GetUpgradeOverString(thread, strUserName, strDevId);
		ResponseToModule(strDevId, strResp);
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		// 给客户端上报进度
		USER_MODULE info = dbMgr.QueryUserModuleByDevId(strDevId);
		NotifyToAPP(info.getUserName(),strDevId, ServerCommDefine.NOTIFY_UPGRADEAP_STATUS, 
				ServerRetCodeMgr.SUCCESS_CODE,  String.format("%d,%d", upgradeMgr.getBlockIdx(), upgradeMgr.getBlockTotal()));
		
		dbMgr.Destroy();
		return ServerRetCodeMgr.SUCCESS_CODE;
	}
	/**********************************************************************************************************
	 * @name UpgradeOkHandle 服务器通知模块，服务器已收到模块升级完成消息
	 * @param cookie,UPGRADEOK,usernme,moduleid#
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	private String GetUpgradeOverString(ServerWorkThread thread, String strUserName, String strDevId)
	{
		String strRsp = String.format("%s,%s,%s,%s#",
				ServerWorkThread.getModuleCookie(strDevId),
				ServerCommDefine.MODULE_UPGRADE_OVER,
				strUserName,
				strDevId);
		
		return strRsp;
	}
}
