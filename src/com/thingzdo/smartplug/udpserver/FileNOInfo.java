package com.thingzdo.smartplug.udpserver;

import java.util.Timer;

import com.thingzdo.smartplug.udpserver.Function.ModuleHeartBeatTask;
import com.thingzdo.smartplug.udpserver.Function.RecvFileBeatTask;

public class FileNOInfo {
	private String strModuleID;
	private int iFileNO;
	private Timer recvFileTimer;
	private RecvFileBeatTask recvFileTask;
	public FileNOInfo(String devId, int fileNO, Timer timer, RecvFileBeatTask bTask)
	{
		this.strModuleID = devId;
		this.iFileNO = fileNO;
		this.recvFileTimer = timer;
		this.setRecvFileBeatTask(bTask);
	}
	
	public FileNOInfo(String devId, int fileNO)
	{
		this.strModuleID = devId;
		this.iFileNO = fileNO;
		this.recvFileTimer = null;
	}

	public String getModuleID()
	{
		return strModuleID;
	}
	
	public void setModuleID(String moduleID)
	{
		this.strModuleID = moduleID;
	}
	
	public int getFileNO()
	{
		return iFileNO;
	}
	
	public void setSrcPort(int fileNO)
	{
		iFileNO = fileNO;
	}

	public Timer getRecvFileTimer()
	{
		return this.recvFileTimer;
	}

	public void setRecvFileTimer(Timer recvfiletimer) {
		this.recvFileTimer = recvfiletimer;
	}
	
	public RecvFileBeatTask getRecvFileBeatTask() {
		return recvFileTask;
	}

	public void setRecvFileBeatTask(RecvFileBeatTask bTask) {
		this.recvFileTask = bTask;
	}
}
