package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ModuleUpgradeOnLineMgr;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;

public class AppUpgradeStartMsgHandle implements ICallFunction{
	@Override
	public int call(Runnable thread_base, String strMsg) {
		String strRet[] 			= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie			= strRet[0].trim();
		String strMsgHeader			= strRet[1].trim();
		String strUserName 			= strRet[2].trim();
		String strModuleId			= strRet[3].trim();
		int iauxFileNo				= Integer.valueOf(strRet[4].trim());
		String strDeviceType		= strRet[5].trim();
	
		/* 校验参数合法性 */
		int iRet = ServerRetCodeMgr.SUCCESS_CODE;
		
		/* 启动 Upgrade start 定时器 */
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		thread.RegisterUpgradeMgr(0,0,strModuleId, strUserName, iauxFileNo, strDeviceType);		
		
		ModuleUpgradeOnLineMgr upgradeMgr = thread.GetModuleUpgradeMgr(strModuleId);
		if (upgradeMgr != null) {
			upgradeMgr.SetUpgradeStartCommand(strMsg);
			upgradeMgr.StartUpgradeStartTimer();			
		}
		
		/* 透传给模块 */
		try {
			/* 待模块返回 */
			strMsg = strRet[0] + "," + strRet[1] + "," + strRet[2] + ","  + strRet[3] + "," + strRet[4] + "#" ;
			iRet = NotifyToModule(strMsg);
			if (ServerRetCodeMgr.SUCCESS_CODE != iRet)
			{
				return iRet;
			}
			
			return ServerRetCodeMgr.SUCCESS_CODE;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e);
		}

		return ServerRetCodeMgr.ERROR_COMMON;
	}
	
	/**********************************************************************************************************
	 * @name UpgradeStrartRspHandle 模板响应软件升级请求
	 * @param cookiID,UPGRADESTART,usrname,moduleID,ret_code,userbinno,Version,blocksize#
		userbinno=0或者1，0表示user1.bin，1表示user2.bin
		Version=YYYYMMDDHHMMSS_V1.0
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg) 
	{		
		// TODO Auto-generated method stub
		return 0;
	}

}
