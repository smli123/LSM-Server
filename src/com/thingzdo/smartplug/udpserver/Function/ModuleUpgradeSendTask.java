package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.util.TimerTask;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ModuleUpgradeOnLineMgr;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;

public class ModuleUpgradeSendTask  extends TimerTask implements ICallFunction{
	ModuleUpgradeOnLineMgr m_upgradeMgr = null;
	private static int m_resend_count = 0;
	private static int RESEND_COUNT_UP_LIMIT = 30;
	
	public ModuleUpgradeSendTask(ModuleUpgradeOnLineMgr mgr)
	{
		m_upgradeMgr = mgr;
	}
	@Override
	public void run() {
		if (null == m_upgradeMgr)
		{
			//没有收到心跳包
			LogWriter.WriteErrorLog(LogWriter.SELF,
					String.format("Upgrade Send: ModuleUpgradeOnLineMgr = null."));
			return;
		}
		if (m_resend_count++ >= RESEND_COUNT_UP_LIMIT) {
			m_upgradeMgr.StopUpgradeSendTimer();
			LogWriter.WriteDebugLog(LogWriter.SELF,
					String.format("Upgrade Failed. Send Timer is Closed. [%s] count:[%d]",m_upgradeMgr.getModuleID(), m_resend_count));
			m_resend_count = 0;

			return;
		}
		
		String strModuleID = m_upgradeMgr.getModuleID();
//		if(ServerWorkThread.getModuleConnectInfo(strModuleID).isAlive() == false)
//		{
//			//模块未登陆
//			LogWriter.WriteErrorLog(LogWriter.SELF,
//					String.format("Upgrade Send: [%s]Module not login.", strModuleID));
//		}
//		else
		{
			//定时器到，检测是否完成接收； 若未到终点，直接重新发送；若已经到达终点，cancel掉定时器即可；
			if (m_upgradeMgr.GetUpgradeSendTimerFlag() == true) {
				String strMsg = m_upgradeMgr.GetUpgradeSendCommand();
				
				try {
					int iRet = ServerWorkThread.ServerMsgHandle(strMsg, strMsg.getBytes());
					
					// Only for Debug Info
					byte[] byResp = m_upgradeMgr.GetUpgradeStartCommand_ToModule();
					String tmpMsg = "";
					for (int i = 0; i < 100; i++) {
						tmpMsg += (char)(byResp[i]);
					}
					LogWriter.WriteDebugLog(LogWriter.SELF,
							String.format("Resend Upgrade_Send command. [%s] count:[%d] Msg:[%s] ", strModuleID, m_resend_count, tmpMsg));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				m_upgradeMgr.StopUpgradeSendTimer();
				
				LogWriter.WriteDebugLog(LogWriter.SELF,
						String.format("Upgrade Send Timer is cancel. [%s] count:[%d]", strModuleID, m_resend_count));
			}
		}
	}
	
	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
}
