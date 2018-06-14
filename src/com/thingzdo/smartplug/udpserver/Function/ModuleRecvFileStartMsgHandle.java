package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ModuleRecvFileMgr;
import com.thingzdo.smartplug.udpserver.ModuleUpgradeOnLineMgr;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;

public class ModuleRecvFileStartMsgHandle implements ICallFunction{
	
	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**********************************************************************************************************
	 * @name ModuleRecvFileStartMsgHandle 模块通知服务器其已收到Start消息
	 * @param 收到格式：20150101000506,JPG_START,yungrade_service,10590495#
	 * @param 回复格式：cookiID,JPG_START,usrname,moduleID,0#
	 * @return  boolean 是否成功
	 * @author smli
	 * @date 2017/04/02
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg) 
	{
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		String strRet[] 			= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie			= strRet[0].trim();
		String strMsgHeader			= strRet[1].trim();
		String strUserName 			= strRet[2].trim();
		String strModuleId			= strRet[3].trim();
		
		ServerWorkThread.RefreshModuleIP(strModuleId, thread.getSrcIP(), thread.getSrcPort());

		/* 注册接收文件 */
		int fileno = thread.GetModuleFileNO(strModuleId);
		thread.RegisterRecvFileMgr(strModuleId, fileno);
		
		ModuleRecvFileMgr mgr = thread.GetModuleRecvFileMgr(strModuleId);
		mgr.StartRecvFileStartTimer();
		
		/* 校验参数合法性 */
		int iRet = ServerRetCodeMgr.SUCCESS_CODE;
			
		/* 透传给模块 */
		try {
			/* 待模块返回 */
			//20150101000506,JPG_START,usrname,moduleid,0,boot,version,512#
			String strRsp = String.format("%s,%s,%s,%s,%s,%s,%s,%s",strCookie,strMsgHeader,strUserName,strModuleId,"0","boot","version","512");
			iRet = NotifyToModule(strRsp);
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
