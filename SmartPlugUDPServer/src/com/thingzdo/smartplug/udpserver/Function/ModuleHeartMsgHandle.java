package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.sql.SQLException;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerParamConfiger;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;

public class ModuleHeartMsgHandle  implements ICallFunction {
	/**********************************************************************************************************
	 * @name  处理模块的心跳
	 * @param 	strMsg: 响应字符串 
	 * 					格式：  cookie, HEART,<username>,< moduleid >
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
		
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		
		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("(%s:%d)\t Resv HEART", thread.getPacket().getAddress().toString(), 
				thread.getPacket().getPort()));
		
		ServerWorkThread.RefreshModuleAliveFlag(strDevId, true);
		ServerWorkThread.RefreshModuleIP(strDevId, thread.getSrcIP(), thread.getSrcPort());
		
		/* 为了定位模块的Socket接受不到消息的bug，临时增加代码调试使用，正式发布必须删除 */
		String strHeartRsp0 = String.format("%s,%s,%s,%s,%s#", strCookie, "Server_Echo", strUserName, strDevId, ServerWorkThread.IsModuleLogin(strDevId) ? "0" : "1");
		try {
			Response(thread.getSrcIP(), thread.getSrcPort(), strHeartRsp0);
			
			LogWriter.WriteDebugLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] Succeed to Send HEART Echo",
					thread.getPacket().getAddress().toString(), 
					thread.getPacket().getPort(),
					strHeartRsp0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			LogWriter.WriteErrorLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] Fail to Send HEART Echo",
					thread.getPacket().getAddress().toString(), 
					thread.getPacket().getPort(),
					strHeartRsp0));
			return ServerRetCodeMgr.ERROR_COMMON;
		}
		
//		 根据何晓旭要求，HearRecv 命令取消
//		if (!ServerWorkThread.IsModuleLogin(strDevId))
//		{
//			/* 未登录，返错 */
//			String strHeartRsp = String.format("%s,%s,%s,%s,1#", strCookie, strCmd, strUserName, strDevId);
//			try {
//				Response(thread.getSrcIP(), thread.getSrcPort(), strHeartRsp);
//				
//				LogWriter.WriteDebugLog(LogWriter.SEND, String.format("(%s:%d)\t [%s] Send HEART Succeed",
//						thread.getPacket().getAddress().toString(), 
//						thread.getPacket().getPort(),
//						strHeartRsp));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return ServerRetCodeMgr.ERROR_COMMON;
//			}
//			
//		}
		
		return ServerRetCodeMgr.SUCCESS_CODE;
	}

	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
}
