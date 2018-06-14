package com.thingzdo.smartplug.udpserver.db;
/**USER_MODULE(String strUserName,String strModuleId,byte iCtrlMode)*/
public class USER_MODULE {
	public final static byte 		PRIMARY				= 0;		//主用户
	public final static byte		SLAVE					= 1;		//从用户
	public final static String	TABLE_NAME		= "user_module";	//数据库表名称
	public final static String 	USER_NAME		= "user_name";
	public final static String   	MODULE_ID		= "module_id";
	public final static String	CTRL_MODE		= "ctrl_mode";			//0主用户，1从用户
	
	private String m_strUserName;
	private String m_strModuleId;
	private byte m_CtrlMode;
	public USER_MODULE(String strUserName,String strModuleId,byte iCtrlMode)
	{
		setUserName(strUserName);
		setModuleId(strModuleId);
		setCtrlMode(iCtrlMode);
	}
	public boolean equal(USER_MODULE info)
	{
		if(null == info)
		{
			return false;
		}
		if(this.m_strUserName.equalsIgnoreCase(info.getUserName())&&
			this.m_strModuleId.equalsIgnoreCase(info.getModuleId())&&
			this.m_CtrlMode == info.getCtrlMode())
		{
			return true;
		}
		return false;
	}
	public String getUserName() {
		return m_strUserName;
	}
	public void setUserName(String strUserName) {
		this.m_strUserName = strUserName;
	}
	public String getModuleId() {
		return m_strModuleId;
	}
	public void setModuleId(String strModuleId) {
		this.m_strModuleId = strModuleId;
	}
	public byte getCtrlMode() {
		return m_CtrlMode;
	}
	public void setCtrlMode(byte iCtrlMode) {
		this.m_CtrlMode = iCtrlMode;
	}
}
