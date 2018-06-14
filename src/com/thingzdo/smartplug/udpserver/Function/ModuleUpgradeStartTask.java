package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.util.TimerTask;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ModuleUpgradeOnLineMgr;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;

public class ModuleUpgradeStartTask  extends TimerTask implements ICallFunction{
	ModuleUpgradeOnLineMgr m_upgradeMgr = null;
	private static int m_resend_count = 0;
	private static int RESEND_COUNT_UP_LIMIT = 10;
	
	public ModuleUpgradeStartTask(ModuleUpgradeOnLineMgr mgr)
	{
		m_upgradeMgr = mgr;
	}
	@Override
	public void run() {
		if (null == m_upgradeMgr)
		{
			//没有收到心跳包
			LogWriter.WriteErrorLog(LogWriter.SELF,
					String.format("Upgrade Start: ModuleUpgradeOnLineMgr = null."));
			return;
		}
		if (m_resend_count++ >= RESEND_COUNT_UP_LIMIT) {
			m_upgradeMgr.StopUpgradeStartTimer();
			LogWriter.WriteDebugLog(LogWriter.SELF,
					String.format("Upgrade Failed. Start Timer is Closed. [%s] count:[%d]", m_upgradeMgr.getModuleID(), m_resend_count));
			//m_resend_count = 0;
			//return;
		}
		
		String strModuleID = m_upgradeMgr.getModuleID();
//		if(ServerWorkThread.getModuleConnectInfo(strModuleID).isAlive() == false)
//		{
//			//模块未登陆
//			LogWriter.WriteErrorLog(LogWriter.SELF,
//					String.format("Upgrade Start: [%s]Module not login.", strModuleID));
//		}
//		else
		{
			//定时器到，检测是否完成接收； 若未到终点，直接重新发送；若已经到达终点，cancel掉定时器即可；
			if (m_upgradeMgr.GetUpgradeStartTimerFlag() == true) {
				String strMsg = m_upgradeMgr.GetUpgradeStartCommand();
				try {
					int iRet = ServerWorkThread.ServerMsgHandle(strMsg, strMsg.getBytes());
					
					LogWriter.WriteDebugLog(LogWriter.SELF,
							String.format("Resend Upgrade_Start command. [%s] count:[%d] Msg:[%s] ", strModuleID, m_resend_count, strMsg));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				m_upgradeMgr.StopUpgradeStartTimer();
				LogWriter.WriteDebugLog(LogWriter.SELF,
						String.format("Upgrade Start Timer is Close. [%s] count:[%d]", strModuleID, m_resend_count));
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
