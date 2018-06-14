package com.thingzdo.smartplug.udpserver.db;

import java.sql.Timestamp;

/**
 * mysql> describe module_info;
+--------------+---------------------+------+-----+---------+-------+
| Field        | Type                | Null | Key | Default | Extra |
+--------------+---------------------+------+-----+---------+-------+
| module_id    | varchar(32)         | NO   |     | NULL    |       |
| module_name  | varchar(32)         | NO   |     | NULL    |       |
| login_time   | datetime 			 | NO   |     | NULL    |       |
| logout_time  | datetime 			 | NO   |     | NULL    |       |
| online_time  | int(11) unsigned    | NO   |     | NULL    |       |
| memo         | varchar(255)        | NO   |     | NULL    |       |
+--------------+---------------------+------+-----+---------+-------+
7 rows in set (0.00 sec)
 * */
public class MODULE_DATA {
	public final static String 	TABLE_NAME 				= "module_data";
	public final static String 	MODULE_ID				= "module_id";
	public final static String 	MODULE_NAME				= "module_name";
	public final static String  LOGIN_TIME				= "login_time";
	public final static String  LOGOUT_TIME				= "logout_time";
	public final static String  ONLINE_TIME				= "online_time";
	public final static String  MEMO					= "memo";
	
	private String m_strModuleId;
	private String m_strModuleName;
	private Timestamp m_dtLoginTime;
	private Timestamp m_dtLogoutTime;
	private long m_iOnlineTime;
	private String m_strMemo;
	
	public MODULE_DATA(String strModuleId,String strModuleName, Timestamp dtLoginTime)
	{
		this.setModuleId(strModuleId);
		this.setModuleName(strModuleName);
		this.setLoginTime(dtLoginTime);
		this.setLogoutTime(new Timestamp(0));
		this.setOnlineTime(0);
		this.setMemo("");
	}
	
	public MODULE_DATA(String strModuleId,String strModuleName, Timestamp dtLoginTime, Timestamp dtLogoutTime)
	{
		this.setModuleId(strModuleId);
		this.setModuleName(strModuleName);
		this.setLoginTime(dtLoginTime);
		this.setLogoutTime(dtLogoutTime);
		this.setOnlineTime(0);
		this.setMemo("");
	}
	
	public MODULE_DATA(String strModuleId,String strModuleName, Timestamp dtLoginTime, Timestamp dtLogoutTime, long iOnlineTime, String strMemo)
	{
		this.setModuleId(strModuleId);
		this.setModuleName(strModuleName);
		this.setLoginTime(dtLoginTime);
		this.setLogoutTime(dtLogoutTime);
		this.setOnlineTime(iOnlineTime);
		this.setMemo(strMemo);
	}
	
	public boolean Equal(MODULE_DATA info)
	{
		if(info == null)
		{
			return false;
		}
		if((this.m_strModuleId.equalsIgnoreCase(info.getModuleId()) &&
			(this.m_strModuleName.equalsIgnoreCase(info.getModuleName())) &&
			(this.m_dtLoginTime.equals((info.getLoginTime()))) &&
			(this.m_dtLogoutTime.equals(info.getLogoutTime()))
			))
		{
			return true;
		}
		return false;
	}

	public String getModuleId() {
		return m_strModuleId;
	}

	public void setModuleId(String strModuleId) {
		this.m_strModuleId = strModuleId;
	}

	public String getModuleName() {
		return m_strModuleName;
	}
	public void setModuleName(String strModuleName) {
		this.m_strModuleName = strModuleName;
	}
	
	public Timestamp getLoginTime() {
		return m_dtLoginTime;
	}

	public void setLoginTime(Timestamp dtLoginTime) {
		this.m_dtLoginTime = dtLoginTime;
	}

	public Timestamp getLogoutTime() {
		return m_dtLogoutTime;
	}

	public void setLogoutTime(Timestamp dtLogoutTime) {
		this.m_dtLogoutTime = dtLogoutTime;
	}

	public long getOnlineTime() {
		return m_iOnlineTime;
	}
	public void setOnlineTime(long iOnlineTime) {
		this.m_iOnlineTime = iOnlineTime;
	}
	
	public String getMemo() {
		return m_strMemo;
	}

	public void setMemo(String strMemo) {
		this.m_strMemo = strMemo;
	}
	
}