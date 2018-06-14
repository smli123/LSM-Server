package com.thingzdo.smartplug.udpserver.db;

import java.sql.Timestamp;

/**
 * mysql> describe module_info;
+--------------+---------------------+------+-----+---------+-------+
| Field        | Type                | Null | Key | Default | Extra |
+--------------+---------------------+------+-----+---------+-------+
| module_id    | varchar(32)         | NO   |     | NULL    |       |
| module_name  | varchar(32)         | NO   |     | NULL    |       |
| oper_date    | datetime 			 | NO   |     | NULL    |       |
| longitude    | double		         | NO   |     | NULL    |       |	经度
| dimension    | double		         | NO   |     | NULL    |       |	纬度
+--------------+---------------------+------+-----+---------+-------+
7 rows in set (0.00 sec)
 * */
public class MODULE_BATTERYLOCATION {
	public final static String 	TABLE_NAME 				= "module_batterylocation";
	public final static String 	MODULE_ID				= "module_id";
	public final static String  OPER_DATE				= "oper_date";
	public final static String  LONGITUDE				= "longitude";
	public final static String  DIMENSION				= "dimension";
	
	private String m_strModuleId;
	private String m_strModuleName;
	private Timestamp m_dtOperDate;
	private double m_dLongitude;
	private double m_dDimension;
	
	public MODULE_BATTERYLOCATION(String strModuleId, Timestamp dtOperDate, double dLongitude, double dDimension)
	{
		this.setModuleId(strModuleId);
		this.setOperDate(dtOperDate);
		this.setLongitude(dLongitude);
		this.setDimension(dDimension);
	}
		
	public boolean Equal(MODULE_BATTERYLOCATION info)
	{
		if(info == null)
		{
			return false;
		}
		if((this.m_strModuleId.equalsIgnoreCase(info.getModuleId()) &&
			(this.m_dtOperDate.getTime() == info.getOperDate().getTime()) &&
			(this.m_dLongitude == info.getLongitude() && (this.m_dDimension == info.getDimension()))
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

	public Timestamp getOperDate() {
		return m_dtOperDate;
	}

	public void setOperDate(Timestamp dtOperDate) {
		this.m_dtOperDate = dtOperDate;
	}

	public double getLongitude() {
		return m_dLongitude;
	}

	public void setLongitude(double dLongitude) {
		this.m_dLongitude = dLongitude;
	}

	public double getDimension() {
		return m_dDimension;
	}

	public void setDimension(double dDimension) {
		this.m_dDimension = dDimension;
	}
	
}