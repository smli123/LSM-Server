package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.PWDTool.PWDManagerTool;
import com.thingzdo.smartplug.udpserver.PubDefine;
import com.thingzdo.smartplug.udpserver.ServerMainThread;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_INFO;

public class AppRemotePrintMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name LoginMsgHandle 用户登录
	 * @param 	strMsg: 命令字符串 格式：<cookie>,ALOGIN,<username>,<version>,<pwd>
	 *                                                 返回：<new_cookie>,ALOGIN, <username>,<0>,<code>, [email]
	 * @return  boolean 是否注册成功
	 * @author zxluan
	 * @throws IOException 
	 * @date 2015/03/24
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg)
	{
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strMsgHeader	= strRet[1].trim();
		String strUserName	= strRet[2].trim();
		String strSwitch	= strRet[3].trim();
		
		ServerWorkThread app_thread = (ServerWorkThread)thread_base;
		
		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("App Remote Print(username:%s) ", strUserName));
		
    	if(strSwitch.equals("true") == true) {
    		PubDefine.DEFAULT_REMOTE_PRINT_SWITCH = true;
    		PubDefine.DEFAULT_REMOTE_PRINT_IP = app_thread.getSrcIP();
    		PubDefine.DEFAULT_REMOTE_PRINT_PORT = app_thread.getSrcPort();
    	} else {
    		PubDefine.DEFAULT_REMOTE_PRINT_SWITCH	= false;
    		PubDefine.DEFAULT_REMOTE_PRINT_IP	= "";
    		PubDefine.DEFAULT_REMOTE_PRINT_PORT	= 0;
    	}
	    return 0;
	}

	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
		
}
