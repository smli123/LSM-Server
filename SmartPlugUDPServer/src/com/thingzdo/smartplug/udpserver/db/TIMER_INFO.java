package com.thingzdo.smartplug.udpserver.db;
/**TIMER_INFO(String strTimerId,String strModuleId,String strPeroid,byte TimeOn,byte TimeOff,byte bEnable)*/
public class TIMER_INFO {
	public final static String 	TABLE_NAME = "timer_info";	//数据库表名
	public final static String 	TIMER_ID		= "timer_id";		//定时器ID
	public final static String	TIMER_TYPE	= "timer_type";	//定时器类型    0：电源 1：夜灯 2：USB 3 : BELL
	public final static String	MODULE_ID	= "module_id";	//定时器所属模块的ID
	public final static String 	PEROID			= "peroid";			//定时器的启动周期
	public final static String	TIME_ON			= "time_on";		//开启时间
	public final static String	TIME_OFF		= "time_off";		//关闭时间
	public final static String	ENABLE_FLAG	= "enable";			//是否使能
	
	public final static byte DISABLE	=	0;
	public final static byte ENABLE 	= 1;
	
	private byte 		m_TimerId;
	private byte		m_TimerType;
	private String		m_strModuleId;
	private String		m_strPeroid;
	private String  	m_strTimeOn;
	private String		m_strTimeOff;
	private byte 		m_Enable;
	
	public TIMER_INFO(byte TimerId,byte TimerType,String strModuleId,String strPeroid,String TimeOn,String TimeOff,byte bEnable)
	{
		this.setTimerId(TimerId);
		this.setTimerType(TimerType);
		this.setModuleId(strModuleId);
		this.setPeroid(strPeroid);
		this.setTimeOn(TimeOn);
		this.setTimeOff(TimeOff);
		this.setEnableFlag(bEnable);
	}
	public boolean equal(TIMER_INFO info)
	{
		if(null == info)
		{
			return false;
		}
		if(this.m_TimerId == info.getTimerId() &&
			this.m_strModuleId.equalsIgnoreCase(info.getModuleId())&&
			this.m_strPeroid.equalsIgnoreCase(info.getPeroid())&&
			this.m_strTimeOn.equalsIgnoreCase(info.getTimeOn())&&
			this.m_strTimeOff.equalsIgnoreCase(info.getTimeOff())&&
			this.m_Enable == info.getEnableFlag())
		{
			return true;
		}
		return false;
	}
	public byte getTimerId() {
		return m_TimerId;
	}
	public void setTimerId(byte TimerId) {
		this.m_TimerId = TimerId;
	}
	public String getTimeOn() {
		return m_strTimeOn;
	}
	public void setTimeOn(String TimeOn) {
		this.m_strTimeOn = TimeOn;
	}
	public String getTimeOff() {
		return m_strTimeOff;
	}
	public void setTimeOff(String TimeOff) {
		this.m_strTimeOff = TimeOff;
	}

	public String getPeroid() {
		return m_strPeroid;
	}

	public void setPeroid(String strPeroid) {
		this.m_strPeroid = strPeroid;
	}

	public String getModuleId() {
		return m_strModuleId;
	}

	public void setModuleId(String strModuleId) {
		this.m_strModuleId = strModuleId;
	}

	public byte getEnableFlag() {
		return m_Enable;
	}

	public void setEnableFlag(byte bEnable) {
		this.m_Enable = bEnable;
	}
	public byte getTimerType() {
		return m_TimerType;
	}
	public void setTimerType(byte TimerType) {
		this.m_TimerType = TimerType;
	}
}
