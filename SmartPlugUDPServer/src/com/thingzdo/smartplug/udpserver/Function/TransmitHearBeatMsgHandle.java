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

public class TransmitHearBeatMsgHandle  implements ICallFunction {
	/**********************************************************************************************************
	 * @name  处理转发器的心跳
	 * @param 	strMsg: 响应字符串 
	 * 					格式：  cookie, TRANSMIT_HEARBEAT,<username>,< moduleid >,<alive>		// 0: logout, 1: login
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg)
	{
		String strRet[] 		= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie		= strRet[0].trim();
		String strCmd			= strRet[1].trim();
		String strUserName		= strRet[2].trim();
		String strDevId			= strRet[3].trim();
		String strEnable		= strRet[4].trim();
		boolean bAlive 			= (strEnable.equalsIgnoreCase("1") ? true : false);
		
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		
		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("(%s:%d)\t Transmit HEART", thread.getPacket().getAddress().toString(), 
				thread.getPacket().getPort()));
		
		if (bAlive == true) {
			// 注册
			if (!ServerWorkThread.RegisterTransmitIP(strDevId, strCookie, thread.getSrcIP(), thread.getSrcPort())) {
				return ServerRetCodeMgr.ERROR_COMMON; 
			}
		} else {
			// 注销
			ServerWorkThread.UnRegisterTransmitIP(strDevId);
		}
		
		String[] ids = ServerWorkThread.getAllTransmitIDs();
		String idfriends = ServerWorkThread.getAllTransmitID ();
		String strHeartRsp0 = String.format("%s,%s,%s,%s,%s,%s#", strCookie, "Transmit_Hearbeat_Echo", strUserName, strDevId, "1", idfriends);
		try {
			// 通知所有客户端
			for (int i = 0; i < ids.length; i++) {
				ConnectInfo info = ServerWorkThread.getTransmitConnectInfo(ids[i]);
				Response(info.getSrcIP(), info.getSrcPort(), strHeartRsp0);
			}
			
			// 只通知发送消息的客户端
//			Response(thread.getSrcIP(), thread.getSrcPort(), strHeartRsp0);
			LogWriter.WriteDebugLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] Succeed to Send Transmit HEART Echo",
					thread.getPacket().getAddress().toString(), 
					thread.getPacket().getPort(),
					strHeartRsp0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] Fail to Send Transmit HEART Echo",
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
