package com.thingzdo.smartplug.udpserver;

import java.util.Timer;

import com.thingzdo.smartplug.udpserver.Function.ModuleHeartBeatTask;

public class ConnectInfo {
	private String strSrcIP;
	private int strSrcPort;
	private String strCookie;
	private Timer heartTimer;
	private ModuleHeartBeatTask beatTask;
	private boolean bAlive;
	public ConnectInfo(String strIP, int strPort, String strCookie, Timer timer, ModuleHeartBeatTask bTask)
	{
		this.strSrcIP = strIP;
		this.strSrcPort = strPort;
		this.strCookie = strCookie;
		this.heartTimer = timer;
		this.setBeatTask(bTask);
		this.setAlive(true);
	}
	
	public ConnectInfo(String strIP, int strPort)
	{
		this.strSrcIP = strIP;
		this.strSrcPort = strPort;
		this.strCookie = "";
		this.heartTimer = null;
		this.setAlive(true);
	}

	public String getSrcIP()
	{
		return strSrcIP;
	}
	
	public void setSrcIP(String strIP)
	{
		strSrcIP = strIP;
	}

	public int getSrcPort()
	{
		return strSrcPort;
	}
	
	public void setSrcPort(int strPort)
	{
		strSrcPort = strPort;
	}
	public String getCookie()
	{
		return strCookie;
	}
	
	public void setCookie(String strCookie)
	{
		this.strCookie = strCookie;
	}
	
	public Timer getHeartTimer()
	{
		return this.heartTimer;
	}

	public boolean isAlive() {
		return bAlive;
	}

	public void setAlive(boolean bAlive) {
		this.bAlive = bAlive;
	}

	public ModuleHeartBeatTask getBeatTask() {
		return beatTask;
	}

	public void setBeatTask(ModuleHeartBeatTask beatTask) {
		this.beatTask = beatTask;
	}
}
