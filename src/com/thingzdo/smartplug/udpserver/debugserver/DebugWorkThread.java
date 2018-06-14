package com.thingzdo.smartplug.udpserver.debugserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.StringTool.StringConvertTool;
import com.thingzdo.smartplug.udpserver.ServerMainThread;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_INFO;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.TIMER_INFO;
import com.thingzdo.smartplug.udpserver.db.USER_INFO;

public class DebugWorkThread  implements Runnable{
	private Socket debugSocket = null;
	private ServerDBMgr m_dbMgr 		= new ServerDBMgr();
	private static int DEBUG_SERVER_BUFFER_SIZE = 128;
	
	DebugWorkThread(Socket debug_socket)
	{
		debugSocket = debug_socket;
	}
	private void SignalParse(String strRecord) throws ClassNotFoundException, SQLException, IOException
	{
		if(strRecord.equalsIgnoreCase("help"))
		{
			DebugHelpHandle();
		}
		else if(strRecord.startsWith("ShowOnlineModule"))
		{
			ShowOnlineModule();
		}
		else if(strRecord.startsWith("DeleteUser"))
		{
			DeleteUser(strRecord);
		}
		else if(strRecord.startsWith("ShowUser"))
		{
			ShowUserInfo();
		}
		else if(strRecord.startsWith("ShowHeartPackNum"))
		{
			ShowHeartPackNum(strRecord);
		}
		else if(strRecord.startsWith("ShowModule"))
		{
			ShowModuleInfo();
		}
		else if(strRecord.startsWith("ShowTimer"))
		{
			ShowTimer(strRecord);
		}
//		else if(strRecord.startsWith("Passthroughtxt"))
//		{
//			Passthroughtxt(strRecord);
//		}
//		else if(strRecord.startsWith("Passthroughbin"))
//		{
//			Passthroughbin(strRecord);
//		}
		else
		{
			debugSocket.getOutputStream().write(String.format("Invalid debug cmd(%s)\n", strRecord).getBytes());
		}
	}
	private void ShowOnlineModule() throws IOException
	{
		String strInfo = ServerWorkThread.getAllOnlineModule();
		debugSocket.getOutputStream().write(strInfo.getBytes());
	}
	private void DeleteUser(String strRecord) throws SQLException, IOException
	{
		String strUserName = strRecord.split(",")[1];
		if(!m_dbMgr.DeleteUserInfo(strUserName))
		{
			debugSocket.getOutputStream().write(String.format("Failed to delete user %s", strUserName).getBytes());
		}
		debugSocket.getOutputStream().write(String.format("Succeed to delete user %s", strUserName).getBytes());
	}
	private void ShowUserInfo() throws ClassNotFoundException, SQLException, IOException
	{
		Vector<USER_INFO> vecUserInfo = m_dbMgr.QueryUserInfo();
		String strUserInfoList = String.format("================all user info list=============\n")
				+ String.format("USER_NAME \t USER_PWD \t EMAIL \n");
		for(int i = 0; i < vecUserInfo.size();i++)
		{
			strUserInfoList +=String.format("%s \t %s \t %s\n", vecUserInfo.get(i).getUserName()
					, vecUserInfo.get(i).getPassWord(),vecUserInfo.get(i).getEmail());
		}
		debugSocket.getOutputStream().write(strUserInfoList.getBytes());
	}
	private void ShowModuleInfo() throws ClassNotFoundException, SQLException, IOException
	{
		Vector<MODULE_INFO> vecModulerInfo = m_dbMgr.QueryModuleInfo();
		if(null == vecModulerInfo)
		{
			debugSocket.getOutputStream().write(String.format("no device exist.").getBytes());
			return;
		}
		String strModuleInfoList = String.format("================all module info list=============\n")
				+ String.format("ID \t NAME \t TIMERID_LIST \t POWER_ON_OFF_TIME_LIST \n");
		for(int i = 0; i < vecModulerInfo.size();i++)
		{
			strModuleInfoList +=String.format("%s \t %s \n"
					,vecModulerInfo.get(i).getModuleId()
					,vecModulerInfo.get(i).getModuleName());
		}
		debugSocket.getOutputStream().write(strModuleInfoList.getBytes());
	}
	private void ShowTimer(String strMsg) throws ClassNotFoundException, SQLException, IOException
	{
		String strRet[] = strMsg.split(",");
		if(strRet.length != 2)
		{
			debugSocket.getOutputStream().write(String.format("Invalid CMD(%s)", strMsg).getBytes());
			return;
		}
		String strDevId = strRet[1];
		Vector<TIMER_INFO> vecTimerInfo = m_dbMgr.QueryTimerInfoList(strDevId);
		String strTimerInfoList = String.format("================timer info list of dev(%s)=============\n",strDevId)
				+ String.format("ID \t TYPE \t MODULE_ID \t PEROID \t TIMEON \t TIMEOFF \t ENABLE\n");
		for(int i = 0; i < vecTimerInfo.size();i++)
		{
			strTimerInfoList +=String.format("%d \t %d \t %s \t %s \t %s \t %s \t %d\n"
					,vecTimerInfo.get(i).getTimerId()
					,vecTimerInfo.get(i).getTimerType()
					,vecTimerInfo.get(i).getModuleId()
					,vecTimerInfo.get(i).getPeroid()
					,vecTimerInfo.get(i).getTimeOn()
					,vecTimerInfo.get(i).getTimeOff()
					,vecTimerInfo.get(i).getEnableFlag());
		}
		debugSocket.getOutputStream().write(strTimerInfoList.getBytes());
	}
	

