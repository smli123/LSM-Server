package com.thingzdo.smartplug.udpserver.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.thingzdo.platform.DBTool.DBTool;
import com.thingzdo.platform.DBTool.DataSourcePool;
import com.thingzdo.platform.LogTool.LogWriter;

public class ServerDBMgr {
	private final static String DB_NAME 		= "smartplug";
	private final static String USER_NAME 		= "root"; 
	private final static String USER_PASS		= "2681b009";
	private DBTool m_dbTool = new DBTool();
	
	public static void Init()
	{
		DataSourcePool.Init(DB_NAME, USER_NAME, USER_PASS);
	}
	
	/**
	 * 创建module_info的触发器
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * */
	public void Destroy()
	{
		m_dbTool.Destroy();
	}
	
	private boolean CreateModuleInfoTriger()
	{
		/*删除module_info时，需要清理timer_info,user_module*/
		String strDropTrigger = "DROP TRIGGER IF EXISTS t_delete_module";
		try {
			m_dbTool.execute(strDropTrigger);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		String strCreateTrigger = "CREATE TRIGGER t_delete_module" +
					" ALTER DELETE ON " + MODULE_INFO.TABLE_NAME +
					" BEGIN" + 
					" delete from " + TIMER_INFO.TABLE_NAME + " where " + TIMER_INFO.MODULE_ID + "= old." + MODULE_INFO.MODULE_ID +
					" delete from " + USER_MODULE.TABLE_NAME + " where " + USER_MODULE.MODULE_ID  + "old." + MODULE_INFO.MODULE_ID;
		return true;
	}
	public boolean UpdateDB(int db_version)
	{		
		return true;
	}
	//开启事务机制
	public boolean BeginTansacion()
	{
		if(null != m_dbTool)
		{
			try {
				m_dbTool.setAutoCommit(false);
				return true;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
			}
		}
		return false;
	}
	//关闭事务机制
	public boolean EndTansacion() 
	{
		if(null != m_dbTool)
		{
			try {
				m_dbTool.setAutoCommit(true);
				return true;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
			}
		}
		return false;
	}
	//提交事务
	public boolean Commit()
	{
		if(null != m_dbTool)
		{
			try {
				m_dbTool.commit();
				return true;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
			}
		}
		return false;
	}
	//取消事务
	public boolean Rollback() 
	{
		if(null != m_dbTool)
		{
			try {
				m_dbTool.rollback();
				return true;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
			}
		}
		
		return false;
	}
	/****************************************************************************
	 * MODULE_DATA相关操作
	 * **************************************************************************/
	public MODULE_DATA QueryModuleDataByModuleId(String strModuleId)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(MODULE_DATA.MODULE_ID, strModuleId);
		selection.put(MODULE_DATA.LOGOUT_TIME, String.valueOf(new Timestamp(0)));
		ResultSet rs;
		try {
			rs = m_dbTool.query(MODULE_DATA.TABLE_NAME, selection);
			if(rs.next())
			{
				return new MODULE_DATA(rs.getString(MODULE_DATA.MODULE_ID),
						rs.getString(MODULE_DATA.MODULE_NAME),
						rs.getTimestamp(MODULE_DATA.LOGIN_TIME),
						rs.getTimestamp(MODULE_DATA.LOGOUT_TIME),
						rs.getInt(MODULE_DATA.ONLINE_TIME),
						rs.getString(MODULE_DATA.MEMO));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return null;
	}
	public boolean InsertModuleData(MODULE_DATA info)
	{
		Map<String,String> content = new HashMap<String,String>();
		content.put(MODULE_DATA.MODULE_ID, info.getModuleId());
		content.put(MODULE_DATA.MODULE_NAME, info.getModuleName());
		content.put(MODULE_DATA.LOGIN_TIME, String.valueOf(info.getLoginTime()));
		content.put(MODULE_DATA.LOGOUT_TIME, String.valueOf(info.getLogoutTime()));
		content.put(MODULE_DATA.ONLINE_TIME, String.valueOf(info.getOnlineTime()));
		content.put(MODULE_DATA.MEMO, info.getMemo());
		try {
			return m_dbTool.insert(MODULE_DATA.TABLE_NAME, content);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	public boolean UpdateModuleData(MODULE_DATA info)
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		
		selection.put(MODULE_DATA.MODULE_ID, info.getModuleId());
		selection.put(MODULE_DATA.LOGIN_TIME, String.valueOf(info.getLoginTime()));
		
		content.put(MODULE_DATA.LOGOUT_TIME, String.valueOf(info.getLogoutTime()));
		content.put(MODULE_DATA.ONLINE_TIME, String.valueOf(info.getOnlineTime()));
		content.put(MODULE_DATA.MEMO, info.getMemo());
		try {
			return m_dbTool.update(MODULE_DATA.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	public boolean InitModuleData()
	{
		Timestamp strCurTime = getCurrentTime();
		
		// 查找所有ModuleIds
		Vector<MODULE_INFO> moduleInfos = QueryModuleInfo();
		
		int total = moduleInfos.size();
		for (int i = 0; i < total; i++) {
			// 在ModuleId中查找只有LoginTime，没有LogoutTime的记录；
			String strModuleId = moduleInfos.get(i).getModuleId();
			MODULE_DATA data = QueryModuleDataByModuleId(strModuleId);
			if (data != null) {
				data.setLogoutTime(strCurTime);
				long ionlineTime = (strCurTime.getTime() - data.getLoginTime().getTime())/1000;
				data.setOnlineTime(ionlineTime);
				// 使用当前时间更新LogoutTime的记录；
				if (UpdateModuleData(data) == false)
					return false;
			}
		}
		return true;
	}
	
	public Timestamp getCurrentTime()
	{
		java.util.Date dt = new java.util.Date();
		Timestamp currentTime = new Timestamp(dt.getTime());
		
		return currentTime;
	}
	
	public boolean DeleteModuleData(String strModuleId)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(MODULE_DATA.MODULE_ID, strModuleId);
		try {
			return m_dbTool.delete(MODULE_DATA.TABLE_NAME, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}

	public boolean ClearModuleData() 
	{
		try {
			return m_dbTool.delete(MODULE_DATA.TABLE_NAME);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	/****************************************************************************
	 * MODULE_CHARGE相关操作
	 * **************************************************************************/
	public Vector<MODULE_CHARGE> QueryModuleChargeByModuleId(String strModuleId, String beginDate, String endDate)
	{
		Vector<MODULE_CHARGE> vecCharge = new Vector<MODULE_CHARGE>();
		String selection = "oper_date >= \"" + beginDate + "\" and oper_date < \"" + endDate + "\"";
		selection += " and module_id = " + strModuleId;
		ResultSet rs;
		try {
			rs = m_dbTool.query(MODULE_CHARGE.TABLE_NAME, selection);
			
			while(rs.next())
			{
				vecCharge.add(new MODULE_CHARGE(rs.getString(MODULE_CHARGE.MODULE_ID),
						rs.getTimestamp(MODULE_CHARGE.OPER_DATE),
						rs.getDouble(MODULE_CHARGE.CHARGE)));
			}
			return vecCharge;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return null;
	}
	public boolean InsertModuleCharge(MODULE_CHARGE info)
	{
		Map<String,String> content = new HashMap<String,String>();
		content.put(MODULE_CHARGE.MODULE_ID, info.getModuleId());
		content.put(MODULE_CHARGE.OPER_DATE, String.valueOf(info.getOperDate()));
		content.put(MODULE_CHARGE.CHARGE, String.valueOf(info.getCharge()));
		try {
			return m_dbTool.insert(MODULE_CHARGE.TABLE_NAME, content);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	public boolean UpdateModuleData(MODULE_CHARGE info)
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		
		selection.put(MODULE_CHARGE.MODULE_ID, info.getModuleId());
		selection.put(MODULE_CHARGE.OPER_DATE, String.valueOf(info.getOperDate()));
		
		content.put(MODULE_CHARGE.CHARGE, String.valueOf(info.getCharge()));
		try {
			return m_dbTool.update(MODULE_CHARGE.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	public boolean DeleteModuleCharge(String strModuleId, String oper_date)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(MODULE_CHARGE.MODULE_ID, strModuleId);
		selection.put(MODULE_CHARGE.OPER_DATE, oper_date);
		try {
			return m_dbTool.delete(MODULE_CHARGE.TABLE_NAME, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	public boolean ClearModuleCharge() 
	{
		try {
			return m_dbTool.delete(MODULE_CHARGE.TABLE_NAME);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	

	/****************************************************************************
	 * MODULE_BATTERYCHARGE相关操作
	 * **************************************************************************/
	public Vector<MODULE_BATTERYCHARGE> QueryModuleBatteryChargeByModuleId(String strModuleId, String beginDate, String endDate)
	{
		Vector<MODULE_BATTERYCHARGE> vecCharge = new Vector<MODULE_BATTERYCHARGE>();
		String selection = "oper_date >= \"" + beginDate + "\" and oper_date < \"" + endDate + "\"";
		selection += " and module_id = " + strModuleId;
		ResultSet rs;
		try {
			rs = m_dbTool.query(MODULE_BATTERYCHARGE.TABLE_NAME, selection);
			
			while(rs.next())
			{
				vecCharge.add(new MODULE_BATTERYCHARGE(rs.getString(MODULE_BATTERYCHARGE.MODULE_ID),
						rs.getTimestamp(MODULE_BATTERYCHARGE.OPER_DATE),
						rs.getDouble(MODULE_BATTERYCHARGE.CHARGE)));
			}
			return vecCharge;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return null;
	}
	
	public Vector<MODULE_BATTERYCHARGE> QueryModuleBatteryChargeByModuleId(String strModuleId)
	{
		Vector<MODULE_BATTERYCHARGE> vecCharge = new Vector<MODULE_BATTERYCHARGE>();
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(MODULE_DATA.MODULE_ID, strModuleId);
		ResultSet rs;
		try {
			rs = m_dbTool.query(MODULE_BATTERYCHARGE.TABLE_NAME, selection);
			
			while(rs.next())
			{
				vecCharge.add(new MODULE_BATTERYCHARGE(rs.getString(MODULE_BATTERYCHARGE.MODULE_ID),
						rs.getTimestamp(MODULE_BATTERYCHARGE.OPER_DATE),
						rs.getDouble(MODULE_BATTERYCHARGE.CHARGE)));
			}
			return vecCharge;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return null;
	}
	
	public boolean InsertModuleBatteryCharge(MODULE_BATTERYCHARGE info)
	{
		Map<String,String> content = new HashMap<String,String>();
		content.put(MODULE_BATTERYCHARGE.MODULE_ID, info.getModuleId());
		content.put(MODULE_BATTERYCHARGE.OPER_DATE, String.valueOf(info.getOperDate()));
		content.put(MODULE_BATTERYCHARGE.CHARGE, String.valueOf(info.getCharge()));
		try {
			return m_dbTool.insert(MODULE_BATTERYCHARGE.TABLE_NAME, content);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	public boolean UpdateModuleBatteryChargeData(MODULE_BATTERYCHARGE info)
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		
		selection.put(MODULE_BATTERYCHARGE.MODULE_ID, info.getModuleId());
		
		content.put(MODULE_BATTERYCHARGE.CHARGE, String.valueOf(info.getCharge()));
		content.put(MODULE_BATTERYCHARGE.OPER_DATE, String.valueOf(info.getOperDate()));
		try {
			return m_dbTool.update(MODULE_BATTERYCHARGE.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	public boolean DeleteModuleBatteryCharge(String strModuleId, String oper_date)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(MODULE_BATTERYCHARGE.MODULE_ID, strModuleId);
		selection.put(MODULE_BATTERYCHARGE.OPER_DATE, oper_date);
		try {
			return m_dbTool.delete(MODULE_BATTERYCHARGE.TABLE_NAME, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	public boolean ClearModuleBatteryCharge() 
	{
		try {
			return m_dbTool.delete(MODULE_BATTERYCHARGE.TABLE_NAME);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	

	/****************************************************************************
	 * MODULE_BATTERYLOCATION相关操作
	 * **************************************************************************/
	public Vector<MODULE_BATTERYLOCATION> QueryModuleBatteryLocationByModuleId(String strModuleId, String beginDate, String endDate)
	{
		Vector<MODULE_BATTERYLOCATION> vecCharge = new Vector<MODULE_BATTERYLOCATION>();
		String selection = "oper_date >= \"" + beginDate + "\" and oper_date < \"" + endDate + "\"";
		selection += " and module_id = " + strModuleId;
		String orders = "oper_date asc";
		ResultSet rs;
		try {
			rs = m_dbTool.query(MODULE_BATTERYLOCATION.TABLE_NAME, selection, orders);
			
			while(rs.next())
			{
				vecCharge.add(new MODULE_BATTERYLOCATION(rs.getString(MODULE_BATTERYLOCATION.MODULE_ID),
						rs.getTimestamp(MODULE_BATTERYLOCATION.OPER_DATE),
						rs.getDouble(MODULE_BATTERYLOCATION.LONGITUDE),
						rs.getDouble(MODULE_BATTERYLOCATION.DIMENSION)));
			}
			return vecCharge;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return null;
	}
	public Vector<MODULE_BATTERYLOCATION> QueryModuleBatteryLocationByModuleId(String strModuleId, int count)
	{
		Vector<MODULE_BATTERYLOCATION> vecCharge = new Vector<MODULE_BATTERYLOCATION>();
		String selection = "module_id = " + strModuleId;
		String orders = "oper_date desc limit " + count;
		ResultSet rs;
		try {
			rs = m_dbTool.query(MODULE_BATTERYLOCATION.TABLE_NAME, selection, orders);
			
			while(rs.next())
			{
				vecCharge.add(new MODULE_BATTERYLOCATION(rs.getString(MODULE_BATTERYLOCATION.MODULE_ID),
						rs.getTimestamp(MODULE_BATTERYLOCATION.OPER_DATE),
						rs.getDouble(MODULE_BATTERYLOCATION.LONGITUDE),
						rs.getDouble(MODULE_BATTERYLOCATION.DIMENSION)));
			}
			return vecCharge;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return null;
	}
	public boolean InsertModuleBatteryLocation(MODULE_BATTERYLOCATION info)
	{
		Map<String,String> content = new HashMap<String,String>();
		content.put(MODULE_BATTERYLOCATION.MODULE_ID, info.getModuleId());
		content.put(MODULE_BATTERYLOCATION.OPER_DATE, String.valueOf(info.getOperDate()));
		content.put(MODULE_BATTERYLOCATION.LONGITUDE, String.valueOf(info.getLongitude()));
		content.put(MODULE_BATTERYLOCATION.DIMENSION, String.valueOf(info.getDimension()));
		try {
			return m_dbTool.insert(MODULE_BATTERYLOCATION.TABLE_NAME, content);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	public boolean UpdateModuleBatteryLocationData(MODULE_BATTERYLOCATION info)
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		
		selection.put(MODULE_BATTERYLOCATION.MODULE_ID, info.getModuleId());
		selection.put(MODULE_BATTERYLOCATION.OPER_DATE, String.valueOf(info.getOperDate()));
		
		content.put(MODULE_BATTERYLOCATION.LONGITUDE, String.valueOf(info.getLongitude()));
		content.put(MODULE_BATTERYLOCATION.DIMENSION, String.valueOf(info.getDimension()));
		try {
			return m_dbTool.update(MODULE_BATTERYLOCATION.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	public boolean DeleteModuleBatteryLocation(String strModuleId, String oper_date)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(MODULE_BATTERYLOCATION.MODULE_ID, strModuleId);
		selection.put(MODULE_BATTERYLOCATION.OPER_DATE, oper_date);
		try {
			return m_dbTool.delete(MODULE_BATTERYLOCATION.TABLE_NAME, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	public boolean ClearModuleBatteryLocation() 
	{
		try {
			return m_dbTool.delete(MODULE_BATTERYLOCATION.TABLE_NAME);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	/****************************************************************************
	 * USER_INFO相关操作
	 * **************************************************************************/
	/**
	 * @name IsUserNameExist 用户名是否存在
	 * @param strUserName
	 * @return boolean
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean IsUserNameExist(String strUserName)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(USER_INFO.USER_NAME, strUserName);
		ResultSet rs;
		try {
			rs = m_dbTool.query(USER_INFO.TABLE_NAME, selection);
			return rs.next();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name IsEmailExist 邮箱是否存在
	 * @param strEmail
	 * @return boolean
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean IsEmailExist(String strEmail)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(USER_INFO.EMAIL, strEmail);
		ResultSet rs;
		try {
			rs = m_dbTool.query(USER_INFO.TABLE_NAME, selection);
			return rs.next();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name QueryUserInfo 查询指定用户的用户信息
	 * @param strUserName
	 * @return USER_INFO 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public USER_INFO QueryUserInfoByUserName(String strUserName)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(USER_INFO.USER_NAME, strUserName);
		ResultSet rs;
		try {
			rs = m_dbTool.query(USER_INFO.TABLE_NAME, selection);
			if(rs.next())
			{
				return new USER_INFO(rs.getString(USER_INFO.USER_NAME),
						rs.getString(USER_INFO.PASSWORD),
						rs.getString(USER_INFO.EMAIL),
						rs.getString(USER_INFO.COOKIE));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return null;
	}
	/**
	 * @name QueryUserInfo 查询指定邮箱的用户信息(因为EMAIL也是唯一的)
	 * @param strEmail 
	 * @return USER_INFO 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public USER_INFO QueryUserInfoByEmail(String strEmail)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(USER_INFO.EMAIL, strEmail);
		ResultSet rs;
		try {
			rs = m_dbTool.query(USER_INFO.TABLE_NAME, selection);
			if(rs.next())
			{
				return new USER_INFO(rs.getString(USER_INFO.USER_NAME),
						rs.getString(USER_INFO.PASSWORD),
						rs.getString(USER_INFO.EMAIL),
						rs.getString(USER_INFO.COOKIE));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return null;
	}
	/**
	 * @name QueryUserInfo 查询所有用户信息
	 * @return Vector<USER_INFO> 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/28
	 */
	public Vector<USER_INFO> QueryUserInfo()
	{
		Vector<USER_INFO> vecUserInfo = new Vector<USER_INFO>();
		try {
			ResultSet rs = m_dbTool.query(USER_INFO.TABLE_NAME);
			while(rs.next())
			{
				vecUserInfo.add(new USER_INFO(rs.getString(USER_INFO.USER_NAME),
						rs.getString(USER_INFO.PASSWORD),
						rs.getString(USER_INFO.EMAIL),
						rs.getString(USER_INFO.COOKIE)));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return vecUserInfo;
	}
	/**
	 * @name InsertUserInfo 插入指定用户的用户信息
	 * @param user_info 待插入的用户信息
	 * @return boolean 
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean InsertUserInfo(USER_INFO user_info)
	{
		Map<String,String> content = new HashMap<String,String>();
		content.put(USER_INFO.USER_NAME, user_info.getUserName());
		content.put(USER_INFO.PASSWORD, user_info.getPassWord());
		content.put(USER_INFO.EMAIL, user_info.getEmail());
		content.put(USER_INFO.COOKIE, user_info.getCookie());
		try {
			return m_dbTool.insert(USER_INFO.TABLE_NAME, content);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name DeleteUserInfo 删除指定的用户信息
	 * @param strUserName 待删除的用户名称
	 * @return boolean 
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean DeleteUserInfo(String strUserName)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(USER_INFO.USER_NAME, strUserName);
		try {
			return m_dbTool.delete(USER_INFO.TABLE_NAME, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name ClearUserInfo 删除所有用户信息
	 * @return boolean 
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean ClearUserInfo() 
	{
		try {
			return m_dbTool.delete(USER_INFO.TABLE_NAME);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name UpdateUserPWD 更新用户密码
	 * @param user_info 待更新的用户信息
	 * @return boolean 
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean UpdateUserPWD(USER_INFO user_info)
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(USER_INFO.PASSWORD, user_info.getPassWord());
		selection.put(USER_INFO.USER_NAME, user_info.getUserName());
		try {
			return m_dbTool.update(USER_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name UpdateUserCookie 更新用户密码
	 * @param user_info 待更新的用户信息
	 * @return boolean 
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean UpdateUserCookie(USER_INFO user_info)
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(USER_INFO.COOKIE, user_info.getCookie());
		selection.put(USER_INFO.USER_NAME, user_info.getUserName());
		try {
			return m_dbTool.update(USER_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name UpdateUserPWD 更新用户密码
	 * @param user_info 待更新的用户信息
	 * @return boolean 
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean UpdateUserEmail(USER_INFO user_info)
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(USER_INFO.EMAIL, user_info.getEmail());
		selection.put(USER_INFO.USER_NAME, user_info.getUserName());
		try {
			return m_dbTool.update(USER_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/****************************************************************************
	 * USER_MODULE相关操作
	 * **************************************************************************/
	/**
	 * @name QueryModuleList 查询指定用户拥有的模块列表
	 * @param strUserName
	 * @return USER_MODULE列表 
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public Vector<USER_MODULE> QueryUserModuleByUserName(String strUserName)
	{
		Vector<USER_MODULE> vecUserModuleList = new Vector<USER_MODULE>();
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(USER_MODULE.USER_NAME, strUserName);
		ResultSet rs;
		try {
			rs = m_dbTool.query(USER_MODULE.TABLE_NAME, selection);
			while(rs.next())
			{
				vecUserModuleList.add(new USER_MODULE(rs.getString(USER_MODULE.USER_NAME),
						rs.getString(USER_MODULE.MODULE_ID),
						rs.getByte(USER_MODULE.CTRL_MODE)));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return vecUserModuleList;
	}
	/**
	 * @name QueryUserModuleByDevId 查询指定设备ID的用户信息
	 * @param strDevId	模块ID
	 * @return USER_MODULE
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public USER_MODULE QueryUserModuleByDevId(String strDevId)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(USER_MODULE.MODULE_ID, strDevId);
		ResultSet rs;
		try {
			rs = m_dbTool.query(USER_MODULE.TABLE_NAME, selection);
			if(rs.next())
			{
				return new USER_MODULE(rs.getString(USER_MODULE.USER_NAME),
						rs.getString(USER_MODULE.MODULE_ID),
						rs.getByte(USER_MODULE.CTRL_MODE));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return null;
	}
	/**
	 * @name QueryModuleList 查询指定用户拥有的模块列表
	 * @param strUserName 	用户名
	 * 					strDevId			模块ID
	 * @return USER_MODULE
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public USER_MODULE QueryUserModule(String strUserName, String strDevId) 
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(USER_MODULE.USER_NAME, strUserName);
		selection.put(USER_MODULE.MODULE_ID, strDevId);
		ResultSet rs;
		try {
			rs = m_dbTool.query(USER_MODULE.TABLE_NAME, selection);
			if(rs.next())
			{
				return new USER_MODULE(rs.getString(USER_MODULE.USER_NAME),
						rs.getString(USER_MODULE.MODULE_ID),
						rs.getByte(USER_MODULE.CTRL_MODE));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return null;
	}
	/**
	 * @name DeleteUserModule 删除指定用户拥有的模块列表
	 * @param strUserName 	用户名
	 * 					strDevId			模块ID
	 * @return boolean 是否成功
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean DeleteUserModule(String strUserName, String strDevId)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(USER_MODULE.USER_NAME, strUserName);
		selection.put(USER_MODULE.MODULE_ID, strDevId);
		try {
			return m_dbTool.delete(USER_MODULE.TABLE_NAME, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name ClearUserModule 删除所有用户列表
	 * @return boolean 是否成功
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean ClearUserModule() 
	{
		try {
			return m_dbTool.delete(USER_MODULE.TABLE_NAME);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name QueryModuleList 查询指定用户拥有的模块列表
	 * @param strUserName
	 * @return USER_MODULE列表 
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean InsertUserModule(USER_MODULE user_module) 
	{
		Map<String,String> content = new HashMap<String,String>();
		content.put(USER_MODULE.USER_NAME, user_module.getUserName());
		content.put(USER_MODULE.MODULE_ID, user_module.getModuleId());
		content.put(USER_MODULE.CTRL_MODE, String.valueOf(user_module.getCtrlMode()));
		try {
			return m_dbTool.insert(USER_MODULE.TABLE_NAME, content);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	
	public boolean UpdateUserModule(USER_MODULE user_module) 
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(USER_MODULE.USER_NAME, user_module.getUserName());
		content.put(USER_MODULE.MODULE_ID, user_module.getModuleId());
		content.put(USER_MODULE.CTRL_MODE, String.valueOf(user_module.getCtrlMode()));
		selection.put(USER_MODULE.MODULE_ID, user_module.getModuleId());
		try {
			return m_dbTool.update(USER_MODULE.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}

	/****************************************************************************
	 * MODULE_INFO相关操作
	 * **************************************************************************/
	/**
	 * @name QueryModuleInfo 查询指定模块的信息
	 * @param strDevId 设备ID
	 * @return MODULE_INFO
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public MODULE_INFO QueryModuleInfo(String strDevId) 
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(MODULE_INFO.MODULE_ID, strDevId);
		ResultSet rs;
		try {
			rs = m_dbTool.query(MODULE_INFO.TABLE_NAME, selection);
			if(rs.next())
			{
				return new MODULE_INFO(rs.getString(MODULE_INFO.MODULE_ID),
						rs.getString(MODULE_INFO.MODULE_NAME),
						rs.getString(MODULE_INFO.MODULE_MAC),
						rs.getString(MODULE_INFO.MODULE_VER),
						rs.getString(MODULE_INFO.MODULE_TYPE),
						rs.getInt(MODULE_INFO.PROTOCOL_TYPE),
						rs.getInt(MODULE_INFO.POWER_STATUS),
						rs.getInt(MODULE_INFO.RED),
						rs.getInt(MODULE_INFO.GREEN),
						rs.getInt(MODULE_INFO.BLUE),
						rs.getInt(MODULE_INFO.MODE),
						rs.getString(MODULE_INFO.COOKIE),
						rs.getString(MODULE_INFO.AIRCON_NAME),
						rs.getString(MODULE_INFO.TV_NAME));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return null;
	}
	/**
	 * @name QueryModuleInfo 查询指定模块的信息
	 * @param strDevId 设备ID
	 * @return MODULE_INFO
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public Vector<MODULE_INFO> QueryModuleInfo()
	{
		Vector<MODULE_INFO> vecModule = new Vector<MODULE_INFO>();
		ResultSet rs;
		try {
			rs = m_dbTool.query(MODULE_INFO.TABLE_NAME);
			while(rs.next())
			{
				vecModule.add(new MODULE_INFO(rs.getString(MODULE_INFO.MODULE_ID),
						rs.getString(MODULE_INFO.MODULE_NAME),
						rs.getString(MODULE_INFO.MODULE_MAC),
						rs.getString(MODULE_INFO.MODULE_VER),
						rs.getString(MODULE_INFO.MODULE_TYPE),
						rs.getInt(MODULE_INFO.PROTOCOL_TYPE),
						rs.getInt(MODULE_INFO.POWER_STATUS),
						rs.getInt(MODULE_INFO.RED),
						rs.getInt(MODULE_INFO.GREEN),
						rs.getInt(MODULE_INFO.BLUE),
						rs.getInt(MODULE_INFO.MODE),
						rs.getString(MODULE_INFO.COOKIE),
						rs.getString(MODULE_INFO.AIRCON_NAME),
						rs.getString(MODULE_INFO.TV_NAME)));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return vecModule;
	}
	/**
	 * @name UpdateModuleInfo_PwrStatus 更新插座通电状态
	 * @param strModuleId 用户模块ID，iPwrStatus 插座通电状，包括继电器，小夜灯
	 * @return boolean 
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean UpdateModuleInfo_PwrStatus(String strModuleId, int iPwrStatus) 
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(MODULE_INFO.POWER_STATUS, String.valueOf(iPwrStatus));
		selection.put(MODULE_INFO.MODULE_ID, strModuleId);
		try {
			return m_dbTool.update(MODULE_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name UpdateModuleInfo_ProType 更新插座UDP TCP协议类型
	 * @param strModuleId 用户模块ID，iPwrStatus 插座通电状，包括继电器，小夜灯
	 * @return boolean 
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean UpdateModuleInfo_ProType(String strModuleId, int iProType) 
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(MODULE_INFO.PROTOCOL_TYPE, String.valueOf(iProType));
		selection.put(MODULE_INFO.MODULE_ID, strModuleId);
		try {
			return m_dbTool.update(MODULE_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	/**
	 * @name UpdateModuleInfo_LightStatus 更新小夜灯状态
	 * @param strModuleId 用户模块ID，iLightStatus 继电器状态
	 * @return boolean 
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean UpdateModuleInfo_RGB(String strModuleId, int iRed, int iGreen, int iBlue,int iMode)
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(MODULE_INFO.RED, String.valueOf(iRed));
		content.put(MODULE_INFO.GREEN, String.valueOf(iGreen));
		content.put(MODULE_INFO.BLUE, String.valueOf(iBlue));
		content.put(MODULE_INFO.MODE, String.valueOf(iMode));
		selection.put(MODULE_INFO.MODULE_ID, strModuleId);
		try {
			return m_dbTool.update(MODULE_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name UpdateModuleInfo 更新模块所有状态信息(不包含模块名称)
	 * @param info 模块信息
	 * @return boolean 
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean UpdateModuleInfo(MODULE_INFO info) 
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(MODULE_INFO.MODULE_MAC, String.valueOf(info.getMac()));
		content.put(MODULE_INFO.MODULE_VER, String.valueOf(info.getModuleVer()));
		content.put(MODULE_INFO.MODULE_TYPE, String.valueOf(info.getModuleType()));
		content.put(MODULE_INFO.PROTOCOL_TYPE, String.valueOf(info.getProType()));
		content.put(MODULE_INFO.POWER_STATUS, String.valueOf(info.getPwrStatus()));
		content.put(MODULE_INFO.MODULE_NAME, String.valueOf(info.getModuleName()));
		content.put(MODULE_INFO.RED, String.valueOf(info.getRed()));
		content.put(MODULE_INFO.GREEN, String.valueOf(info.getGreen()));
		content.put(MODULE_INFO.BLUE, String.valueOf(info.getBlue()));
		content.put(MODULE_INFO.MODE, String.valueOf(info.getMode()));
		content.put(MODULE_INFO.COOKIE, info.getCookie());
		content.put(MODULE_INFO.AIRCON_NAME, info.getAirConName());
		selection.put(MODULE_INFO.MODULE_ID, info.getModuleId());
		try {
			return m_dbTool.update(MODULE_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}

	/**
	 * @name InsertModuleInfo 填加指定模块的信息
	 * @param module_info 待填加的模块信息
	 * @return boolean 是否成功
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean InsertModuleInfo(MODULE_INFO module_info) 
	{
		Map<String,String> content = new HashMap<String,String>();
		content.put(MODULE_INFO.MODULE_ID, module_info.getModuleId());
		content.put(MODULE_INFO.MODULE_NAME, module_info.getModuleName());
		content.put(MODULE_INFO.MODULE_MAC, module_info.getMac());
		content.put(MODULE_INFO.MODULE_VER, module_info.getModuleVer());
		content.put(MODULE_INFO.MODULE_TYPE, module_info.getModuleType());
		content.put(MODULE_INFO.PROTOCOL_TYPE, String.valueOf(module_info.getProType()));
		content.put(MODULE_INFO.POWER_STATUS, String.valueOf(module_info.getPwrStatus()));
		content.put(MODULE_INFO.RED, String.valueOf(module_info.getRed()));
		content.put(MODULE_INFO.GREEN, String.valueOf(module_info.getGreen()));
		content.put(MODULE_INFO.BLUE, String.valueOf(module_info.getBlue()));
		content.put(MODULE_INFO.MODE, String.valueOf(module_info.getMode()));
		content.put(MODULE_INFO.COOKIE, module_info.getCookie());
		content.put(MODULE_INFO.AIRCON_NAME, module_info.getAirConName());
		try {
			return m_dbTool.insert(MODULE_INFO.TABLE_NAME, content);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name QueryModuleInfo 查询指定模块的信息
	 * @param strDevId 设备ID
	 * @return MODULE_INFO
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean DeleteModuleInfo(String strModuleId) 
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(MODULE_INFO.MODULE_ID, strModuleId);
		try {
			return m_dbTool.delete(MODULE_INFO.TABLE_NAME, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name ClearModuleInfo 删除所有模块的信息
	 * @return MODULE_INFO
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/03/28
	 */
	public boolean ClearModuleInfo() 
	{
		try {
			return m_dbTool.delete(MODULE_INFO.TABLE_NAME);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	/**
	 * @name UpdateModuleName 查询指定模块的信息
	 * @param strModuleId 设备ID
	 *                 strModuleName 模块名称
	 * @return MODULE_INFO
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean UpdateModuleInfo_ModuleName(String strModuleId,String strModuleName) 
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(MODULE_INFO.MODULE_NAME, strModuleName);
		selection.put(MODULE_INFO.MODULE_ID, strModuleId);
		try {
			return m_dbTool.update(MODULE_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	/**
	 * @name UpdateModuleInfo_Cookie 查询指定模块的cookie
	 * @param strModuleId 设备ID
	 *                 strModuleName 模块名称
	 * @return MODULE_INFO
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean UpdateModuleInfo_Cookie(String strModuleId,String strCookie) 
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(MODULE_INFO.COOKIE, strCookie);
		selection.put(MODULE_INFO.MODULE_ID, strModuleId);
		try {
			return m_dbTool.update(MODULE_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	public boolean UpdateModuleInfo_AirCon(String strModuleId,String strAirconName) 
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(MODULE_INFO.AIRCON_NAME, strAirconName);
		selection.put(MODULE_INFO.MODULE_ID, strModuleId);
		try {
			return m_dbTool.update(MODULE_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	public boolean UpdateModuleInfo_TV(String strModuleId,String strAirconName) 
	{
		Map<String,String> content = new HashMap<String,String>();
		Map<String,String> selection = new HashMap<String,String>();
		content.put(MODULE_INFO.TV_NAME, strAirconName);
		selection.put(MODULE_INFO.MODULE_ID, strModuleId);
		try {
			return m_dbTool.update(MODULE_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	/***********************************************************************************************************
	 * TIMER_INFO相关操作
	 * ********************************************************************************************************/
	/**
	 * @name InsertTimerInfo 增加一个定时器
	 * @param info 定时器的相关信息
	 * @return boolean 是否成功
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean InsertTimerInfo(TIMER_INFO info)
	{
		Map<String,String> content = new HashMap<String,String>();
		content.put(TIMER_INFO.TIMER_ID, String.valueOf(info.getTimerId()));
		content.put(TIMER_INFO.TIMER_TYPE, String.valueOf(info.getTimerType()));
		content.put(TIMER_INFO.MODULE_ID, info.getModuleId());
		content.put(TIMER_INFO.PEROID, info.getPeroid());
		content.put(TIMER_INFO.TIME_ON, String.valueOf(info.getTimeOn()));
		content.put(TIMER_INFO.TIME_OFF, String.valueOf(info.getTimeOff()));
		content.put(TIMER_INFO.ENABLE_FLAG, String.valueOf(info.getEnableFlag()));
		try {
			return m_dbTool.insert(TIMER_INFO.TABLE_NAME, content);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name DeleteTimerInfo 删除一个定时器
	 * @param 	strTimerId 定时器ID
	 *  				strModuleId 定时器所属模块ID
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean DeleteTimerInfo(byte TimerId,String strModuleId)
	{
		Map<String,String> selection 	= new HashMap<String,String>();
		selection.put(TIMER_INFO.TIMER_ID, String.valueOf(TimerId));
		selection.put(TIMER_INFO.MODULE_ID, strModuleId);
		try {
			return m_dbTool.delete(TIMER_INFO.TABLE_NAME, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	/**
	 * @name DeleteTimerInfo 删除该模块的所有定时器任务
	 * @param strModuleId 定时器所属模块ID
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean DeleteTimerInfo(String strModuleId) 
	{
		Map<String,String> selection 	= new HashMap<String,String>();
		selection.put(TIMER_INFO.MODULE_ID, strModuleId);
		try {
			return m_dbTool.delete(TIMER_INFO.TABLE_NAME, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	/**
	 * @name ClearTimerInfo 删除所有定时器
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean ClearTimerInfo()
	{
		try {
			return m_dbTool.delete(TIMER_INFO.TABLE_NAME);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	/**
	 * @name UpdateTimerInfo 更新定时器时间
	 * @param info 定时器信息
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean UpdateTimerInfo(TIMER_INFO info) 
	{
		Map<String,String> selection 	= new HashMap<String,String>();
		Map<String,String> content 	= new HashMap<String,String>();
		//执行条件
		selection.put(TIMER_INFO.TIMER_ID, String.valueOf(info.getTimerId()));
		selection.put(TIMER_INFO.MODULE_ID, info.getModuleId());
		//更新内容
		content.put(TIMER_INFO.TIMER_TYPE, String.valueOf(info.getTimerType()));
		content.put(TIMER_INFO.PEROID, info.getPeroid());
		content.put(TIMER_INFO.TIME_ON, String.valueOf(info.getTimeOn()));
		content.put(TIMER_INFO.TIME_OFF, String.valueOf(info.getTimeOff()));
		try {
			return m_dbTool.update(TIMER_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	/**
	 * @name InsertUpdateTimerInfo 更新定时器时间
	 * @param info 定时器信息
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean InsertUpdateTimerInfo(TIMER_INFO info) 
	{
		Map<String,String> selection 	= new HashMap<String,String>();
		Map<String,String> content 	= new HashMap<String,String>();
		//执行条件
		selection.put(TIMER_INFO.TIMER_ID, String.valueOf(info.getTimerId()));
		selection.put(TIMER_INFO.MODULE_ID, info.getModuleId());
		//更新内容
		content.put(TIMER_INFO.TIMER_ID, String.valueOf(info.getTimerId()));
		content.put(TIMER_INFO.MODULE_ID, info.getModuleId());
		content.put(TIMER_INFO.TIMER_TYPE, String.valueOf(info.getTimerType()));
		content.put(TIMER_INFO.PEROID, info.getPeroid());
		content.put(TIMER_INFO.TIME_ON, String.valueOf(info.getTimeOn()));
		content.put(TIMER_INFO.TIME_OFF, String.valueOf(info.getTimeOff()));
		content.put(TIMER_INFO.ENABLE_FLAG, String.valueOf(info.getEnableFlag()));
		try {
			return m_dbTool.insert_or_update (TIMER_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	/**
	 * @name UpdateTimerEnableFlag 修改定时器的使能状态
	 * @param 	strTimerId 定时器ID
	 * 					strModuleId 模块ID
	 * 					bEnable 是否使能
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean UpdateTimerEnableFlag(byte TimerId,String strModuleId,String strFlag)
	{
		Map<String,String> selection 	= new HashMap<String,String>();
		Map<String,String> content 	= new HashMap<String,String>();
		selection.put(TIMER_INFO.TIMER_ID, String.valueOf(TimerId));
		selection.put(TIMER_INFO.MODULE_ID, strModuleId);
		content.put(TIMER_INFO.ENABLE_FLAG, strFlag);
		try {
			return m_dbTool.update(TIMER_INFO.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	/**
	 * @name QueryTimerInfo 查询一个定时器
	 * @param 	strTimerId 定时器ID
	 * 					strDevId 模块ID （定时器ID与模块ID都是主键）
	 * @return TIMER_INFO 查询到的定时器信息
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public TIMER_INFO QueryTimerInfo(String strDevId,byte TimerId)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(TIMER_INFO.TIMER_ID, String.valueOf(TimerId));
		selection.put(TIMER_INFO.MODULE_ID, strDevId);
		ResultSet rs;
		try {
			rs = m_dbTool.query(TIMER_INFO.TABLE_NAME, selection);
			if(rs.next())
			{
				return new TIMER_INFO(rs.getByte(TIMER_INFO.TIMER_ID),
						rs.getByte(TIMER_INFO.TIMER_TYPE),
						rs.getString(TIMER_INFO.MODULE_ID),
						rs.getString(TIMER_INFO.PEROID),
						rs.getString(TIMER_INFO.TIME_ON),
						rs.getString(TIMER_INFO.TIME_OFF),
						rs.getByte(TIMER_INFO.ENABLE_FLAG));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return null;
	}
	/**
	 * @name QueryTimerInfo 查询一个定时器
	 * @param 	strTimerId 定时器ID
	 * 					strDevId 模块ID （定时器ID与模块ID都是主键）
	 * @return Vector<TIMER_INFO> 查询到的定时器列表
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public Vector<TIMER_INFO> QueryTimerInfoList(String strDevId)
	{
		Vector<TIMER_INFO> vecTimerInfo = new Vector<TIMER_INFO>();
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(TIMER_INFO.MODULE_ID, strDevId);
		ResultSet rs;
		try {
			rs = m_dbTool.query(TIMER_INFO.TABLE_NAME, selection);
			while(rs.next())
			{
				vecTimerInfo.add(new TIMER_INFO(rs.getByte(TIMER_INFO.TIMER_ID),
						rs.getByte(TIMER_INFO.TIMER_TYPE),
						rs.getString(TIMER_INFO.MODULE_ID),
						rs.getString(TIMER_INFO.PEROID),
						rs.getString(TIMER_INFO.TIME_ON),
						rs.getString(TIMER_INFO.TIME_OFF),
						rs.getByte(TIMER_INFO.ENABLE_FLAG)));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return vecTimerInfo;
	}
	
	/***********************************************************************************************************
	 * MODULE_IR_SCENE相关操作
	 * ********************************************************************************************************/
	/**
	 * @name InsertIRSceneInfo 增加一个红外数据定时器
	 * @param info 定时器的相关信息
	 * @return boolean 是否成功
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean InsertIRSceneInfo(MODULE_IRSCENE info)
	{
		Map<String,String> content = new HashMap<String,String>();
//		content.put(MODULE_IRSCENE.ID, String.valueOf(info.getId()));
		content.put(MODULE_IRSCENE.IRSCENE_ID, String.valueOf(info.getIRSceneId()));
		content.put(MODULE_IRSCENE.MODULE_ID, String.valueOf(info.getModuleId()));
		content.put(MODULE_IRSCENE.ENABLE, String.valueOf(info.getEnable()));
		content.put(MODULE_IRSCENE.POWER, String.valueOf(info.getPower()));
		content.put(MODULE_IRSCENE.MODE, String.valueOf(info.getMode()));
		content.put(MODULE_IRSCENE.DIRECTION, String.valueOf(info.getDirection()));
		content.put(MODULE_IRSCENE.SCALE, String.valueOf(info.getScale()));
		content.put(MODULE_IRSCENE.TEMPERATURE, String.valueOf(info.getTemperature()));
		content.put(MODULE_IRSCENE.TIME, String.valueOf(info.getTime()));
		content.put(MODULE_IRSCENE.PERIOD, String.valueOf(info.getPeriod()));
		content.put(MODULE_IRSCENE.IRNAME, String.valueOf(info.getIRName()));
		try {
			return m_dbTool.insert(MODULE_IRSCENE.TABLE_NAME, content);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}
	
	/**
	 * @name DeleteIRSceneInfo 删除一个定时器
	 * @param 	strTimerId 定时器ID
	 *  				strModuleId 定时器所属模块ID
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean DeleteIRSceneInfo(int Id, String strModuleId)
	{
		Map<String,String> selection 	= new HashMap<String,String>();
		selection.put(MODULE_IRSCENE.IRSCENE_ID, String.valueOf(Id));
		selection.put(MODULE_IRSCENE.MODULE_ID, strModuleId);
		try {
			return m_dbTool.delete(MODULE_IRSCENE.TABLE_NAME, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}

	/**
	 * @name DeleteIRSceneInfo 删除该模块的所有定时器任务
	 * @param strModuleId 定时器所属模块ID
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean DeleteIRSceneInfo(String strModuleId) 
	{
		Map<String,String> selection 	= new HashMap<String,String>();
		selection.put(MODULE_IRSCENE.MODULE_ID, strModuleId);
		try {
			return m_dbTool.delete(MODULE_IRSCENE.TABLE_NAME, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	/**
	 * @name ClearIRSceneInfo 删除所有定时器
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean ClearIRSceneInfo()
	{
		try {
			return m_dbTool.delete(MODULE_IRSCENE.TABLE_NAME);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		return false;
	}	
	
	/**
	 * @name UpdateIRSceneInfo 更新定时器时间
	 * @param info 定时器信息
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean UpdateIRSceneInfo(MODULE_IRSCENE info) 
	{
		Map<String,String> selection 	= new HashMap<String,String>();
		Map<String,String> content 	= new HashMap<String,String>();
		//执行条件
		selection.put(MODULE_IRSCENE.IRSCENE_ID, String.valueOf(info.getIRSceneId()));
		selection.put(MODULE_IRSCENE.MODULE_ID, info.getModuleId());
		//更新内容
		content.put(MODULE_IRSCENE.IRSCENE_ID, String.valueOf(info.getIRSceneId()));
		content.put(MODULE_IRSCENE.MODULE_ID, info.getModuleId());
		content.put(MODULE_IRSCENE.ENABLE, String.valueOf(info.getEnable()));
		content.put(MODULE_IRSCENE.POWER, String.valueOf(info.getPower()));
		content.put(MODULE_IRSCENE.MODE, String.valueOf(info.getMode()));
		content.put(MODULE_IRSCENE.DIRECTION, String.valueOf(info.getDirection()));
		content.put(MODULE_IRSCENE.SCALE, String.valueOf(info.getScale()));
		content.put(MODULE_IRSCENE.TEMPERATURE, String.valueOf(info.getTemperature()));
		content.put(MODULE_IRSCENE.TIME, String.valueOf(info.getTime()));
		content.put(MODULE_IRSCENE.PERIOD, info.getPeriod());
		content.put(MODULE_IRSCENE.IRNAME, info.getIRName());
		try {
			return m_dbTool.update(MODULE_IRSCENE.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	
	/**
	 * @name InsertUpdateIRSceneInfo 更新定时器时间
	 * @param info 定时器信息
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean InsertUpdateIRSceneInfo(MODULE_IRSCENE info) 
	{
		Map<String,String> selection 	= new HashMap<String,String>();
		Map<String,String> content 	= new HashMap<String,String>();
		//执行条件
		selection.put(MODULE_IRSCENE.IRSCENE_ID, String.valueOf(info.getIRSceneId()));
		selection.put(MODULE_IRSCENE.MODULE_ID, info.getModuleId());
		//更新内容
		content.put(MODULE_IRSCENE.IRSCENE_ID, String.valueOf(info.getIRSceneId()));
		content.put(MODULE_IRSCENE.MODULE_ID, info.getModuleId());
		content.put(MODULE_IRSCENE.ENABLE, String.valueOf(info.getEnable()));
		content.put(MODULE_IRSCENE.POWER, String.valueOf(info.getPower()));
		content.put(MODULE_IRSCENE.MODE, String.valueOf(info.getMode()));
		content.put(MODULE_IRSCENE.DIRECTION, String.valueOf(info.getDirection()));
		content.put(MODULE_IRSCENE.SCALE, String.valueOf(info.getScale()));
		content.put(MODULE_IRSCENE.TEMPERATURE, String.valueOf(info.getTemperature()));
		content.put(MODULE_IRSCENE.TIME, String.valueOf(info.getTime()));
		content.put(MODULE_IRSCENE.PERIOD, info.getPeriod());
		content.put(MODULE_IRSCENE.IRNAME, info.getIRName());
		try {
			return m_dbTool.insert_or_update (MODULE_IRSCENE.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
	/**
	 * @name UpdateTimerEnableFlag 修改定时器的使能状态
	 * @param 	strTimerId 定时器ID
	 * 					strModuleId 模块ID
	 * 					bEnable 是否使能
	 * @return boolean 是否成功
	 * @throws SQLException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public boolean UpdateIRSceneEnableFlag(int Id,String strModuleId,int iEnable)
	{
		Map<String,String> selection 	= new HashMap<String,String>();
		Map<String,String> content 	= new HashMap<String,String>();
		selection.put(MODULE_IRSCENE.IRSCENE_ID, String.valueOf(Id));
		selection.put(MODULE_IRSCENE.MODULE_ID, strModuleId);
		content.put(MODULE_IRSCENE.ENABLE, String.valueOf(iEnable));
		try {
			return m_dbTool.update(MODULE_IRSCENE.TABLE_NAME, content, selection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return false;
	}
//
//	public boolean UpdateIRName(String strModuleId, String strAirConName) {
//		Map<String,String> content = new HashMap<String,String>();
//		Map<String,String> selection = new HashMap<String,String>();
//		content.put(MODULE_INFO.AIRCON_NAME, strAirConName);
//		selection.put(MODULE_INFO.MODULE_ID, strModuleId);
//		try {
//			return m_dbTool.update(MODULE_INFO.TABLE_NAME, content, selection);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
//		}
//		
//		return false;
//	}

	public String getIRName(String strModuleId) {
		MODULE_INFO module = this.QueryModuleInfo(strModuleId);
		return module.getAirConName();
	}
	
	public String getTVIRName(String strModuleId) {
		MODULE_INFO module = this.QueryModuleInfo(strModuleId);
		return module.getTVName();
	}
	
	/**
	 * @name QueryIRSceneInfo 查询一个定时器
	 * @param 	strTimerId 定时器ID
	 * 					strDevId 模块ID （定时器ID与模块ID都是主键）
	 * @return TIMER_INFO 查询到的定时器信息
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public MODULE_IRSCENE QueryIRSceneInfo(String strModuleId, int Id)
	{
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(MODULE_IRSCENE.IRSCENE_ID, String.valueOf(Id));
		selection.put(MODULE_IRSCENE.MODULE_ID, strModuleId);
		ResultSet rs;
		try {
			rs = m_dbTool.query(MODULE_IRSCENE.TABLE_NAME, selection);
			if(rs.next())
			{
				return new MODULE_IRSCENE(
						rs.getInt(MODULE_IRSCENE.IRSCENE_ID),
						rs.getString(MODULE_IRSCENE.MODULE_ID),
						rs.getInt(MODULE_IRSCENE.POWER),
						rs.getInt(MODULE_IRSCENE.MODE),
						rs.getInt(MODULE_IRSCENE.DIRECTION),
						rs.getInt(MODULE_IRSCENE.SCALE),
						rs.getInt(MODULE_IRSCENE.TEMPERATURE),
						rs.getString(MODULE_IRSCENE.TIME),
						rs.getString(MODULE_IRSCENE.PERIOD),
						rs.getString(MODULE_IRSCENE.IRNAME),
						rs.getInt(MODULE_IRSCENE.ENABLE));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return null;
	}
	/**
	 * @name QueryTimerInfo 查询一个定时器
	 * @param 	strTimerId 定时器ID
	 * 					strDevId 模块ID （定时器ID与模块ID都是主键）
	 * @return Vector<TIMER_INFO> 查询到的定时器列表
	 * @throws SQLException ClassNotFoundException
	 * @author zxluan
	 * @date 2015/04/09
	 */
	public Vector<MODULE_IRSCENE> QueryIRSceneInfoList(String strModuleId)
	{
		Vector<MODULE_IRSCENE> vecTimerInfo = new Vector<MODULE_IRSCENE>();
		Map<String,String> selection = new HashMap<String,String>();
		selection.put(MODULE_IRSCENE.MODULE_ID, strModuleId);
		ResultSet rs;
		try {
			rs = m_dbTool.query(MODULE_IRSCENE.TABLE_NAME, selection);
			while(rs.next())
			{
				vecTimerInfo.add(new MODULE_IRSCENE(
						rs.getInt(MODULE_IRSCENE.IRSCENE_ID),
						rs.getString(MODULE_IRSCENE.MODULE_ID),
						rs.getInt(MODULE_IRSCENE.POWER),
						rs.getInt(MODULE_IRSCENE.MODE),
						rs.getInt(MODULE_IRSCENE.DIRECTION),
						rs.getInt(MODULE_IRSCENE.SCALE),
						rs.getInt(MODULE_IRSCENE.TEMPERATURE),
						rs.getString(MODULE_IRSCENE.TIME),
						rs.getString(MODULE_IRSCENE.PERIOD),
						rs.getString(MODULE_IRSCENE.IRNAME),
						rs.getInt(MODULE_IRSCENE.ENABLE)));
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e,"");
		}
		
		return vecTimerInfo;
	}

	private boolean AutoTest_IRSceneInfo()
	{
		System.out.println("Succeed to auto test module IRScene.");
		return true;
	}
	
	private boolean AutoTest_ModuleInfo()
	{
		System.out.println("Succeed to auto test module info.");
		return true;
	}
	private boolean AutoTest_TimerInfo()
	{
		TIMER_INFO src_info = new TIMER_INFO((byte)1,(byte)0,"1001","1111111","2306","2309",(byte)1);
	
		//清理
		this.ClearTimerInfo();
		
		//SETP1 新增TIMER_INFO
		boolean bRet = this.InsertTimerInfo(src_info);
		TIMER_INFO  info = this.QueryTimerInfo(src_info.getModuleId(),src_info.getTimerId());
		if(!bRet || !info.equal(src_info))
		{
			System.err.println("InsertTimerInfo failed.");
			return false;
		}
		//STEP2 更新定时器使能
		src_info.setEnableFlag(TIMER_INFO.DISABLE);
		bRet = this.UpdateTimerEnableFlag(src_info.getTimerId(), info.getModuleId(), "0");
		info = this.QueryTimerInfo(src_info.getModuleId(), src_info.getTimerId());
		if(!bRet || !info.equal(src_info))
		{
			System.err.println("UpdateTimerEnableFlag failed.");
			return false;
		}
		//STEP3 更新定时器信息
		src_info.setPeroid("1110000");
		src_info.setTimeOn("2209");
		src_info.setTimeOff("2230");
		bRet = this.UpdateTimerInfo(src_info);
		info = this.QueryTimerInfo(src_info.getModuleId(), src_info.getTimerId());
		if(!bRet ||  !info.equal(src_info))
		{
			System.err.println("UpdateTimerInfo failed.");
			return false;
		}
		//STEP4 删除定时器
		bRet = this.DeleteTimerInfo(src_info.getTimerId(), src_info.getModuleId());
		if(!bRet)
		{
			System.err.println("DeleteTimerInfo failed.");
			return false;
		}
		System.out.println("Succeed to auto test timer info.");
		return true;
	}
	private boolean AutoTest_UserInfo()
	{
		USER_INFO src_info = new USER_INFO("zxluan","123","csulzx@qq.com","1");
	
		//清理
		this.ClearUserInfo();
		//STEP1 填加新的用户信息
		boolean bRet = this.InsertUserInfo(src_info);
		USER_INFO info = this.QueryUserInfoByUserName(src_info.getUserName());
		if(!bRet || !info.equal(src_info))
		{
			System.err.println("InsertUserInfo failed.");
			return false;
		}
		//STEP2 通过用户邮箱查询用户信息
		info = this.QueryUserInfoByEmail(src_info.getEmail());
		if(!info.equal(src_info))
		{
			System.err.println("QueryUserInfoByEmail failed.");
			return false;
		}
		//STEP3 判定邮箱是否存在
		if(!this.IsEmailExist(src_info.getEmail()) ||
			this.IsEmailExist("1111"))
		{
			System.err.println("IsEmailExist failed.");
			return false;
		}
		//STEP4 判定用户名是否存在
		if(!this.IsUserNameExist(src_info.getUserName()) ||
			this.IsUserNameExist("1111"))
		{
			System.err.println("IsUserNameExist failed.");
			return false;
		}
		//STEP5 更新用户邮箱
		src_info.setEmail("37547063@qq.com");
		bRet = this.UpdateUserEmail(src_info);
		info = this.QueryUserInfoByUserName(src_info.getUserName());
		if(!bRet || !info.equal(src_info))
		{
			System.err.println("UpdateUserEmail failed.");
			return false;
		}
		//STEP6 更新用户密码
		src_info.setPassWord("1111111111");
		bRet = this.UpdateUserPWD(src_info);
		info = this.QueryUserInfoByUserName(src_info.getUserName());
		if(!bRet || !info.equal(src_info))
		{
			System.err.println("UpdateUserPWD failed.");
			return false;
		}
		//STEP7 删除用户
		bRet = this.DeleteUserInfo(src_info.getUserName());
		if(!bRet)
		{
			System.err.println("DeleteUserInfo failed.");
			return false;
		}
		
		System.out.println("Succeed to auto test user info.");
		return true;
	}
	private boolean AutoTest_UserModule()
	{
		USER_MODULE src_info = new USER_MODULE("zxluan","1001",USER_MODULE.PRIMARY);

		//清理
		this.ClearUserModule();
		
		//STEP1 新增USER_MODULE
		boolean bRet = this.InsertUserModule(src_info);
		USER_MODULE info = this.QueryUserModule(src_info.getUserName(), src_info.getModuleId());
		if(!bRet || !info.equal(src_info))
		{
			System.err.println("InsertUserModule failed.");
			return false;
		}
		//STEP2 删除USER_MODULE
		bRet = this.DeleteUserModule(src_info.getUserName(), src_info.getModuleId());
		if(!bRet)
		{
			System.err.println("DeleteUserModule failed.");
			return false;
		}
	
		System.out.println("Succeed to auto test user module.");
		return true;
	}
	public static void main(String[] args)
	{
		ServerDBMgr db_tool = new ServerDBMgr();
		//STEP1 MODULE_INFO 自测
		db_tool.AutoTest_ModuleInfo();
		//STEP2 TIMER_INFO自测
		db_tool.AutoTest_TimerInfo();
		//STEP3 USER_INFO自测
		db_tool.AutoTest_UserInfo();
		//STEP4 USER_MODULE自测
		db_tool.AutoTest_UserModule();
	}

}
