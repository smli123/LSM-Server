package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;

public class ModuleBellOnMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name ModuleBellOnMsgHandle 通用命令发送处理句柄
	 * @param 	strMsg: 命令字符串 
	 * 					格式：<cookie>,BELLON,<username>,<module_id>
	 * @RET 		不需要模块响应
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg) 
	{
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strMsgHeader	= strRet[1].trim();
		String strUserName 	= strRet[2].trim();
		String strModuleId	= strRet[3].trim();
		
		/* 获取用户线程 */
		ServerWorkThread thread = (ServerWorkThread)thread_base;
				
		/* 校验参数合法性 */
		int iRet = CheckAppCmdValid(strUserName, strCookie);
		if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			ResponseToAPP(strMsgHeader, strUserName, strModuleId, iRet);
			return iRet;
		}
		
		/* 透传给模块 */
		try {
			NotifyToModule(strMsg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e);
			
			LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t App(%s) Fail to BellOn of module(%s). ", 
					thread.getSrcIP(),thread.getSrcPort(),strUserName,strModuleId));
			
			return ServerRetCodeMgr.ERROR_COMMON;
		}

		LogWriter.WriteTraceLog(LogWriter.SELF, String.format("(%s:%d)\t App(%s) Succeed to BellOn of module(%s). ", 
				thread.getSrcIP(),thread.getSrcPort(),strUserName,strModuleId));
		
		return ServerRetCodeMgr.SUCCESS_CODE;
	}

	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		
		return 0;
	}	
}
