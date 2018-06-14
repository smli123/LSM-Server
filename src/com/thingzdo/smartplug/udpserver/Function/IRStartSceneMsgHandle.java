package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.PWDTool.PWDManagerTool;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_IRSCENE;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.TIMER_INFO;
import com.thingzdo.smartplug.udpserver.db.USER_INFO;

public class IRStartSceneMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name UpdatePWDHandle 修改用户密码
	 * @param 	strMsg: 命令字符串 格式：<new_cookie>,STARTSCENE,<username>,<moduleid>,<空调名称>,<sceneid>#
	 * @RET 		<new_cookie>,STARTSCENE,<username>,<moduleid>,<空调名称>,<sceneid>#
	 *                  其中return code: 0表示成功，其它：错误码
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg) 
	{
		return 0;
	}

	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strNewCookie	= strRet[0].trim();
		String strMsgHeader	= strRet[1].trim();
		String strUserName 	= strRet[2].trim();
		String strModuleId	= strRet[3].trim();
		String strIRName	= strRet[5].trim();
		int iID 			= Integer.valueOf(strRet[6].trim());
		
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		
		/* 更新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);
		/* 刷新心跳状态 */
		ServerWorkThread.RefreshModuleAliveFlag(strModuleId, true);
		ServerWorkThread.RefreshModuleIP(strModuleId, thread.getSrcIP(), thread.getSrcPort());

		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
			MODULE_IRSCENE info = dbMgr.QueryIRSceneInfo(strModuleId, iID);
			int ipower = info.getPower();
			int imode = info.getMode();
			int idirection = info.getDirection();
			int iScale = info.getScale();
			int iTemp = info.getTemperature();
			String strIRId = info.getIRName();
			String strSubIRName = "";
			
			if (ipower == MODULE_IRSCENE.POWER_ON) {
//				// POWER ON
//				strSubIRName = "power_on";
//				sendIRMessage(strNewCookie, strUserName, strModuleId, strIRId, strSubIRName);
				
//				// SET MODE
//				strSubIRName = "";
//				if (imode == MODULE_IRSCENE.MODE_AUTO) {
//					strSubIRName = "mode_auto";
//				} else if (imode == MODULE_IRSCENE.MODE_COOL) {
//					strSubIRName = "mode_cool";
//				} else if (imode == MODULE_IRSCENE.MODE_WET) {
//					strSubIRName = "mode_wet";
//				} else if (imode == MODULE_IRSCENE.MODE_WIND) {
//					strSubIRName = "mode_wind";
//				} else if (imode == MODULE_IRSCENE.MODE_WARM) {
//					strSubIRName = "mode_warm";
//				}
//				sendIRMessage(strNewCookie, strUserName, strModuleId, strIRId, strSubIRName);
//
//				// SET SCALE
//				strSubIRName = "";
//				if (iScale == MODULE_IRSCENE.WINDSCALE_AUTO) {
//					strSubIRName = "scale_auto";
//				} else if (iScale == MODULE_IRSCENE.WINDSCALE_BIG) {
//					strSubIRName = "scale_big";
//				} else if (iScale == MODULE_IRSCENE.WINDSCALE_MIDDLE) {
//					strSubIRName = "scale_middle";
//				} else if (iScale == MODULE_IRSCENE.WINDSCALE_SMALL) {
//					strSubIRName = "scale_small";
//				}
//				sendIRMessage(strNewCookie, strUserName, strModuleId, strIRId, strSubIRName);
//
//				// SET DIRECTION
//				strSubIRName = "";
//				if (idirection == MODULE_IRSCENE.DIRECTIOIN_ON) {
//					strSubIRName = "direction_on";
//				} else if (idirection == MODULE_IRSCENE.DIRECTIOIN_OFF) {
//					strSubIRName = "direction_off";
//				}
//				sendIRMessage(strNewCookie, strUserName, strModuleId, strIRId, strSubIRName);

				// SET TEMPERATURE
				strSubIRName = "temp_" + String.valueOf(iTemp); 
				sendIRMessage(strNewCookie, strUserName, strModuleId, strIRId, strSubIRName);
			
			} else if (ipower == MODULE_IRSCENE.POWER_OFF) {
				strSubIRName = "power_off";
				sendIRMessage(strNewCookie, strUserName, strModuleId, strIRId, strSubIRName);
				
			} else {
				// do nothing...
			}
			
			return ServerRetCodeMgr.SUCCESS_CODE;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ServerRetCodeMgr.ERROR_COMMON;
		}
		finally
		{
			dbMgr.Destroy();
		}
	}
	
	private int sendIRMessage (String strCookie, String strUserName, String strModuleId, String strIRId, String strSubIRName) {
		try {	
			String strIRSubId = ServerWorkThread.getIRSubId(strSubIRName);
			if(strIRSubId == null) {
				LogWriter.WriteErrorLog(LogWriter.SELF, 
						String.format("Get IR Sub is null (%s).", strSubIRName));
				return -1;
			}
			
			String IRValue = ServerWorkThread.ReadCurrentAR(strIRId, strIRSubId);
			String[] tempStr = IRValue.split(",");
			int IR_length = tempStr.length; 
			String strMsgTemp= strCookie + "," + ServerCommDefine.APP_AIRCON_CTRL_MSG_HEADER + "," + strUserName + "," + strModuleId + ",0,0," + String.valueOf(IR_length) + "," + IRValue + "#";
			
			int iRet = ResponseToModule(strModuleId, changeCommand(strMsgTemp));
			if (ServerRetCodeMgr.SUCCESS_CODE != iRet)
			{
				return iRet;
			}
	
			return ServerRetCodeMgr.SUCCESS_CODE;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e);
			return ServerRetCodeMgr.ERROR_COMMON;
		}
	}
	

	private byte[] changeCommand(String cmd_text) {
//		final int BUF_SIZE = 1024;  	// 定义发送的二进制BUFFER的大小。
		
		byte[] byte_info;		
		String revMsg = cmd_text.substring(0, cmd_text.indexOf("#"));
		String arrays[] = revMsg.split(",");
		String cmd = arrays[1];
		int i = 0;
		int j = 0;
		
		// 红外命令单独处理
		if (arrays[1].equals("APPAIRCON") == true) {
			//byte_info = new byte[BUF_SIZE];
			
			// 临时Debug，要删除
			int count = 0;
			int length = Integer.parseInt(arrays[6]);
			for (j = 0; j < length; j++) {
				int value = Integer.parseInt(arrays[7 + j]);
				char a = (char)((value >> 8) & 0xFF);
				char b = (char)(value & 0xFF);
				count = count + a + b;
			}
			
			String strHeader = arrays[0] + "," + arrays[1] + "," + arrays[2] + "," + arrays[3] + "," + arrays[4] + "," + arrays[5] + "," +  arrays[6] + "," + count + ",";
			byte[] temp = strHeader.getBytes();
			
			byte_info = new byte[temp.length + Integer.parseInt(arrays[6]) * 2 + 1];
			
			for (i = 0; i < temp.length; i++) {
				byte_info[i] = temp[i];
			}
			
			length = Integer.parseInt(arrays[6]);
			for (j = 0; j < length; j++) {
				int value = Integer.parseInt(arrays[7 + j]);
				
				byte_info[i++] = (byte)((value >> 8) & 0xFF);
				byte_info[i++] = (byte)(value & 0xFF);
			}
			
			byte_info[i++] = '#';
			
//			// 增加 \0 空数据
//			for (; i < BUF_SIZE; i++) {
//				byte_info[i] = 0;
//			}
			
			// DEBUG： 只是为了打印调试信息
			String print_str = "";
			for (i = 0; i < byte_info.length; i++) {
				int v = byte_info[i] & 0xFF;
		        String hv = Integer.toHexString(v);
		        
				print_str = print_str + hv + ",";
			}
			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("IR_Send command:%s", new String(byte_info)));
			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("IR_Send command:%s", print_str));
			
		} else {
			 byte_info = cmd_text.getBytes();
			 LogWriter.WriteTraceLog(LogWriter.SELF, String.format("Normal command:%s", new String(byte_info)));
		}
		
		return byte_info;
	}
	
}
