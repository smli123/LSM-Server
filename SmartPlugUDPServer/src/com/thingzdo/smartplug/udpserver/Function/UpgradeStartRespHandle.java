package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.ModuleUpgradeOnLineMgr;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class UpgradeStartRespHandle implements ICallFunction{
	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
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
	    ServerWorkThread thread = (ServerWorkThread)thread_base;
		String strRet[] = strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strMsgHeader = strRet[1].trim();
		String strUserName	= strRet[2].trim();
		String strDevId		= strRet[3].trim();
		int iRetCode		= Integer.valueOf(strRet[4].trim());
		int iFileBinNo		= Integer.valueOf(strRet[5].trim()) ;
		String strVersion	= strRet[6].trim();
		int iBlockSize		= Integer.valueOf(strRet[7].trim()) ;
		int iAuxFileBinNo	= 1024;  // Default Value
		
		/* 更新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strCookie);
		
		/* 刷新心跳状态 , 由于存在Resend机制，此处不能设置Alive为true*/
		//ServerWorkThread.RefreshModuleAliveFlag(strDevId, true);
		ServerWorkThread.RefreshModuleIP(strDevId, thread.getSrcIP(), thread.getSrcPort());
		
		if(iRetCode != 0)
		{
			LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s) module return error. %d", strDevId, iRetCode));
			return ServerRetCodeMgr.ERROR_CODE_MODULE_RET_ERROR;
		}

		/* 关闭 Upgrade start 定时器 */
		ModuleUpgradeOnLineMgr upgradeMgr = thread.GetModuleUpgradeMgr(strDevId);
		if (upgradeMgr != null) {
			iAuxFileBinNo = upgradeMgr.getAuxFileBinNo();
			//upgradeMgr.SetUpgradeStartCommand("");
			upgradeMgr.StopUpgradeStartTimer();
		}
		thread.UnRegisterUpgradeMgr(strDevId);
		
		//初始化升级校验参数 存在多次注册，不过没有关系
		thread.RegisterUpgradeMgr(iBlockSize, iFileBinNo, strDevId, strUserName, iAuxFileBinNo, upgradeMgr.getDeviceType());
		
		/* 启动 Upgrade send 定时器 */
		upgradeMgr = thread.GetModuleUpgradeMgr(strDevId);
		if (upgradeMgr == null) {
			return ServerRetCodeMgr.ERROR_COMMON;
			
		} else {
			String strMsg_resend = strMsg;	// lishimin need modify to change command.
			upgradeMgr.SetUpgradeSendCommand(strMsg_resend);
			upgradeMgr.StartUpgradeSendTimer();
		}
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		try {
			byte[] byResp = new byte[iBlockSize + 100];
		
			/* 读取并发送数据 */
			int iRet = thread.GetModuleUpgradeMgr(strDevId).GetSoftwareBinData(strCookie, byResp);
			
			if(iRet == ModuleUpgradeOnLineMgr.SEND_END
				    || iRet==ModuleUpgradeOnLineMgr.SEND_ERROR)
				{
					/* 首次发送不应该到该分支，属异常 */
					LogWriter.WriteErrorLog(LogWriter.SELF, "abnormal stop load."); 
					
					// 给客户端上报进度
					USER_MODULE info = dbMgr.QueryUserModuleByDevId(strDevId);
					NotifyToAPP(info.getUserName(),strDevId, ServerCommDefine.NOTIFY_UPGRADEAP_STATUS, 
							iRet,  String.format("%d,%d", upgradeMgr.getBlockIdx(), upgradeMgr.getBlockTotal()));
					return ServerRetCodeMgr.ERROR_COMMON;
				}
			upgradeMgr.SetUpgradeStartCommand_ToModule(byResp);

			/* 将数据发送至模块 */
			ResponseToModule(strDevId, byResp);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e);
		} finally {
			dbMgr.Destroy();
		}
		
		return ServerRetCodeMgr.SUCCESS_CODE;
	}

}
