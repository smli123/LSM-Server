package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ModuleUpgradeOnLineMgr;
import com.thingzdo.smartplug.udpserver.ServerMainThread;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;

//命令行格式：
//20161021023113,AppPassThrough,windhz,16291752,23,         23*&#!3()#
//0                 1            2           3  4           5
//cookie          cmd_name      user     id     cmd_count   cmd_content

public class AppPassThroughMsgHandle implements ICallFunction{
	@Override
	public int call(Runnable thread_base, String strMsg) {
		String strRet[] 			= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie			= strRet[0].trim();
		String strMsgHeader			= strRet[1].trim();
		String strUserName 			= strRet[2].trim();
		String strModuleId			= strRet[3].trim();
		int cmd_byte_count			= Integer.valueOf(strRet[4].trim());
	
		/* 校验参数合法性 */
		int iRet = ServerRetCodeMgr.SUCCESS_CODE;
		
		/* 启动 Upgrade start 定时器 */
		ServerWorkThread thread = (ServerWorkThread)thread_base;

		String strRetbak[] = strMsg.split(",");
		if(strRetbak.length < 6)
		{
			LogWriter.WriteTraceLog(LogWriter.SELF, 
					String.format("(%s:%d)\t App(%s) Invalid CMD(%s). ", 
							thread.getSrcIP(),thread.getSrcPort(),strUserName,strMsg));
			return ServerRetCodeMgr.ERROR_COMMON;
		}
		
		// 找出透传给module的命令行
		byte[] all_cmd = strMsg.getBytes();
		byte[] module_cmd = null;
		int comma_sum = 0;
		int i = 0;
		for (i = 0; i < all_cmd.length; i++) {
			if (all_cmd[i] == 44) {  	// 44: 表示 逗号 ，
				comma_sum++;
			}
			if (comma_sum == 5) {		// 表示第5个逗号后面的为：要透传给MODULE的全部命令
				// copy i+1 -> all_cmd.length
				module_cmd = new byte[all_cmd.length - i -1];
				for (int j = i+1; j < all_cmd.length; j++) {
//					if (j-i-1 > cmd_byte_count) {
//						break;
//					}
					module_cmd[j-i-1] = all_cmd[j];
				}
				break;
			}
		}
		//lishimin Debug need delete
		int k = 0;
		String str_all_cmd = "";
		String str_mod_cmd = "";
		for (k = 0; k < all_cmd.length; k++) {
			str_all_cmd = str_all_cmd + String.format("%x ", all_cmd[k]);
		}
		for (k = 0; k < module_cmd.length; k++) {
			str_mod_cmd = str_mod_cmd + String.format("%x ", module_cmd[k]);
		}
		LogWriter.WriteTraceLog(LogWriter.SELF, String.format("[count] all:%d i:%d left:%d, mod:%d", all_cmd.length, i, all_cmd.length - i -1,module_cmd.length));
		LogWriter.WriteTraceLog(LogWriter.SELF, String.format("All_Cmd:%s", str_all_cmd));
		LogWriter.WriteTraceLog(LogWriter.SELF, String.format("Mod_Cmd:%s", str_mod_cmd));
		
		LogWriter.WriteTraceLog(LogWriter.SELF, 
				String.format("(%s:%d)\t Module(%s) byte_count: [%d], all_cmd_i_1: [%d]. ", 
						thread.getSrcIP(),thread.getSrcPort(),strModuleId, cmd_byte_count, all_cmd.length - i -1));
		
		LogWriter.WriteTraceLog(LogWriter.SELF, 
				String.format("(%s:%d)\t Module(%s) PassThrough's CMD: [%s]. ", 
						thread.getSrcIP(),thread.getSrcPort(),strModuleId,new String(module_cmd)));
		
		if ( thread.IsModuleLogin(strModuleId) == false ) {
			LogWriter.WriteTraceLog(LogWriter.SELF, 
					String.format("(%s:%d)\t Module(%s) Error：Module(%s) is not login. ", 
							thread.getSrcIP(),thread.getSrcPort(),strUserName,strModuleId));
			try {
				Response(thread.getSrcIP(),thread.getSrcPort(), String.format("(%s:%d)\t Module(%s) Error：Module(%s) is not login. "));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if ( (thread.IsModuleLogin(strModuleId) == false  && false) ) {
			LogWriter.WriteTraceLog(LogWriter.SELF, 
					String.format("(%s:%d)\t Module(%s) Error：Module(%s) is not login. ", 
							thread.getSrcIP(),thread.getSrcPort(),strUserName,strModuleId));
			try {
				Response(thread.getSrcIP(),thread.getSrcPort(), String.format("(%s:%d)\t Module(%s) Error：Module(%s) is not login. "));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ServerRetCodeMgr.ERROR_CODE_MODULE_ID_UNREGISTERED;
		} else {
			/* 透传给模块 */
			try {
				/* 待模块返回 */
				String strIP = thread.getModuleIp(strModuleId);
				int strPort = thread.getModulePort(strModuleId);
				
				// 针对透传的APPAIRCONSERVER特殊处理
				String str_Module_Cmd = String.valueOf(module_cmd);
				str_Module_Cmd = str_Module_Cmd.substring(0, str_Module_Cmd.lastIndexOf("#") + 1);
				String[] module_cmds = str_Module_Cmd.split(ServerCommDefine.CMD_SPLIT_STRING);
				String Module_Msg = module_cmds[1].trim();
				
				LogWriter.WriteTraceLog(LogWriter.SELF, 
						String.format("Select (%s: %s)",module_cmd,Module_Msg));
				
				if(Module_Msg.equals("APPAIRCONSERVER") == true) {
					LogWriter.WriteTraceLog(LogWriter.SELF, 
							String.format("(%s:%d)\t ReSendToServer(%s)", 
									"127.0.0.1",5000,module_cmd));
					Response("127.0.0.1", 5000, module_cmd);
					return ServerRetCodeMgr.SUCCESS_CODE;
				} else {
				
					byte[] buffer = new byte[1024];
					DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(strIP), strPort);    
					dataPacket.setData(module_cmd);
					ServerMainThread.dataSocket.send(dataPacket);
					
					LogWriter.WriteTraceLog(LogWriter.SELF, 
							String.format("(%s:%d)\t Module(%s) Send to Module cmd succeed. ", 
									thread.getSrcIP(),thread.getSrcPort(),strModuleId));
					
					return ServerRetCodeMgr.SUCCESS_CODE;
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SELF, e);
				
				LogWriter.WriteTraceLog(LogWriter.SELF, 
						String.format("(%s:%d)\t Module(%s) Send to Module cmd Fail. ", 
								thread.getSrcIP(),thread.getSrcPort(),strModuleId));
			}
		}
		
		return ServerRetCodeMgr.ERROR_COMMON;
	}

	// 命令行格式：
	//20161021023113,AppPassThrough,windhz,16291752,23,         23*&#!3()#
	//0                 1            2           3  4           5
	//cookie          cmd_name      user     id     cmd_count   cmd_content

		@Override
		public int call(Runnable thread_base, String strMsg, byte[] strMsgBin) {
			String strRet[] 			= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
			String strCookie			= strRet[0].trim();
			String strMsgHeader			= strRet[1].trim();
			String strUserName 			= strRet[2].trim();
			String strModuleId			= strRet[3].trim();
			int cmd_byte_count			= Integer.valueOf(strRet[4].trim());
		
			/* 校验参数合法性 */
			int iRet = ServerRetCodeMgr.SUCCESS_CODE;
			
			/* 启动 Upgrade start 定时器 */
			ServerWorkThread thread = (ServerWorkThread)thread_base;

			String strRetbak[] = strMsg.split(",");
			if(strRetbak.length < 6)
			{
				LogWriter.WriteTraceLog(LogWriter.SELF, 
						String.format("(%s:%d)\t App(%s) Invalid CMD(%s). ", 
								thread.getSrcIP(),thread.getSrcPort(),strUserName,strMsg));
				return ServerRetCodeMgr.ERROR_COMMON;
			}
			
			// 找出透传给module的命令行
			byte[] all_cmd = strMsgBin;
			byte[] module_cmd = null;
			int comma_sum = 0;
			int i = 0;
			for (i = 0; i < all_cmd.length; i++) {
				if (all_cmd[i] == 44) {  	// 44: 表示 逗号 ，
					comma_sum++;
				}
				if (comma_sum == 5) {		// 表示第5个逗号后面的为：要透传给MODULE的全部命令
					// copy i+1 -> all_cmd.length
					module_cmd = new byte[all_cmd.length - i -1];
					for (int j = i+1; j < all_cmd.length; j++) {
//						if (j-i-1 > cmd_byte_count) {
//							break;
//						}
						module_cmd[j-i-1] = all_cmd[j];
					}
					break;
				}
			}
//			//lishimin Debug need delete
//			int k = 0;
//			String str_all_cmd = "";
//			String str_mod_cmd = "";
//			for (k = 0; k < all_cmd.length; k++) {
//				str_all_cmd = str_all_cmd + String.format("%x ", all_cmd[k]);
//			}
//			for (k = 0; k < module_cmd.length; k++) {
//				str_mod_cmd = str_mod_cmd + String.format("%x ", module_cmd[k]);
//			}
//			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("[count_bin] all:%d i:%d left:%d, mod:%d", all_cmd.length, i, all_cmd.length - i -1,module_cmd.length));
//			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("All_Cmd:%s", str_all_cmd));
//			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("Mod_Cmd:%s", str_mod_cmd));
//			
//			LogWriter.WriteTraceLog(LogWriter.SELF, 
//					String.format("(%s:%d)\t Module(%s) byte_count: [%d], all_cmd_i_1: [%d]. ", 
//							thread.getSrcIP(),thread.getSrcPort(),strModuleId, cmd_byte_count, all_cmd.length - i -1));
			
			LogWriter.WriteTraceLog(LogWriter.SELF, 
					String.format("(%s:%d)\t Module(%s) PassThrough's CMD: [%s]. ", 
							thread.getSrcIP(),thread.getSrcPort(),strModuleId,new String(module_cmd)));
			

			// 针对透传的APPAIRCONSERVER特殊处理
			String str_Module_Cmd = new String(module_cmd);
			str_Module_Cmd = str_Module_Cmd.substring(0, str_Module_Cmd.lastIndexOf("#") + 1);
			String[] module_cmds = str_Module_Cmd.split(ServerCommDefine.CMD_SPLIT_STRING);
			String Module_Msg = module_cmds[1].trim();
			
			try {
				if(Module_Msg.equals("APPAIRCONSERVER") == true) {
					LogWriter.WriteTraceLog(LogWriter.SELF, 
							String.format("(%s:%d)\t ReSendToServer(%s)", 
									"127.0.0.1", 5000, str_Module_Cmd));
					Response("127.0.0.1", 5000, module_cmd);
					return ServerRetCodeMgr.SUCCESS_CODE;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SELF, e);
			}
			
			if ( thread.IsModuleLogin(strModuleId) == false) {
				String tmp_strMsg = String.format("(%s:%d)\t UserName(%s) Error：Module(%s) is not login. ", 
						thread.getSrcIP(),thread.getSrcPort(),strUserName,strModuleId);
				LogWriter.WriteTraceLog(LogWriter.SELF, tmp_strMsg);
						
				try {
					Response(thread.getSrcIP(),thread.getSrcPort(), tmp_strMsg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
//			if ( (thread.IsModuleLogin(strModuleId) == false) && false) {
//				LogWriter.WriteTraceLog(LogWriter.SELF, 
//						String.format("(%s:%d)\t UserName(%s) Error：Module(%s) is not login. ", 
//								thread.getSrcIP(),thread.getSrcPort(),strUserName,strModuleId));
//				return ServerRetCodeMgr.ERROR_CODE_MODULE_ID_UNREGISTERED;
//			} else 
			{
				/* 待模块返回 */
				String strIP = thread.getModuleIp(strModuleId);
				int iPort = thread.getModulePort(strModuleId);
				
				/* 透传给模块 */
				try {
					byte[] buffer = new byte[1024];
					DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(strIP), iPort);    
					dataPacket.setData(module_cmd);
					ServerMainThread.dataSocket.send(dataPacket);
					
					LogWriter.WriteTraceLog(LogWriter.SELF, 
							String.format("(%s:%d)\t Module(%s) APPPassthrought Send to Module cmd succeed. ", 
									strIP,iPort,strModuleId));
					
					return ServerRetCodeMgr.SUCCESS_CODE;
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LogWriter.WriteExceptionLog(LogWriter.SELF, e);
					
					LogWriter.WriteTraceLog(LogWriter.SELF, 
							String.format("(%s:%d)\t APPPassthrought Module(%s) Send to Module cmd Fail. ", 
									strIP,iPort,strModuleId));
				}
			}
			
			return ServerRetCodeMgr.ERROR_COMMON;
		}
		
	/**********************************************************************************************************
	 * @name UpgradeStrartRspHandle 模板响应软件升级请求
	 * @param cookiID,UPGRADESTART,usrname,moduleID,ret_code,userbinno,Version,blocksize#
		userbinno=0或者1，0表示user1.bin，1表示user2.bin
		Version=YYYYMMDDHHMMSS_V1.0
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg) 
	{		
		// TODO Auto-generated method stub
		return 0;
	}

}
