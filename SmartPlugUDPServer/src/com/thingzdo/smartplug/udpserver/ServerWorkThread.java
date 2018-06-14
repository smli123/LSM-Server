package com.thingzdo.smartplug.udpserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.Function.AddModuleMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.AppLogOutMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.AppLoginMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.AppPassThroughMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.AppRemotePrintMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.AppUpgradeStartMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.DeleteModuleMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.EnableEnergeHandle;
import com.thingzdo.smartplug.udpserver.Function.FindPwdHandle;
import com.thingzdo.smartplug.udpserver.Function.IRAddSceneMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.IRDelSceneMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.IREnableSceneMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.IRModifySceneMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.IRQuerySceneMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.IRQueryTVIRDataHandle;
import com.thingzdo.smartplug.udpserver.Function.IRStartSceneMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModEmailMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModPwdMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModifyPlugNameHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleALEDCtrlMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleAddTimerMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleAirConServerCtrlMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleAirConCtrlMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleBack2APCtrlMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleBellOnMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleCurtainCtrlMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleDelTimerMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleEnableIRTimerMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleHeartBeatTask;
import com.thingzdo.smartplug.udpserver.Function.ModuleHeartMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleLightRGBMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleLogFileMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleLoginHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleModNameMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleModPlugMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleModTimerMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModulePowerCtrlMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleRecvFileEndMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleRecvFileSendMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleRecvFileStartMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleSetTcpUdpMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleSetTimerEnableFlagMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleTVServerCtrlMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.ModuleWindowCtrlMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.NotifyBatteryEnergeHandle;
import com.thingzdo.smartplug.udpserver.Function.NotifyBatteryLocationHandle;
import com.thingzdo.smartplug.udpserver.Function.NotifyEnergeHandle;
import com.thingzdo.smartplug.udpserver.Function.NotifyKettleStatusHandle;
import com.thingzdo.smartplug.udpserver.Function.NotifyCurtainStatusHandle;
import com.thingzdo.smartplug.udpserver.Function.NotifyPowerStatusHandle;
import com.thingzdo.smartplug.udpserver.Function.QueryAllModuleInfoHandle;
import com.thingzdo.smartplug.udpserver.Function.QueryBatteryEnergeHandle;
import com.thingzdo.smartplug.udpserver.Function.QueryBatteryLocationHandle;
import com.thingzdo.smartplug.udpserver.Function.QueryChargeHandle;
import com.thingzdo.smartplug.udpserver.Function.QueryGonglvHandle;
import com.thingzdo.smartplug.udpserver.Function.QueryNetIPHandle;
import com.thingzdo.smartplug.udpserver.Function.TransmitHearBeatMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.TransmitTransMsgHandle;
import com.thingzdo.smartplug.udpserver.Function.IRQueryIRDataHandle;
import com.thingzdo.smartplug.udpserver.Function.UpgradeEndRspHandle;
import com.thingzdo.smartplug.udpserver.Function.UpgradeReEndRspHandle;
import com.thingzdo.smartplug.udpserver.Function.UpgradeReSendRespHandle;
import com.thingzdo.smartplug.udpserver.Function.UpgradeSendRespHandle;
import com.thingzdo.smartplug.udpserver.Function.UpgradeStartRespHandle;
import com.thingzdo.smartplug.udpserver.Function.UserRegisterMsgHandle;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerParamConfiger;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class ServerWorkThread  implements Runnable{
	private static Map<String,ConnectInfo> m_AppIPMap 		= new HashMap<String,ConnectInfo>();
	private static Map<String,ConnectInfo> m_ModuleIPMap 	= new HashMap<String,ConnectInfo>();
	private static Map<String,ConnectInfo> m_ModuleTransmitMap 	= new HashMap<String,ConnectInfo>();
	private static Map<Object,Object> m_SendFuncMap 		= new HashMap<Object,Object>();
	private static DatagramPacket m_packet = null;
	private static Map<String, ModuleUpgradeOnLineMgr> m_moduleUpgradeMgrMap = new HashMap<String, ModuleUpgradeOnLineMgr>();
	private static ServerWorkThread m_thread = null;
	
	private static Map<String,Integer> m_ModuleFileNOMap 	= new HashMap<String,Integer>();
	private static Map<String,ModuleRecvFileMgr> m_ModuleRecvFileMap 	= new HashMap<String,ModuleRecvFileMgr>();
	private static Map<String,ModuleLogFileMgr> m_ModuleLogFileMap 	= new HashMap<String,ModuleLogFileMgr>();
	
	// GSON 空调红外数据
	private static List<Map<String, Object>> m_IRData = new ArrayList<Map<String, Object>>();
	private static Set<String> m_IRSet = new HashSet<String>();
	private static Map<String, String> m_IRSubId = new HashMap<String,String>();
	
	// GSON 电视红外数据
	private static List<Map<String, Object>> m_TVIRData = new ArrayList<Map<String, Object>>();
	private static Set<String> m_TVIRSet = new HashSet<String>();
	private static Map<String, String> m_TVIRSubId = new HashMap<String,String>();
	
    public static void parseJSONWithGSON_IRDATA(String jsonStr)
    {
        m_IRData.clear();
        m_IRSet.clear();
        
        JSONObject jsonObj;
        Map<String, Object> map = null;
        try
        {
            jsonObj = new JSONObject();
            JSONArray objList = JSONArray.fromObject(jsonStr);

            for (int i = 0; i < objList.size(); i++)
            {
            	JSONObject jsonItem = objList.getJSONObject(i);
            	map = new HashMap<String, Object>();
    			
    	        map.put("id", 		jsonItem.getString("id"));
    	        map.put("sub_id", 	jsonItem.getString("sub_id"));
    	        map.put("value", 	jsonItem.getString("value"));
    	        
    	        m_IRData.add(map);
    	        m_IRSet.add(jsonItem.getString("id"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
	
	// 读取红外数据值
	public static String ReadCurrentAR(String id, String sub_id) {
		for (int i = 0; i < m_IRData.size(); i++) {
			if (m_IRData.get(i).get("id").toString().equals(id) && m_IRData.get(i).get("sub_id").toString().equals(sub_id)) {
				return m_IRData.get(i).get("value").toString();
			}
		}
		return null;
	}

	public static String getIRSet() {
		String str = m_IRSet.toString();
		str = str.replace("[", "");
		str = str.replace("]", "");
		str = str.replace(", ", "@");
		return str;
	}
	

    public static void parseJSONWithGSON_TVIRDATA(String jsonStr)
    {
        m_TVIRData.clear();
        m_TVIRSet.clear();
        
        JSONObject jsonObj;
        Map<String, Object> map = null;
        try
        {
            jsonObj = new JSONObject();
            JSONArray objList = JSONArray.fromObject(jsonStr);

            for (int i = 0; i < objList.size(); i++)
            {
            	JSONObject jsonItem = objList.getJSONObject(i);
            	map = new HashMap<String, Object>();
    			
    	        map.put("id", 		jsonItem.getString("id"));
    	        map.put("sub_id", 	jsonItem.getString("sub_id"));
    	        map.put("value", 	jsonItem.getString("value"));
    	        
//    	        LogWriter.WriteErrorLog(LogWriter.SELF, 
//    					String.format("TV (%s:%s)\t:%s", 
//    							jsonItem.getString("id"),
//    							jsonItem.getString("sub_id"),
//    							jsonItem.getString("value")));
    	        
    	        m_TVIRData.add(map);
    	        m_TVIRSet.add(jsonItem.getString("id"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
	
	// 读取红外数据值
	public static String ReadCurrentTVAR(String id, String sub_id) {
		for (int i = 0; i < m_IRData.size(); i++) {
			if (m_TVIRData.get(i).get("id").toString().equals(id) && m_TVIRData.get(i).get("sub_id").toString().equals(sub_id)) {
				return m_TVIRData.get(i).get("value").toString();
			}
		}
		return null;
	}

	public static String getTVIRSet() {
		String str = m_TVIRSet.toString();
		str = str.replace("[", "");
		str = str.replace("]", "");
		str = str.replace(", ", "@");
		return str;
	}
	
	public ServerWorkThread(DatagramPacket packet)
	{
		m_packet = packet;
		m_thread = this;
	}
	public static Map<String,ConnectInfo> getModuleIPMap()
	{
		return m_ModuleIPMap;
	}
	public static DatagramPacket getPacket()
	{
		return m_packet;
	}
	
	public static String getSrcIP()
	{
		return m_packet.getAddress().getHostAddress();
	}
	
	public static int getSrcPort()
	{
		return m_packet.getPort();
	}
	
	public void RegisterUpgradeMgr(int iBlockSize, int iFileBinNo, String strDevId, String strUserName, int iaux_FileBinNo, String strDeviceType) 
	{
		ModuleUpgradeOnLineMgr moduleUpgradeMgr = new ModuleUpgradeOnLineMgr(iBlockSize, iFileBinNo, strDevId, strUserName, iaux_FileBinNo, strDeviceType);
		m_moduleUpgradeMgrMap.put(strDevId, moduleUpgradeMgr);
	}
	public void UnRegisterUpgradeMgr(String strDevId)
	{
		ModuleUpgradeOnLineMgr mgr = GetModuleUpgradeMgr(strDevId);
		if (mgr != null) {
			mgr.StopUpgradeStartTimer();
			mgr.StopUpgradeSendTimer();
		}
		m_moduleUpgradeMgrMap.remove(strDevId);
	}	
	public ModuleUpgradeOnLineMgr GetModuleUpgradeMgr(String strdevId)
	{
		if (!m_moduleUpgradeMgrMap.containsKey(strdevId))
		{
			LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Module(%s) not register. NO Upgrade Mgr",  strdevId));
			return null;
		}
		return m_moduleUpgradeMgrMap.get(strdevId);
	}	
	
	/**注册接收文件管理器 **/
	public void RegisterRecvFileMgr(String strDevId, int fileno)
	{
		ModuleRecvFileMgr moduleRecvFileMgr = new ModuleRecvFileMgr(strDevId, fileno);
		m_ModuleRecvFileMap.put(strDevId, moduleRecvFileMgr);
	}
	
	public static void UnRegisterRecvFileMgr(String strDevId)
	{
		ModuleRecvFileMgr mgr = GetModuleRecvFileMgr(strDevId);
		if (mgr != null) {
			mgr.Destroy();
		}
		m_ModuleRecvFileMap.remove(strDevId);
	}

	public static ModuleRecvFileMgr GetModuleRecvFileMgr(String strDevId)
	{
		if (!m_ModuleRecvFileMap.containsKey(strDevId))
		{
			LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Module(%s) not register. NO RecvFile Mgr",  strDevId));
			return null;
		}
		return m_ModuleRecvFileMap.get(strDevId);
	}
	
	/** 注册模块日志记录文件管理器 **/
	public static void RegisterModuleLogFileMgr(String strDevId)
	{
		ModuleLogFileMgr moduleLogFileMgr = new ModuleLogFileMgr(strDevId);
		m_ModuleLogFileMap.put(strDevId, moduleLogFileMgr);
	}
	
	public static void UnRegisterModuleLogFileMgr(String strDevId)
	{
		ModuleLogFileMgr mgr = GetModuleLogFileMgr(strDevId);
		if (mgr != null) {
			mgr.Destroy();
		}
		m_ModuleLogFileMap.remove(strDevId);
	}

	public static ModuleLogFileMgr GetModuleLogFileMgr(String strDevId)
	{
		if (!m_ModuleLogFileMap.containsKey(strDevId))
		{
			LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Module(%s) not register. NO RecvLogFile Mgr",  strDevId));
			return null;
		}
		return m_ModuleLogFileMap.get(strDevId);
	}
	
	public static int GetModuleFileNO(String strDevId)
	{
		if (!m_ModuleFileNOMap.containsKey(strDevId))
		{
			SetModuleFileNO(strDevId, 0);
		}
		return m_ModuleFileNOMap.get(strDevId);
	}

	public static void SetModuleFileNO(String strDevId, int fileno)
	{
		m_ModuleFileNOMap.put(strDevId, fileno);
	}
	
	public static void Init()
	{
		/* APP命令 */
		m_SendFuncMap.put(ServerCommDefine.APP_REMOTE_PRINT_MSG_HEADER, new AppRemotePrintMsgHandle());

		m_SendFuncMap.put(ServerCommDefine.APP_REGUSER_MSG_HEADER, new UserRegisterMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_RSTPWD_MSG_HEADER, new FindPwdHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_LOGIN_MSG_HEADER, new AppLoginMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_LOGOUT_MSG_HEADER, new AppLogOutMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_ADD_PLUG_MSG_HEADER, new AddModuleMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_DEL_PLUG_MSG_HEADER, new DeleteModuleMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_MOD_PLUG_MSG_HEADER, new ModifyPlugNameHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_QRY_PLUG_MSG_HEADER, new QueryAllModuleInfoHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_MOD_EMAIL_MSG_HEADER, new ModEmailMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_MOD_PWD_MSG_HEADER, new ModPwdMsgHandle());
		
		// 空调红外遥控器
		m_SendFuncMap.put(ServerCommDefine.APP_QRY_IRDATA_MSG_HEADER, new IRQueryIRDataHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_AIRCON_SERVER_CTRL_MSG_HEADER, new ModuleAirConServerCtrlMsgHandle());
		
		// 电视红外遥控器
		m_SendFuncMap.put(ServerCommDefine.APP_QRY_TV_IRDATA_MSG_HEADER, new IRQueryTVIRDataHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_TV_SERVER_CTRL_MSG_HEADER, new ModuleTVServerCtrlMsgHandle());
		
		
		m_SendFuncMap.put(ServerCommDefine.APP_ADD_SCENE_MSG_HEADER, new IRAddSceneMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_DEL_SCENE_MSG_HEADER, new IRDelSceneMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_MODIFY_SCENE_MSG_HEADER, new IRModifySceneMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_QUERY_SCENE_MSG_HEADER, new IRQuerySceneMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_ENABLE_SCENE_MSG_HEADER, new IREnableSceneMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.START_SCENE_MSG_HEADER, new IRStartSceneMsgHandle());

		m_SendFuncMap.put(ServerCommDefine.ADD_SCENE_MSG_HEADER, new IRAddSceneMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.DEL_SCENE_MSG_HEADER, new IRDelSceneMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.MODIFY_SCENE_MSG_HEADER, new IRModifySceneMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.QUERY_SCENE_MSG_HEADER, new IRQuerySceneMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.ENABLE_SCENE_MSG_HEADER, new IREnableSceneMsgHandle());		
		
		/* APP和模块命令*/
		m_SendFuncMap.put(ServerCommDefine.APP_BELL_ON_MSG_HEADER, new ModuleBellOnMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_SET_TIMER_ENABLE_MSG_HEADER, new ModuleSetTimerEnableFlagMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_TCP_UDP_MSG_HEADER, new ModuleSetTcpUdpMsgHandle());		
		m_SendFuncMap.put(ServerCommDefine.APP_PARENT_CTRL_MSG_HEADER, new ModulePowerCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_USB_CTRL_MSG_HEADER, new ModulePowerCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_LIGHT_CTRL_MSG_HEADER, new ModulePowerCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_POWER_CTRL_MSG_HEADER, new ModulePowerCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_BACK2AP_CTRL_MSG_HEADER, new ModuleBack2APCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_LIGHT_RGB_MSG_HEADER, new ModuleLightRGBMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_ADD_TIMER_MSG_HEADER, new ModuleAddTimerMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_DEL_TIMER_MSG_HEADER, new ModuleDelTimerMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_MOD_TIMER_MSG_HEADER, new ModuleModTimerMsgHandle());

		m_SendFuncMap.put(ServerCommDefine.APP_CURTAIN_CTRL_MSG_HEADER, new ModuleCurtainCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_WINDOW_CTRL_MSG_HEADER, new ModuleWindowCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_ALED_CTRL_MSG_HEADER, new ModuleALEDCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_AIRCON_CTRL_MSG_HEADER, new ModuleAirConCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_UPGRADE_START_MSG_HEADER, new AppUpgradeStartMsgHandle());

		/* APP透传通道命令 */
		m_SendFuncMap.put(ServerCommDefine.APP_PASSTHROUGH_MSG_HEADER, new AppPassThroughMsgHandle());
		
		/* 模块命令 */
		m_SendFuncMap.put(ServerCommDefine.BELL_ON_MSG_HEADER, new ModuleBellOnMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.SET_TIMER_ENABLE_MSG_HEADER, new ModuleSetTimerEnableFlagMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.TCP_UDP_MSG_HEADER, new ModuleSetTcpUdpMsgHandle());
		
		m_SendFuncMap.put(ServerCommDefine.NOTIFY_POWER_STATUS, new NotifyPowerStatusHandle());
		m_SendFuncMap.put(ServerCommDefine.NOTIFY_CURTAIN_STATUS, new NotifyCurtainStatusHandle());
		m_SendFuncMap.put(ServerCommDefine.NOTIFY_KETTLE_STATUS, new NotifyKettleStatusHandle());
		
		/* 功率功能 */
		m_SendFuncMap.put(ServerCommDefine.NOTIFY_ENERGE, new NotifyEnergeHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_ENABLE_ENERGE, new EnableEnergeHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_QUERY_GONGLV, new QueryGonglvHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_QUERY_CHARGE, new QueryChargeHandle());
		m_SendFuncMap.put(ServerCommDefine.ENABLE_ENERGE, new EnableEnergeHandle());
		m_SendFuncMap.put(ServerCommDefine.QUERY_GONGLV, new QueryGonglvHandle());
		
		/* 智能电池 */
		m_SendFuncMap.put(ServerCommDefine.NOTIFY_BATTERYENERGE, new NotifyBatteryEnergeHandle());
		m_SendFuncMap.put(ServerCommDefine.NOTIFY_BATTERYLOCATION, new NotifyBatteryLocationHandle());
		
		m_SendFuncMap.put(ServerCommDefine.APP_QUERY_BATTERYENERGE, new QueryBatteryEnergeHandle());
		m_SendFuncMap.put(ServerCommDefine.APP_QUERY_BATTERYLOCATON, new QueryBatteryLocationHandle());
		
		m_SendFuncMap.put(ServerCommDefine.QUERY_NETIP, new QueryNetIPHandle());
		
		m_SendFuncMap.put(ServerCommDefine.MODULE_HEART_MSG_HEADER, new ModuleHeartMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.PARENT_CTRL_MSG_HEADER, new ModulePowerCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.USB_CTRL_MSG_HEADER, new ModulePowerCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.LIGHT_CTRL_MSG_HEADER, new ModulePowerCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.POWER_CTRL_MSG_HEADER, new ModulePowerCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.BACK2AP_CTRL_MSG_HEADER, new ModuleBack2APCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.LIGHT_RGB_MSG_HEADER, new ModuleLightRGBMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.ADD_TIMER_MSG_HEADER, new ModuleAddTimerMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.DEL_TIMER_MSG_HEADER, new ModuleDelTimerMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.MOD_PLUG_MSG_HEADER, new ModuleModPlugMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.MOD_TIMER_MSG_HEADER, new ModuleModTimerMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.MOD_MODULE_NAME_MSG_HEADER, new ModuleModNameMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.MODULE_LOGIN_MSG_HEADER, new ModuleLoginHandle());
		m_SendFuncMap.put(ServerCommDefine.UPGRADE_START_MSG_HEADER, new UpgradeStartRespHandle());
		m_SendFuncMap.put(ServerCommDefine.MODULE_UPGRADE_SEND_MSG_HEADER, new UpgradeSendRespHandle());
		m_SendFuncMap.put(ServerCommDefine.MODULE_UPGRADE_END_MSG_HEADER, new UpgradeEndRspHandle());
		m_SendFuncMap.put(ServerCommDefine.MODULE_UPGRADE_RESEND_MSG_HEADER, new UpgradeReSendRespHandle());
		m_SendFuncMap.put(ServerCommDefine.MODULE_UPGRADE_REEND_MSG_HEADER, new UpgradeReEndRspHandle());
		
		m_SendFuncMap.put(ServerCommDefine.CURTAIN_CTRL_MSG_HEADER, new ModuleCurtainCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.WINDOW_CTRL_MSG_HEADER, new ModuleWindowCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.ALED_CTRL_MSG_HEADER	, new ModuleALEDCtrlMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.AIRCON_CTRL_MSG_HEADER, new ModuleAirConCtrlMsgHandle());

		m_SendFuncMap.put(ServerCommDefine.MODULE_ENAMBE_IR_TIMER_MSG_HEADER, new ModuleEnableIRTimerMsgHandle());
		
		/* 模块升级命令 */
		m_SendFuncMap.put(ServerCommDefine.MODULE_RECV_FILE_START_MSG_HEADER, new ModuleRecvFileStartMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.MODULE_RECV_FILE_SEND_MSG_HEADER, new ModuleRecvFileSendMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.MODULE_RECV_FILE_END_MSG_HEADER, new ModuleRecvFileEndMsgHandle());
		
		/* 模块日志 */
		m_SendFuncMap.put(ServerCommDefine.MODULE_LOG_FILE_START_MSG_HEADER, new ModuleLogFileMsgHandle());
		
		/** 转发器功能  **/
		m_SendFuncMap.put(ServerCommDefine.TRANSMIT_HEARBEAT_MSG_HEADER, new TransmitHearBeatMsgHandle());
		m_SendFuncMap.put(ServerCommDefine.TRANSMIT_TRANS_MSG_HEADER, new TransmitTransMsgHandle());

		/* 空调红外数据接口 */
		String jsonStr = JsonFileReader.getJson(ServerParamConfiger.strIRFileName);
		parseJSONWithGSON_IRDATA(jsonStr);
		initIRSubID();
		
		/* 电视红外数据接口 */
		jsonStr = JsonFileReader.getJson(ServerParamConfiger.strTVIRFileName);
		parseJSONWithGSON_TVIRDATA(jsonStr);
		initTVIRSubID();
	}
	
	private static void initIRSubID() {
		// TODO Auto-generated method stub
		m_IRSubId.put("mode_auto","001");
		m_IRSubId.put("mode_cool","002");
		m_IRSubId.put("mode_wet","003");
		m_IRSubId.put("mode_wind","004");
		m_IRSubId.put("mode_warm","005");
		m_IRSubId.put("scale_auto","006");
		m_IRSubId.put("scale_big","007");
		m_IRSubId.put("scale_middle","008");
		m_IRSubId.put("scale_small","009");
		m_IRSubId.put("direction_on","010");
		m_IRSubId.put("direction_off","011");
		m_IRSubId.put("sway_1","012");
		m_IRSubId.put("sway_2","013");
		m_IRSubId.put("sway_3","014");
		m_IRSubId.put("sway_4","015");
		m_IRSubId.put("open_01","016");
		m_IRSubId.put("open_02","017");
		m_IRSubId.put("open_03","018");
		m_IRSubId.put("open_04","019");
		m_IRSubId.put("open_05","020");
		m_IRSubId.put("open_06","021");
		m_IRSubId.put("open_07","022");
		m_IRSubId.put("open_08","023");
		m_IRSubId.put("open_09","024");
		m_IRSubId.put("open_10","025");
		m_IRSubId.put("open_11","026");
		m_IRSubId.put("open_12","027");
		m_IRSubId.put("open_13","028");
		m_IRSubId.put("open_14","029");
		m_IRSubId.put("open_15","030");
		m_IRSubId.put("open_close","031");
		m_IRSubId.put("close_01","032");
		m_IRSubId.put("close_02","033");
		m_IRSubId.put("close_03","034");
		m_IRSubId.put("close_04","035");
		m_IRSubId.put("close_05","036");
		m_IRSubId.put("close_06","037");
		m_IRSubId.put("close_07","038");
		m_IRSubId.put("close_08","039");
		m_IRSubId.put("close_09","040");
		m_IRSubId.put("close_10","041");
		m_IRSubId.put("close_11","042");
		m_IRSubId.put("close_12","043");
		m_IRSubId.put("close_13","044");
		m_IRSubId.put("close_14","045");
		m_IRSubId.put("close_15","046");
		m_IRSubId.put("close_close","047");
		m_IRSubId.put("temp_16","048");
		m_IRSubId.put("temp_17","049");
		m_IRSubId.put("temp_18","050");
		m_IRSubId.put("temp_19","051");
		m_IRSubId.put("temp_20","052");
		m_IRSubId.put("temp_21","053");
		m_IRSubId.put("temp_22","054");
		m_IRSubId.put("temp_23","055");
		m_IRSubId.put("temp_24","056");
		m_IRSubId.put("temp_25","057");
		m_IRSubId.put("temp_26","058");
		m_IRSubId.put("temp_27","059");
		m_IRSubId.put("temp_28","060");
		m_IRSubId.put("temp_29","061");
		m_IRSubId.put("temp_30","062");
		m_IRSubId.put("power_on","063");
		m_IRSubId.put("power_off","064");
		m_IRSubId.put("warmer","065");

	}

	private static void initTVIRSubID() {
		m_TVIRSubId.put("power_on","001");
		m_TVIRSubId.put("power_off","002");
		m_TVIRSubId.put("mute_on","003");
		m_TVIRSubId.put("mute_off","004");
		m_TVIRSubId.put("volume_add","005");
		m_TVIRSubId.put("volume_reduce","006");
		m_TVIRSubId.put("channel_add","007");
		m_TVIRSubId.put("channel_reduce","008");
		m_TVIRSubId.put("num_01","009");
		m_TVIRSubId.put("num_02","010");
		m_TVIRSubId.put("num_03","011");
		m_TVIRSubId.put("num_04","012");
		m_TVIRSubId.put("num_05","013");
		m_TVIRSubId.put("num_06","014");
		m_TVIRSubId.put("num_07","015");
		m_TVIRSubId.put("num_08","016");
		m_TVIRSubId.put("num_09","017");
		m_TVIRSubId.put("num_00","018");
		m_TVIRSubId.put("num_cancel","019");
		m_TVIRSubId.put("num_ok","020");
	}

	private static boolean IsNeedJudgeLogin(String strCmd)
	{
		if (strCmd.equalsIgnoreCase(ServerCommDefine.APP_LOGIN_MSG_HEADER) ||
			strCmd.equalsIgnoreCase(ServerCommDefine.APP_REGUSER_MSG_HEADER) ||
			strCmd.equalsIgnoreCase(ServerCommDefine.APP_RSTPWD_MSG_HEADER) ||
			strCmd.equalsIgnoreCase(ServerCommDefine.APP_PASSTHROUGH_MSG_HEADER) ||
			strCmd.equalsIgnoreCase(ServerCommDefine.APP_AIRCON_SERVER_CTRL_MSG_HEADER))
		{
			return false;
		}
		
		return true;
	}
	
	public void StartHeartTimer(String strModuleID)
	{
		ConnectInfo info = ServerWorkThread.getModuleConnectInfo(strModuleID);
		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Start Heart Timer:%s, timer info:%s", strModuleID, info.getHeartTimer().toString()));
		info.getHeartTimer().schedule(info.getBeatTask(), 
				ServerParamConfiger.getiTimeOutHeartBeat(),  
				ServerParamConfiger.getiTimeOutHeartBeat());
	}
	
	public void StopHeartTimer(String strModuleID)
	{
		ConnectInfo info = ServerWorkThread.getModuleConnectInfo(strModuleID);
		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Stop Heart Timer:%s, timer info:%s", strModuleID, info.getHeartTimer().toString()));
		//info.getBeatTask().cancel();
		info.getHeartTimer().cancel();
	}
	
	/**
	 * * APP COOKIE
	 * */
	public static String GetCurAppIP(String strUserName)
	{
		if (!m_AppIPMap.containsKey(strUserName))
		{
			LogWriter.WriteDebugLog(LogWriter.SELF, String.format("User(%s) not login. NO APP IP",  strUserName));
			return null;
		}
		return m_AppIPMap.get(strUserName).getSrcIP();
	}
	
	public static int GetCurAppPort(String strUserName)
	{
		if (!m_AppIPMap.containsKey(strUserName))
		{
			LogWriter.WriteDebugLog(LogWriter.SELF, String.format("User(%s) not login. NO APP Port",  strUserName));
			return 0;
		}
		return m_AppIPMap.get(strUserName).getSrcPort();
	}
	
	public static void RegisterUserIP(String strUserName, String strIP, int strPort)
	{
		ConnectInfo info = new ConnectInfo(strIP, strPort);
		m_AppIPMap.put(strUserName, info);
	}
	
	public static void RefreshUserIP(String strUserName, String strIP, int strPort)
	{
		ConnectInfo info = m_AppIPMap.get(strUserName);
		if (null != info)
		{
			info.setSrcIP(strIP);
			info.setSrcPort(strPort);
			m_AppIPMap.put(strUserName, info);
		} else {
			RegisterUserIP(strUserName, strIP, strPort);
		}
	}
	
	public static void RefreshAppCookie(String strUserName, String strCookie)
	{
		ConnectInfo info = m_AppIPMap.get(strUserName);
		if (null != info)
		{
			info.setCookie(strCookie);
			m_AppIPMap.put(strUserName, info);	
		}
	}
	
	public static void UnRegisterUserIP(String strUserName)
	{
		m_AppIPMap.remove(strUserName);
	}
	
	public static boolean IsUserLogin(String strUserName)
	{
		// 临时：测试账户，不用检车用户登录
		if(strUserName.contains(PubDefine.TEST_USERNAME) == true) {
			return true;
		} else {
			return m_AppIPMap.containsKey(strUserName);
		}
	}
	
	public static int CheckCookie(String strLocal, String strRemote)
	{
//		//目前不校验Cookie
//		if(!strLocal.equalsIgnoreCase(strRemote))
//		{  
//			LogWriter.WriteDebugLog(LogWriter.SELF, 
//					String.format("local cookie is %s, remote cookie:%s", strLocal,strRemote));
//			return ServerRetCodeMgr.ERROR_CODE_COOKIE_INCORRECT;
//		}
		
		return ServerRetCodeMgr.SUCCESS_CODE;
	}
	
	/**
	 * * 
	 * */
	public static ConnectInfo getModuleConnectInfo(String strModuleID)
	{
		return m_ModuleIPMap.get(strModuleID);
	}
	
	public static String getModuleIp(String strModuleID)
	{
		ConnectInfo info = m_ModuleIPMap.get(strModuleID);
		if (null != info)
		{
			return m_ModuleIPMap.get(strModuleID).getSrcIP();
		}
		LogWriter.WriteErrorLog(LogWriter.SELF, String.format("Module(%s) not login. NO MODULE IP", strModuleID));
		
		return null;
	}

	public static int getModulePort(String strModuleID)
	{
		ConnectInfo info = m_ModuleIPMap.get(strModuleID);
		if (null != info)
		{
			return m_ModuleIPMap.get(strModuleID).getSrcPort();
		}
		LogWriter.WriteErrorLog(LogWriter.SELF, String.format("Module(%s) not login. NO MODULE Port", strModuleID));
		
		return 0;
	}
	public static String getAllOnlineModule()
	{
		String strInfo = "========all online module info========\n";
		for (Entry<String, ConnectInfo> entry : m_ModuleIPMap.entrySet()) 
		{  
			strInfo += "Key = " + entry.getKey() + ", Value(IP) = " + entry.getValue().getSrcIP() + ", Value(Port) = " + entry.getValue().getSrcPort();  
			strInfo += "\n";
		}  
		
		return strInfo;
	}
	
	public static void RegisterModuleIP(String strModuleID, String strCookie, String strIP, int intPort)
	{
		if (m_ModuleIPMap.containsKey(strModuleID))
		{
			getModuleConnectInfo(strModuleID).getBeatTask().cancel();
			getModuleConnectInfo(strModuleID).getHeartTimer().cancel();
		}
		
		Timer heart_timer = new Timer();
		ModuleHeartBeatTask beatTask = new ModuleHeartBeatTask(strModuleID);
		m_ModuleIPMap.put(strModuleID, new ConnectInfo(strIP, intPort,strCookie, heart_timer, beatTask));
	}
	
	public static void RefreshModuleIP(String strModuleID, String strIP, int intPort)
	{
		ConnectInfo info = m_ModuleIPMap.get(strModuleID);
		if (null != info)
		{
			info.setSrcIP(strIP);
			info.setSrcPort(intPort);
			m_ModuleIPMap.put(strModuleID, info);
		}
	}
	
	public static void RefreshModuleCookie(String strModuleID, String strCookie)
	{
		ConnectInfo info = m_ModuleIPMap.get(strModuleID);
		if (null != info)
		{
			info.setCookie(strCookie);
			m_ModuleIPMap.put(strModuleID, info);	
		}
	}
	
	public static void RefreshModuleAliveFlag(String strModuleID, boolean bAlive)
	{
		String str;
		ConnectInfo info = m_ModuleIPMap.get(strModuleID);
		if (null != info)
		{
			info.setAlive(bAlive);
			m_ModuleIPMap.put(strModuleID, info);	
		}
		
		if (null == info)
		{
			str = String.format("Module=%s, aLive=%d; info is null.", strModuleID, bAlive?1:0);
		}
		else
		{
			str = String.format("Module=%s, aLive=%d, info is not null.", strModuleID, bAlive?1:0);
		}
//		Exception e = new Exception(str);
//		LogWriter.WriteExceptionLog(LogWriter.SELF, e, str);
	}
	
	public static void UnRegisterModuleIP(String strModuleID)
	{
		if (m_ModuleIPMap.containsKey(strModuleID))
		{
			getModuleConnectInfo(strModuleID).getHeartTimer().cancel();
		}
		
		m_ModuleIPMap.remove(strModuleID);
	}
	
	/* 转发器 组件
	 * Author: 
	 */
	public static boolean RegisterTransmitIP(String strModuleID, String strCookie, String strIP, int intPort)
	{
		ConnectInfo info = m_ModuleTransmitMap.get(strModuleID);
		if (null != info)
		{
			return false;
//			info.setSrcIP(strIP);
//			info.setSrcPort(intPort);
//			m_ModuleTransmitMap.put(strModuleID, info);
		} else {
			m_ModuleTransmitMap.put(strModuleID, new ConnectInfo(strIP, intPort,strCookie, null, null));
			return true;
		}
	}
	
	public static void UnRegisterTransmitIP(String strModuleID)
	{
		m_ModuleTransmitMap.remove(strModuleID);
	}
	
	public static ConnectInfo getTransmitConnectInfo(String strModuleID)
	{
		return m_ModuleTransmitMap.get(strModuleID);
	}

	public static int getNewTransmitID () {
		for (int i = 1000; i < 10000; i++) {
			String strId = String.valueOf(i);
			if (m_ModuleTransmitMap.get(strId) == null)
				return i;
		}
		return 0;
	}
	
	public static String getAllTransmitID () {
		String strOut = "";
		int i = 0;
		for (String key : m_ModuleTransmitMap.keySet()) {
			strOut += key;
			i++;
			if (i < m_ModuleTransmitMap.size())
				strOut += "@";
		}
		
		return strOut;
	}	
	
	public static String[] getAllTransmitIDs () {
		String[] strOut = new String[m_ModuleTransmitMap.size()];
		int i = 0;
		for (String key : m_ModuleTransmitMap.keySet()) {
			strOut[i++] = key;
		}
		
		return strOut;
	}
	
	
	public static boolean IsModuleLogin(String strModuleID)
	{
		return m_ModuleIPMap.containsKey(strModuleID);
	}
	
	public void Response(String strRet) throws IOException
	{
		byte[] info = strRet.getBytes();
		Response(info);
	}
	
	public void Response(byte[] info) throws IOException
	{
		DatagramPacket dataPacket = new DatagramPacket(info, info.length, m_packet.getAddress(), m_packet.getPort());    
		ServerMainThread.dataSocket.send(dataPacket);
	}
	
	/**
	 * ResponseToAPP(String strCmd, String strUserName, String strModuleId, int error_code)
	 * @throws SQLException 
	 * */
	public void ResponseToAPP(ServerWorkThread app_thread, String strCmd, String strUserName, String strModuleId, int ret_code, String strDesp) 
	{
		if(!ServerWorkThread.IsUserLogin(strUserName))
		{
			LogWriter.WriteErrorLog(LogWriter.SELF, String.format("user(%s) is offline.", strUserName));  
			return;
		}
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");	
		String strNewCookie = df.format(new Date());
		
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);

		String strRet = String.format("%s,%s,%s,%s,%08x#", strNewCookie, strCmd, strUserName, strModuleId, ret_code);
		try {
			Response(strRet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e,m_packet.getAddress().toString() + ":" + m_packet.getPort());
		}
		
		if(ret_code == ServerRetCodeMgr.SUCCESS_CODE)
		{
			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t [%s] %s (%s)",
					m_packet.getAddress().toString(),
					m_packet.getPort(),
					strRet,
					ServerRetCodeMgr.getRetCodeDescription(ret_code),
					strUserName));
			return;
		}
		
		LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s:%d)\t [%s] %s (%s) description:%s",
				m_packet.getAddress().toString(),
				m_packet.getPort(),
				strRet,
				ServerRetCodeMgr.getRetCodeDescription(ret_code),
				strUserName,strDesp));
		return;
	}
	
	private static boolean isAppCommand(String strCmd)
	{
		if (strCmd.toUpperCase().startsWith("APP"))
		{
			return true;
		}
		return false;
	}
	
	public static int ServerMsgHandle(String strMsg, byte[] strMsgBin) throws IOException 
	{
		strMsg = strMsg.substring(0, strMsg.lastIndexOf("#") + 1);
		String[] strResult 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strResult[0].trim();
		String strCmd 		= strResult[1].trim();
		String strUserName	= strResult[2].trim();
	
		ICallFunction func = (ICallFunction) m_SendFuncMap.get(strCmd);
		if(null == func)
		{
			LogWriter.WriteErrorLog(LogWriter.RECV, 
					String.format("(%s:%d)\t Invalid Msg Header:%s", 
							m_packet.getAddress().toString(),
							m_packet.getPort(),
							strMsg));
			return  ServerRetCodeMgr.ERROR_FAILD_FIND_CMD_FUNC;
		}
		

		/* APP命令判断 */
		if (isAppCommand(strCmd))
		{
			LogWriter.WriteDebugLog(LogWriter.RECV,
					String.format("(%s:%d)\t FromAPP: %s",
							m_packet.getAddress().toString(),
							m_packet.getPort(),
							strMsg));
			
			if (IsNeedJudgeLogin(strCmd))
			{
				/* 是否已经登录 */
				if(!ServerWorkThread.IsUserLogin(strUserName))
				{
					String strResp = String.format("%s,%s,%s,%s,2#",
							ServerCommDefine.DEFAULT_COOKIE, ServerCommDefine.APP_LOGOUT_MSG_HEADER, strUserName, "000000");
					
					LogWriter.WriteDebugLog(LogWriter.SEND,
							String.format("(%s:%d)\t SEND: %s",
									m_packet.getAddress().toString(),
									m_packet.getPort(),
									strResp));
					
					String strIP = getSrcIP();
					int strPort = getSrcPort();
					byte[] buffer = new byte[1024];
					DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(strIP), strPort);    
					dataPacket.setData(strResp.getBytes());  
					ServerMainThread.dataSocket.send(dataPacket);	
					
					return ServerRetCodeMgr.ERROR_CODE_APP_NOT_LOGIN;					
				}
			}
			
			/* 命令刷新 UserIP/Port */
			if (!(strCmd.equals(ServerCommDefine.APP_LOGIN_MSG_HEADER)  || 
				  strCmd.equals(ServerCommDefine.APP_RSTPWD_MSG_HEADER) ||
				  strCmd.equals(ServerCommDefine.APP_LOGOUT_MSG_HEADER) ||
				  strCmd.equals(ServerCommDefine.APP_REGUSER_MSG_HEADER)
				  )) {
				
				String old_strIP = ServerWorkThread.GetCurAppIP(strUserName);
				int old_Port = ServerWorkThread.GetCurAppPort(strUserName);
				String strIP = getSrcIP();
				int strPort = getSrcPort();
				/*  */
				if ((null == old_strIP) ||
					!(old_strIP.equals(strIP) && (old_Port == strPort))) {
					ServerWorkThread.RefreshUserIP(strUserName, strIP, strPort);
					
					LogWriter.WriteDebugLog(LogWriter.SELF, 
							String.format("old(%s:%d)->new(%s:%d)\t [%s] [%s] RefreshUserIP_Port",
									old_strIP == null ? "NULL" : old_strIP, 
									old_Port,
									strIP, 
									strPort,
									strUserName, strCmd));	
				}
			}
			if (strCmd.equals(ServerCommDefine.APP_PASSTHROUGH_MSG_HEADER))
			{
				return func.call(m_thread, strMsg, strMsgBin);
			} else {
				return func.call(m_thread, strMsg);
			}
		}
		
		LogWriter.WriteDebugLog(LogWriter.RECV,
				String.format("(%s:%d)\t FromModule: %s",
						m_packet.getAddress().toString(),
						m_packet.getPort(),
						strMsg));
		
		/* 模块命令 */
		return func.resp(m_thread, strMsg);
	}
	
	public void run() {
		try {
			/* JAVA环境编码 */
//			System.out.println("JVM file encoding: " + System.getProperty("file.encoding"));

//			String receiveStr = new String(m_packet.getData(), 0, m_packet.getLength(),"ISO-8859-1");
			
//			String receiveStr = new String(m_packet.getData(), 0, m_packet.getLength(),"UTF-8");
			String receiveStr = new String(m_packet.getData());
			
//			System.out.println(String.format("receiveIP: %s:%d", m_packet.getAddress().toString(), m_packet.getPort()));
//			System.out.println("receiveStr: " + receiveStr);
//			System.out.println("receiveStr(byte): " + Arrays.toString((m_packet.getData())));
			
//			if (receiveStr.getBytes()[0] == '\0') {
//				return;
//			}
			
			int iRet = ServerMsgHandle(receiveStr, m_packet.getData());
			LogWriter.WriteTraceLog(LogWriter.SELF, 
					String.format("(%s:%d)\t Completed to Excute Command(Ret:0x%08x).", m_packet.getAddress().toString(),m_packet.getPort(), iRet));	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e,m_packet.getAddress().toString() + ":" + m_packet.getPort());
		} 
	}
		
	public static void main(String[] args)
	{
		return;
	}

	public static String getAppCookie(String strUserName) {
		// 临时： 测试账户
		if(strUserName.equals(PubDefine.TEST_USERNAME) == true) {
			return "20171208101017";
		} else {
			return m_AppIPMap.get(strUserName).getCookie();
		}
	}
	
	public static String getModuleCookie(String strModuleID)
	{
		return m_ModuleIPMap.get(strModuleID).getCookie();
	}

	public static String getIRSubId(String subIRName) {
		// TODO Auto-generated method stub
		String subId = m_IRSubId.get(subIRName);
		return subId;
	}
	public static String getTVIRSubId(String subIRName) {
		// TODO Auto-generated method stub
		String subId = m_TVIRSubId.get(subIRName);
		return subId;
	}

}