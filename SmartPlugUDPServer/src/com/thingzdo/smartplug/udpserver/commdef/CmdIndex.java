package com.thingzdo.smartplug.udpserver.commdef;

public class CmdIndex {
	public String m_strMsgHeader;
	public String m_strUserName;
	public String m_strDevId;
	
	public CmdIndex(String strMsgHeaer, String strUserName, String strDevId)
	{
		m_strMsgHeader = strMsgHeaer;
		m_strUserName = strUserName;
		m_strDevId = strDevId;
	}
}
