package com.thingzdo.smartplug.udpserver.db;
/**
 * mysql> describe module_irscene;
+--------------+---------------------+------+-----+---------+-------+
| Field        | Type                | Null | Key | Default | Extra |
+--------------+---------------------+------+-----+---------+-------+
| id    	   | varchar(32)         | NO   | PRI | NULL    |       |
| module_id    | varchar(32)         | NO   |     | NULL    |       |
| enable       | varchar(32)         | NO   |     | NULL    |       |
| power        | tinyint(1) unsigned | NO   |     | NULL    |       |
| mode         | tinyint(1) unsigned | NO   |     | NULL    |       |
| direction    | tinyint(1) unsigned | NO   |     | NULL    |       |
| temperature  | tinyint(1) unsigned | NO   |     | NULL    |       |
| time         | varchar(32)         | NO   |     | NULL    |       |
| period       | varchar(32)         | NO   |     | NULL    |       |
| irname       | varchar(32)         | NO   |     | NULL    |       |
+--------------+---------------------+------+-----+---------+-------+
7 rows in set (0.00 sec)
 * */
public class MODULE_IRSCENE {

	public final static String 	TABLE_NAME 				= "module_irscene";
	public final static String 	IRSCENE_ID				= "irscene_id";	// 模块ID
	public final static String 	MODULE_ID				= "module_id";	// 模块ID
	public final static String 	ENABLE					= "enable";		// 是否使能
	public final static String  POWER					= "power";		// 开关
	public final static String  MODE					= "mode";		// 模式
	public final static String  DIRECTION				= "direction";	// 风向
	public final static String  SCALE					= "scale";		// 风力
	public final static String  TEMPERATURE				= "temperature";// 温度
	public final static String	TIME					= "time";		// 定时的时间
	public final static String  PERIOD					= "period";		// 定时周期
	public final static String  IRNAME					= "irname";		// 空调名称

	public final static int ENABLED			= 0;
	public final static int DISABLE 		= 1;
	

	public final static int POWER_ON 		= 0;
	public final static int POWER_OFF 		= 1;

	public final static int DIRECTIOIN_OFF	= 0;
	public final static int DIRECTIOIN_ON	= 1;

	public final static int WINDSCALE_AUTO	= 0;
	public final static int WINDSCALE_SMALL	= 1;
	public final static int WINDSCALE_MIDDLE= 2;
	public final static int WINDSCALE_BIG	= 3;
	
	public final static int MODE_AUTO		= 0;
	public final static int MODE_COOL		= 1;
	public final static int MODE_WET		= 2;
	public final static int MODE_WIND		= 3;
	public final static int MODE_WARM		= 4;

	private int    m_Id;
	private int    m_irsceneid;
	private String m_strModuleId;
	private int	   m_iEnable;
	private int	   m_iPower;
	private int	   m_iMode;
	private int	   m_iDirection;
	private int	   m_iScale;
	private int	   m_iTemperature;
	private String m_strTime;
	private String m_strPeriod;
	private String m_strIRName;
	
	public MODULE_IRSCENE(int irsceneid, String strModuleId, int iPower, int iMode, int iDirection, int iScale, int iTemperature, String strTime, String strPeriod, String strIRName, int iEnable)
	{
		this.setIRSceneId(irsceneid);
		this.setModuleId(strModuleId);
		this.setEnable(iEnable);
		this.setPower(iPower);
		this.setMode(iMode);
		this.setDirection(iDirection);
		this.setScale(iScale);
		this.setTemperature(iTemperature);
		this.setTime(strTime);
		this.setPeriod(strPeriod);
		this.setIRName(strIRName);
	}
	public MODULE_IRSCENE(String strModuleId)
	{
		this.setModuleId(strModuleId);
		this.setIRSceneId(0);
		this.setEnable(DISABLE);
		this.setPower(POWER_OFF);
		this.setMode(MODE_AUTO);
		this.setDirection(DIRECTIOIN_OFF);
		this.setTemperature(25);
		this.setTime("00:00");
		this.setPeriod("0000000");
		this.setIRName("");
	}
	
	public boolean Equal(MODULE_IRSCENE info)
	{
		if(info == null)
		{
			return false;
		}
		if( this.m_strModuleId.equalsIgnoreCase(info.getModuleId()) &&
			(this.m_irsceneid == info.getIRSceneId()))
		{
			return true;
		}
		return false;
	}

	public int getIRSceneId() {
		return m_irsceneid;
	}
	public void setIRSceneId(int Id) {
		this.m_irsceneid = Id;
	}	
	public String getModuleId() {
		return m_strModuleId;
	}
	public void setModuleId(String ModuleId) {
		this.m_strModuleId = ModuleId;
	}
	public int getEnable() {
		return m_iEnable;
	}
	public void setEnable(int Enable) {
		this.m_iEnable = Enable;
	}
	public int getPower() {
		return m_iPower;
	}
	public void setPower(int Power) {
		this.m_iPower = Power;
	}
	public int getMode() {
		return m_iMode;
	}
	public void setMode(int Mode) {
		this.m_iMode = Mode;
	}
	public int getDirection() {
		return m_iDirection;
	}
	public void setDirection(int Direction) {
		this.m_iDirection = Direction;
	}
	public int getScale() {
		return m_iScale;
	}
	public void setScale(int Scale) {
		this.m_iScale = Scale;
	}
	public int getTemperature() {
		return m_iTemperature;
	}
	public void setTemperature(int Temperature) {
		this.m_iTemperature = Temperature;
	}
	public String getTime() {
		return m_strTime;
	}
	public void setTime(String Time) {
		this.m_strTime = Time;
	}
	public String getPeriod() {
		return m_strPeriod;
	}
	public void setPeriod(String Period) {
		this.m_strPeriod = Period;
	}
	public String getIRName() {
		return m_strIRName;
	}
	public void setIRName(String strIRName) {
		this.m_strIRName = strIRName;
	}
}