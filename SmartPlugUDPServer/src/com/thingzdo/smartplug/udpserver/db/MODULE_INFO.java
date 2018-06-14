package com.thingzdo.smartplug.udpserver.db;
/**
 * mysql> describe module_info;
+--------------+---------------------+------+-----+---------+-------+
| Field        | Type                | Null | Key | Default | Extra |
+--------------+---------------------+------+-----+---------+-------+
| module_id    | varchar(32)         | NO   | PRI | NULL    |       |
| module_name  | varchar(32)         | NO   |     | NULL    |       |
| power_status | tinyint(1) unsigned | NO   |     | NULL    |       |
| light_status | tinyint(1) unsigned | NO   |     | NULL    |       |
| red          | tinyint(1) unsigned | NO   |     | NULL    |       |
| green        | tinyint(1) unsigned | NO   |     | NULL    |       |
| blue         | tinyint(1) unsigned | NO   |     | NULL    |       |
+--------------+---------------------+------+-----+---------+-------+
7 rows in set (0.00 sec)
 * */
public class MODULE_INFO {
	public final static String 	TABLE_NAME 				= "module_info";
	public final static String 	MODULE_ID				= "module_id";
	public final static String 	MODULE_NAME				= "module_name";
	public final static String  MODULE_MAC				= "mac";
	public final static String  MODULE_VER				= "module_version";
	public final static String  MODULE_TYPE				= "module_type";
	public final static String  PROTOCOL_TYPE			= "protype";
	public final static String  POWER_STATUS			= "power_status";
	public final static String	MODE					= "mode";
	public final static String  RED						= "red";
	public final static String  GREEN					= "green";
	public final static String  BLUE					= "blue";
	public final static String  COOKIE					= "cookie";
	public final static String  AIRCON_NAME				= "aircon_name";
	public final static String  TV_NAME					= "tv_name";
	
	public final static int POWER_OFF 		= 0;
	public final static int POWER_ON 		= 1;
	public final static int OFFLINE			= 0;
	public final static int ONLINE			= 1;
	
	private String m_strModuleId;
	private String m_strModuleName;
	private String m_strMac;
	private String m_strModuleVer;
	private String m_strModuleType;
	private int m_iProType;	/*Э������ TCP��0��UDP��1*/
	private int m_iPwrStatus;
	private int m_iRed;
	private int m_iGreen;
	private int m_iBlue;
	private int m_iMode;
	private String m_strCookie;
	private String m_airconname;
	private String m_tvname;
	
	
	public MODULE_INFO(String strModuleId,String strModuleName, String strMac, String strModuleVer, String strModuleType, 
			int iProType, int iPwrStatus, int iRed, int iGreen, int iBlue, int iMode, String strCookie, String airconname, String tvname)
	{
		this.setModuleId(strModuleId);
		this.setModuleName(strModuleName);
		this.setMac(strMac);
		this.setModuleVer(strModuleVer);
		this.setModuleType(strModuleType);
		this.setProType(iProType);
		this.setPwrStatus(iPwrStatus);
		this.setRed(iRed);
		this.setGreen(iGreen);
		this.setBlue(iBlue);
		this.setMode(iMode);
		this.setCookie(strCookie);
		this.setAirConName(airconname);
		this.setTVName(tvname);
	}
	public MODULE_INFO(String strModuleId,String strModuleName, String strMac)
	{
		this.setModuleId(strModuleId);
		this.setModuleName(strModuleName);
		this.setMac(strMac);
		this.setModuleVer("unknow");
		this.setModuleType("0_0");
		this.setPwrStatus(this.POWER_OFF);
		this.setRed(255);
		this.setGreen(255);
		this.setBlue(255);
		this.setMode(0);
		this.setCookie("0");
		this.setProType(0);
		this.setAirConName("");
		this.setTVName("");
	}
	public MODULE_INFO(String strModuleId,String strModuleName, String strMac, String strModuleVer, String strModuleType)
	{
		this.setModuleId(strModuleId);
		this.setModuleName(strModuleName);
		this.setMac(strMac);
		this.setModuleVer(strModuleVer);
		this.setModuleType(strModuleType);
		this.setPwrStatus(this.POWER_OFF);
		this.setRed(255);
		this.setGreen(255);
		this.setBlue(255);
		this.setMode(0);
		this.setCookie("0");
		this.setProType(0);
		this.setAirConName("");
		this.setTVName("");
	}
	
	public boolean Equal(MODULE_INFO info)
	{
		if(info == null)
		{
			return false;
		}
		if((this.m_strModuleId.equalsIgnoreCase(info.getModuleId()) &&
			(this.m_strModuleName.equalsIgnoreCase(info.getModuleName()))))
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

	/**
	 * @return the m_strModuleName
	 */
	public String getModuleName() {
		return m_strModuleName;
	}

	/**
	 * @param m_strModuleName the m_strModuleName to set
	 */
	public void setModuleName(String m_strModuleName) {
		this.m_strModuleName = m_strModuleName;
	}
	public int getPwrStatus() {
		return m_iPwrStatus;
	}
	public void setPwrStatus(int iPwrStatus) {
		this.m_iPwrStatus = iPwrStatus;
	}
	public int getRed() {
		return m_iRed;
	}
	public void setRed(int iRed) {
		this.m_iRed = iRed;
	}
	public int getGreen() {
		return m_iGreen;
	}
	public void setGreen(int iGreen) {
		this.m_iGreen = iGreen;
	}
	public int getBlue() {
		return m_iBlue;
	}
	public void setBlue(int iBlue) {
		this.m_iBlue = iBlue;
	}
	public int getMode() {
		return m_iMode;
	}
	public void setMode(int iMode) {
		this.m_iMode = iMode;
	}
	public String getCookie() {
		return m_strCookie;
	}
	public void setCookie(String strCookie) {
		this.m_strCookie = strCookie;
	}
	public int getProType() {
		return m_iProType;
	}
	public void setProType(int iProType) {
		this.m_iProType = iProType;
	}
	public String getMac() {
		return m_strMac;
	}
	public void setMac(String strMac) {
		this.m_strMac = strMac;
	}
	public String getModuleVer() {
		return m_strModuleVer;
	}
	public void setModuleVer(String strModuleVer) {
		this.m_strModuleVer = strModuleVer;
	}
	public String getModuleType() {
		return m_strModuleType;
	}
	public void setModuleType(String strModuleType) {
		this.m_strModuleType = strModuleType;
	}
	public String getAirConName() {
		return m_airconname;
	}
	public void setAirConName(String airconname) {
		this.m_airconname = airconname;
	}
	public String getTVName() {
		return m_tvname;
	}
	public void setTVName(String tvname) {
		this.m_tvname = tvname;
	}
}