	private void Passthroughtxt(String strMsg) throws ClassNotFoundException, SQLException, IOException
	{
		String strRet[] = strMsg.split(",");
		if(strRet.length < 3)
		{
			debugSocket.getOutputStream().write(String.format("Invalid CMD(%s)", strMsg).getBytes());
			return;
		}
		
		// 找出透传给module的命令行
		String module_cmd = "";
		for (int i=2; i < strRet.length-1; i++) {
			module_cmd += strRet[i];
			module_cmd += ",";
		}
		module_cmd += strRet[strRet.length-1];
		
		String strDevId = strRet[1];
		String strInfos = String.format("================Pass through Module Text CMD of dev(%s)=============\n",strDevId)
				+ String.format("ID \t command \t Result\n");
		
		if (ServerWorkThread.IsModuleLogin(strDevId) == false) {
			strInfos += String.format("%s \t %s \t %s\n"
					,strDevId, module_cmd, "Fail: module not login");
		} else {
			/* 透传给模块 */
			try {
				/* 待模块返回 */
				boolean bRet = ResponseToModule(strDevId, module_cmd);
				strInfos += String.format("%s \t %s \t %s\n"
						,strDevId, module_cmd, bRet ? "Succeed" : String.format("Fail: send text cmd error"));
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SELF, e);
			}
		}

