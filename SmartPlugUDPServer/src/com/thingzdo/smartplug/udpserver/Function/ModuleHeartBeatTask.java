package com.thingzdo.smartplug.udpserver.Function;

import java.sql.Timestamp;
import java.util.TimerTask;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ConnectInfo;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerParamConfiger;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_DATA;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class ModuleHeartBeatTask  extends TimerTask implements ICallFunction{
	String m_strModuleID = null;
	public ModuleHeartBeatTask(String strModuleID)
	{
		m_strModuleID = strModuleID;
	}
	@Override
	public void run() {
		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("[%s]Curr Timer:%s", m_strModuleID, this.toString()));
		
		ConnectInfo info = ServerWorkThread.getModuleConnectInfo(m_strModuleID);
		if (null == info)
		{
			//没有收到心跳包
			LogWriter.WriteHeartLog(LogWriter.SELF,
					String.format("[%s]Module not login. HearBeatTask is cancel.", m_strModuleID));
			// 40352 必须修改的地方
			this.cancel();
			return;
		}
		
		/* 判断当前的TASK是否为游离TASK */
		if (info.getBeatTask() != this)
		{
			LogWriter.WriteDebugLog(LogWriter.SELF, 
					String.format("[BUG]This BeatTask is OffControl(%s),id=%s", this.toString(),m_strModuleID));
		}
		
		if(ServerWorkThread.getModuleConnectInfo(m_strModuleID).isAlive())
		{
			//收到心跳包
			ServerWorkThread.RefreshModuleAliveFlag(m_strModuleID, false);
			LogWriter.WriteHeartLog(LogWriter.SELF,
					String.format("[%s]Server receive heart package.TaskTimer:%s", m_strModuleID, this.toString()));
		}
		else
		{
			//没有收到心跳包
			LogWriter.WriteHeartLog(LogWriter.SELF,
					String.format("[%s]Server did not receive heart package.TaskTimer:%s", m_strModuleID, this.toString()));
			
			/* step1通知用户模块下线  */
			// Debug for new ServerDBMgr()
//			LogWriter.WriteDebugLog(LogWriter.SELF, 
//					String.format("(Before ModuleHeartBeatTask new ServerDBMgr(), m_strModuleID=(%s)", m_strModuleID));
			
			ServerDBMgr dbMgr = new ServerDBMgr();
			
//			LogWriter.WriteDebugLog(LogWriter.SELF, 
//					String.format("(After ModuleHeartBeatTask new ServerDBMgr(), m_strModuleID=(%s)", m_strModuleID));
			
			try
			{
				USER_MODULE user_info = dbMgr.QueryUserModuleByDevId(m_strModuleID);
				if (null != user_info)
				{
					NotifyToAPP(user_info.getUserName(), m_strModuleID, 
							ServerCommDefine.APP_NOTIFY_ONLINE_MSG_HEADER, 
							ServerRetCodeMgr.SUCCESS_CODE,
							String.valueOf(ServerCommDefine.MODULE_OFF_LINE)) ;
				}
	
				/* step2 清理模块存储的信息 */
				ServerWorkThread.UnRegisterModuleIP(m_strModuleID);
				
				//清理模块日志信息
				ServerWorkThread.UnRegisterModuleLogFileMgr(m_strModuleID);
				
				/* step3 停止心跳检测定时器*/
				LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Stop Heart Timer:%s, timer info:%s", m_strModuleID, info.getHeartTimer().toString()));
				info.getHeartTimer().cancel();
				
				// 更新模块上线日志信息 lishimin -- MODULE_DATA
				if (ServerParamConfiger.getRecordModuleData() == true) {
					dbMgr.BeginTansacion();
					MODULE_DATA data = dbMgr.QueryModuleDataByModuleId(m_strModuleID);
					Timestamp dtLogoutTime = dbMgr.getCurrentTime();
					data.setLogoutTime(dtLogoutTime);
					long ionlineTime = (dtLogoutTime.getTime() - data.getLoginTime().getTime())/1000;
					data.setOnlineTime(ionlineTime);
					
					boolean bRet = dbMgr.UpdateModuleData(data);
					if(!bRet)
					{
						LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s)\t Failed to UpdateModuleData:[%s - %s]", 
								m_strModuleID,data.getLoginTime(),data.getLogoutTime()));
						dbMgr.Rollback();
						dbMgr.EndTansacion();
					}
					dbMgr.Commit();
					dbMgr.EndTansacion();
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				dbMgr.Destroy();
			}
		}
	}
	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
}
