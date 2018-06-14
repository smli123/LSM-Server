package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.sql.SQLException;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ConnectInfo;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerParamConfiger;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;

public class TransmitTransMsgHandle  implements ICallFunction {
	/**********************************************************************************************************
	 * @name  处理模块的心跳
	 * @param 	strMsg: 响应字符串 
	 * 					格式：  <cookie>,TRANSMIT_TRANS,<username>,<moduleid>,<dstmoduleid>,<content>
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg)
	{
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		
		String strRet[] 		= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie		= strRet[0].trim();
		String strCmd			= strRet[1].trim();
		String strUserName		= strRet[2].trim();
		String strDevId			= strRet[3].trim();
		String strDstDevId		= strRet[4].trim();
		
		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("(%s:%d)\t TransMsg:%s", thread.getPacket().getAddress().toString(), 
				thread.getPacket().getPort(), strMsg));
		
//		ServerWorkThread.RegisterTransmitIP(strDevId, strCookie, thread.getSrcIP(), thread.getSrcPort());
		
		ConnectInfo info = ServerWorkThread.getTransmitConnectInfo(strDstDevId);
		if(info == null) {
			LogWriter.WriteDebugLog(LogWriter.SELF, String.format("(%s:%d)\t [Transmit]Dest isn't exist.MSG:%s", thread.getPacket().getAddress().toString(), 
					thread.getPacket().getPort(), strMsg));
			return ServerRetCodeMgr.ERROR_COMMON;
		}

		strMsg = strMsg.substring(0, strMsg.indexOf("#")+1);
		
		// 转发给目标
		try {
			Response(info.getSrcIP(), info.getSrcPort(), strMsg);
			
			LogWriter.WriteDebugLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] Succeed to Transmit Msg",
					info.getSrcIP(), info.getSrcPort(), strMsg));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] Fail to Transmit Msg",
					thread.getPacket().getAddress().toString(), 
					thread.getPacket().getPort(),
					strMsg));
			return ServerRetCodeMgr.ERROR_COMMON;
		}
		
		// 反馈给源
		String strHeartRsp0 = String.format("%s,%s,%s,%s,%s#", strCookie, "TRANSMIT_TRANS_Echo", strUserName, strDevId, "0");
		try {
			Response(thread.getSrcIP(), thread.getSrcPort(), strHeartRsp0);
			
			LogWriter.WriteDebugLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] Succeed to Transmit Msg",
					thread.getPacket().getAddress().toString(), 
					thread.getPacket().getPort(),
					strHeartRsp0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] Fail to Transmit Msg",
					thread.getPacket().getAddress().toString(), 
					thread.getPacket().getPort(),
					strHeartRsp0));
			return ServerRetCodeMgr.ERROR_COMMON;
		}
		
		return ServerRetCodeMgr.SUCCESS_CODE;
	}

	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
}