		debugSocket.getOutputStream().write(strInfos.getBytes());
	}
	
	private void Passthroughbin(String strMsg) throws ClassNotFoundException, SQLException, IOException
	{
		String strRet[] = strMsg.split(",");
		if(strRet.length < 3)
		{
			debugSocket.getOutputStream().write(String.format("Invalid CMD(%s)", strMsg).getBytes());
			return;
		}
		
		// 找出透传给module的命令行
		byte[] all_cmd = strMsg.getBytes();
		byte[] module_cmd = null;
		int comma_sum = 0;
		for (int i = 0; i < all_cmd.length; i++) {
			if (all_cmd[i] == 44) {  // 44: 表示 逗号 ，
				comma_sum++;
			}
			if (comma_sum == 2) {
				// copy i+1 -> all_cmd.length
				module_cmd = new byte[all_cmd.length - i -1];
				for (int j = i+1; j < all_cmd.length; j++) {
					module_cmd[j-i-1] = all_cmd[j];
				}
				break;
			}
		}
		
		String strDevId = strRet[1];
		String strInfos = String.format("================Pass through Module Bin CMD of dev(%s)=============\n",strDevId)
				+ String.format("ID \t command \t Result\n");
		
		if (ServerWorkThread.IsModuleLogin(strDevId) == false) {
			strInfos += String.format("%s \t %s \t %s\n"
					,strDevId, new String(module_cmd), "Fail: module not login");
		} else {
			/* 透传给模块 */
			boolean bRet = true;
			try {
				/* 待模块返回 */
				String strIP = ServerWorkThread.getModuleIp(strDevId);
				int strPort = ServerWorkThread.getModulePort(strDevId);
				byte[] buffer = new byte[1024];
				DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(strIP), strPort);    
				dataPacket.setData(module_cmd);  
				ServerMainThread.dataSocket.send(dataPacket);
				
				bRet = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SELF, e);
				 bRet = false;
			}
				
			strInfos += String.format("%s \t %s \t %s\n"
					,strDevId, new String(module_cmd), bRet ? "Succeed" : String.format("Fail: send bin cmd error"));
		}

		debugSocket.getOutputStream().write(strInfos.getBytes());
	}
	
	private void ShowHeartPackNum(String strMsg) throws IOException
	{
	}
	private void DebugHelpHandle() throws IOException
	{
		String strHelp = "=============help cmd list================\n"
								+ "ShowUser\t->show all user  info\n"
								+ "DeleteUser,<username>\t-> delete user\n"
								+ "ShowHeartPackNum,<module_id>\t-> show how many packs server received from smartplug with module_id\n"
								+ "ShowModule\t->show all module info.\n"
								+ "ShowTimer,<module_id>\t->show all timer info of device(module_id)\n"
//								+ "Passthroughtxt,<module_id>,<module_command>\t->pass throug module text command to device(module_id)\n"
//								+ "Passthroughbin,<module_id>,<module_command>\t->pass throug module bin command to device(module_id)\n"
		                        + "ShowOnlineModule \n";
		debugSocket.getOutputStream().write(strHelp.getBytes());
	}
	public void run() {
		// TODO Auto-generated method stub
		LogWriter.WriteTraceLog(LogWriter.SRV_SELF_LOG,String.format("debug Client %s connectd.",debugSocket.getRemoteSocketAddress().toString()));
		/*STEP1 解释字符串*/
		try {
			   
				while(true)
				{
					byte[] buffer = new byte[DEBUG_SERVER_BUFFER_SIZE];
				
					if(-1 == debugSocket.getInputStream().read(buffer))
					{
						debugSocket.close();
						LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG,String.format("Debug Request(%s) be closed.",debugSocket.getInetAddress().toString()));				   
						return;
					}
					String strRecord = StringConvertTool.Byte2String(buffer);
					LogWriter.WriteDebugLog(LogWriter.SRV_SELF_LOG,String.format("DebugWorkThread Receive Debug Commond(%s)", strRecord));
					SignalParse(strRecord);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG,e);
			}
		LogWriter.WriteTraceLog(LogWriter.SRV_SELF_LOG,String.format("debug Client %s quit.",debugSocket.getRemoteSocketAddress().toString()));
		
	}
	
	public boolean ResponseToModule(String strModuleID, String strInfo) 
	{
		String strCurIP = ServerWorkThread.getModuleIp(strModuleID);
		int int_port = ServerWorkThread.getModulePort(strModuleID);
		try {
			Response(strCurIP, int_port, strInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SEND, e, "[Debug] Send Msg To Module Fail." + strCurIP + ":" + int_port + " ModuleID:" + strModuleID + " Info:" + strInfo);
			return false;
		}
		LogWriter.WriteTraceLog(LogWriter.SEND, String.format("(%s:%d)\t\t [Debug] Send Msg To Module Succeed. [%s].", 
				strCurIP, int_port, strInfo));
		return true;
	}
	
	
	public void Response(String strIp, int port, String strRet) throws IOException
	{
		byte[] buffer = new byte[1024];
		DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(strIp), port);    
		dataPacket.setData(strRet.getBytes());  
		ServerMainThread.dataSocket.send(dataPacket);	
	}
	
	public void Response(String strIp, int port, byte[] info) throws IOException
	{
		byte[] buffer = new byte[1024];
		DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(strIp), port);    
		dataPacket.setData(info);  
		ServerMainThread.dataSocket.send(dataPacket);
	}

}
