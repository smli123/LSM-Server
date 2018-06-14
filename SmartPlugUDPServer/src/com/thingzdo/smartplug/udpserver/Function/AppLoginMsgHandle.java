package com.thingzdo.smartplug.udpserver.Function;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.PWDTool.PWDManagerTool;
import com.thingzdo.smartplug.udpserver.ServerMainThread;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_INFO;

public class AppLoginMsgHandle implements ICallFunction{
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
		String strPass		= strRet[4].trim();
		
		ServerWorkThread app_thread = (ServerWorkThread)thread_base;
		
		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Enter App Login(username:%s) ", strUserName));
		
		// Debug for new ServerDBMgr()
//		LogWriter.WriteDebugLog(LogWriter.SELF, 
//				String.format("(Before APPLogin new ServerDBMgr(), strUserName=(%s)", strUserName));
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		
//		LogWriter.WriteDebugLog(LogWriter.SELF, 
//				String.format("(After APPLogin new ServerDBMgr(), strUserName=(%s)", strUserName));
		
	    try
		{
	    	//STEP2 确认用户是否已注册
			USER_INFO user_info = dbMgr.QueryUserInfoByUserName(strUserName);
			if(null == user_info)
			{
				ResponseToAPPWithDefaultCookie(app_thread, strMsgHeader,  strUserName, 
						ServerCommDefine.DEFAULT_MODULE_ID,
						ServerRetCodeMgr.ERROR_CODE_USER_NAME_UNREGISTERED);
				return ServerRetCodeMgr.ERROR_CODE_USER_NAME_UNREGISTERED;				
			}
			
			//STEP3 确认用户密码错误
			if(!PWDManagerTool.validatePassword(user_info.getPassWord(), strPass))
			{
				ResponseToAPPWithDefaultCookie(app_thread, strMsgHeader,  strUserName, 
						ServerCommDefine.DEFAULT_MODULE_ID,
						ServerRetCodeMgr.ERROR_CODE_PASSWORD_ERROR);
				return ServerRetCodeMgr.ERROR_CODE_PASSWORD_ERROR;				
			}

			//STEP4 确认APP是否已登录
			if(ServerWorkThread.IsUserLogin(strUserName))
			{
				// 参数1#：表示强制退出；0#: 表示正常登陆的退出,参数2#表示用户未登录；
				String strCmd = String.format("%s,%s,%s,%s,1#",
						ServerCommDefine.DEFAULT_COOKIE, ServerCommDefine.APP_LOGOUT_MSG_HEADER, strUserName, "000000");
				String strCurIP = ServerWorkThread.GetCurAppIP(strUserName);
				int int_port = ServerWorkThread.GetCurAppPort(strUserName);
				
				//通知前用户，帐户在另一地方登录
				try {
					if (!(strCurIP.equalsIgnoreCase(app_thread.getSrcIP()) && (int_port == app_thread.getSrcPort())))
					{
						LogWriter.WriteDebugLog(LogWriter.SELF, 
								String.format("(old_user:[%s:%d],new_user:[%s:%d])\t [%s] user logined in other places. (%s)",
										strCurIP, int_port,
										app_thread.getSrcIP(), app_thread.getSrcPort(),
										strCmd,
										user_info.getUserName()));
						
						byte[] buffer = new byte[1024];
						DatagramPacket dp = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(strCurIP), int_port);    
						dp.setData(strCmd.getBytes());
						ServerMainThread.dataSocket.send(dp);
						
						LogWriter.WriteTraceLog(LogWriter.SEND, 
								String.format("(%s:%d)\t [%s] Succeed to Send Force_Logout(%s). ", 
										strCurIP, int_port,strCmd,strUserName));

						//挤掉当前用户
						ServerWorkThread.UnRegisterUserIP(strUserName);	
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LogWriter.WriteExceptionLog(LogWriter.SEND, e);
					
					LogWriter.WriteTraceLog(LogWriter.SEND, 
							String.format("(%s:%d)\t [%s] Fail to Send Force_Logout(%s). ", 
									strCurIP, int_port,strCmd,strUserName));
				}
			}
			
			//STEP5 注册
			LogWriter.WriteDebugLog(LogWriter.SELF, String.format("app username:%s, socket:(%s:%d)", 
					strUserName,app_thread.getSrcIP(),app_thread.getSrcPort()));
			
			ServerWorkThread.RegisterUserIP(strUserName, app_thread.getSrcIP(), app_thread.getSrcPort());

			//STEP6 给APP回信息
			ResponseToAPP(strMsgHeader, strUserName, ServerRetCodeMgr.SUCCESS_CODE, user_info.getEmail());
		
			LogWriter.WriteTraceLog(LogWriter.SELF, 
					String.format("(%s:%d)\t Succeed to Login(%s). ", 
					app_thread.getSrcIP(), app_thread.getSrcPort(), strUserName));
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

	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
		
}
