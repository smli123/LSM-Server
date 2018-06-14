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
| charge       | double		         | NO   |     | NULL    |       |
+--------------+---------------------+------+-----+---------+-------+
7 rows in set (0.00 sec)
 * */
public class MODULE_CHARGE {
	public final static String 	TABLE_NAME 				= "module_charge";
	public final static String 	MODULE_ID				= "module_id";
	public final static String  OPER_DATE				= "oper_date";
	public final static String  CHARGE					= "charge";
	
	private String m_strModuleId;
	private String m_strModuleName;
	private Timestamp m_dtOperDate;
	private double m_dCharge;
	
	public MODULE_CHARGE(String strModuleId, Timestamp dtOperDate, double dCharge)
	{
		this.setModuleId(strModuleId);
		this.setOperDate(dtOperDate);
		this.setCharge(dCharge);
	}
		
	public boolean Equal(MODULE_CHARGE info)
	{
		if(info == null)
		{
			return false;
		}
		if((this.m_strModuleId.equalsIgnoreCase(info.getModuleId()) &&
			(this.m_dtOperDate.getTime() == info.getOperDate().getTime()) &&
			(this.m_dCharge == info.getCharge())
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
	
	public double getCharge() {
		return m_dCharge;
	}

	public void setCharge(double dCharge) {
		this.m_dCharge = dCharge;
	}
	
}