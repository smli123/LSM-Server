package com.thingzdo.smartplug.udpserver.Function;

import com.mysql.jdbc.log.Log;
import com.sun.corba.se.impl.activation.ServerMain;
import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerMainThread;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_INFO;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class ModuleAirConServerCtrlMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name ParentCtrlRespHandle  status通用回应函数；所有对STATUS的变更，均由该函数响应 
	 * 			包含：1、继电器通/断开控制   2、USB控制  3、PARENT CTRL 4、
	 * @param 	strMsg: 命令字符串 
	 * 					格式：<cookie>,PARENTCTRL,<username>,<module_id>,<ctrl>
	 * @RET 		<new_cookie>,PARENTCTRL, <username>,<module_id>,<code>,<status>，<原命令字符串>
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg) 
	{
		String strRet[] 			= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie			= strRet[0].trim();
		String strMsgHeader			= strRet[1].trim();
		String strUserName 			= strRet[2].trim();
		String strModuleId			= strRet[3].trim();
		String strIRId				= strRet[4].trim();
		String strIRSubId			= strRet[5].trim();
	
		/* 校验参数合法性 */
		int iRet = CheckAppCmdValid(strUserName, strCookie);
		if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			ResponseToAPP(strMsgHeader, strUserName, strModuleId, iRet);
			return iRet;
		}
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
			USER_MODULE info = dbMgr.QueryUserModuleByDevId(strModuleId);
			if(null == info)
			{
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_USER_NOT_OWN_MODULE);
				return ServerRetCodeMgr.ERROR_CODE_USER_NOT_OWN_MODULE;
			}
			
			//被修改模块必须是已填加的模块
			MODULE_INFO module_info = dbMgr.QueryModuleInfo(strModuleId);
			if(null == module_info)
			{
				ResponseToAPP(strMsgHeader, info.getUserName(), ServerRetCodeMgr.ERROR_CODE_MODULE_ID_UNREGISTERED);
				return ServerRetCodeMgr.ERROR_CODE_MODULE_ID_UNREGISTERED;			
			}
		
			/* 透传给模块 */
			try {	
				/* 待模块返回 */
				
				String IRValue = ServerWorkThread.ReadCurrentAR(strIRId, strIRSubId);
				
				// Print the Temp Vlaue
				LogWriter.WriteTraceLog(LogWriter.SELF, String.format("IRID:%s, SubID:%s", strIRId, strIRSubId));
				LogWriter.WriteTraceLog(LogWriter.SELF, String.format("IRValue:%s", IRValue));
				
				String[] tempStr = IRValue.split(",");
				int IR_length = tempStr.length; 
				String strMsgTemp= strCookie + "," + ServerCommDefine.APP_AIRCON_CTRL_MSG_HEADER + "," + strUserName + "," + strModuleId + ",0,0," + String.valueOf(IR_length) + "," + IRValue + "#";
				
				iRet = ResponseToModule(strModuleId, changeCommand(strMsgTemp));
				if (ServerRetCodeMgr.SUCCESS_CODE != iRet)
				{
					return iRet;
				}

				return ServerRetCodeMgr.SUCCESS_CODE;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SELF, e);
			}

			return ServerRetCodeMgr.ERROR_COMMON;	
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
//		        String hv = Integer.toHexString(v);
//		        String hv = String.format("%2x", v).replace(" ", "0");
		        String hv = String.format("%2d", v).replace(" ", "0");
		        
				print_str = print_str + hv + " ";
			}
			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("IR_Send command:%s", new String(byte_info)));
			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("IR_Send command:%s", print_str));
			
		} else {
			 byte_info = cmd_text.getBytes();
			 LogWriter.WriteTraceLog(LogWriter.SELF, String.format("Normal command:%s", new String(byte_info)));
		}
		
		return byte_info;
	}
	
	

	/**********************************************************************************************************
	 * @name ParentCtrlRespHandle  status通用回应函数；所有对STATUS的变更，均由该函数响应 
	 * 			包含：1、继电器通/断开控制   2、USB控制  3、PARENT CTRL 4、
	 * @param 	strMsg: 命令字符串 
	 * 					格式：<cookie>,PARENTCTRL,<username>,<module_id>,<ctrl>
	 * @RET 		<new_cookie>,PARENTCTRL, <username>,<module_id>,<code>,<status>，<原命令字符串>
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	@Override
	public int resp(Runnable thread_base, String strMsg)
	{		
		// TODO Auto-generated method stub
		return 0;
	}
}
