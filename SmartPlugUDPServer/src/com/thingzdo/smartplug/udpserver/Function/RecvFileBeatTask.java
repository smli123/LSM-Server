package com.thingzdo.smartplug.udpserver.Function;

import java.sql.Timestamp;
import java.util.TimerTask;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ConnectInfo;
import com.thingzdo.smartplug.udpserver.ModuleRecvFileMgr;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerParamConfiger;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_DATA;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class RecvFileBeatTask  extends TimerTask implements ICallFunction{
	private ModuleRecvFileMgr m_RecvFileMgr = null;

	public RecvFileBeatTask(ModuleRecvFileMgr moduleRecvFileMgr) {
		// TODO Auto-generated constructor stub
		this.m_RecvFileMgr = moduleRecvFileMgr;
	}
	@Override
	public void run() {
		String strModuleID = m_RecvFileMgr.getModuleID();
		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("[%s]RecvFileTask Timer:%s", strModuleID, this.toString()));
		
		m_RecvFileMgr.StopRecvFileStartTimer();
		ServerWorkThread.UnRegisterRecvFileMgr(strModuleID);
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
