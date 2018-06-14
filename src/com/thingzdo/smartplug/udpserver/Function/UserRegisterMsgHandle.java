package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.PWDTool.PWDManagerTool;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_INFO;

public class UserRegisterMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name RegisterMsgHandle 新用户注册
	 * @param 	strMsg: 命令字符串 格式：<cookie>,REG,<username>,<version>,<pwd>,<email>
	 * @RET <new_cookie>,REG, <username>,<moduleid>,<code>
	 * @return  boolean 是否注册成功
	 * @author zxluan
	 * @date 2015/03/24
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg) 
	{
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strMsgHeader = strRet[1].trim();
		String strUserName	= strRet[2].trim();
		String strPass		= strRet[4].trim();
		String strEmail		= strRet[5].trim();
		
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
			//将用户密码MD5加密
			String pass_md5 = PWDManagerTool.generatePassword(strPass);
			
			if(dbMgr.IsUserNameExist(strUserName))
			{
				//APP用户名已注册
				ResponseToAPPWithDefaultCookie(thread, strMsgHeader,  strUserName, 
						ServerCommDefine.DEFAULT_MODULE_ID,
						ServerRetCodeMgr.ERROR_CODE_USER_NAME_REGISTERED);
				return ServerRetCodeMgr.ERROR_CODE_USER_NAME_REGISTERED;		
			}
			
			if(dbMgr.IsEmailExist(strEmail))
			{
				//APP邮箱已注册
				ResponseToAPPWithDefaultCookie(thread, strMsgHeader,  strUserName, 
						ServerCommDefine.DEFAULT_MODULE_ID,
						ServerRetCodeMgr.ERROR_CODE_EMAIL_REGISTERED);
				return ServerRetCodeMgr.ERROR_CODE_EMAIL_REGISTERED;	
			}
			
			//将用户名及密码写入数据库
			dbMgr.InsertUserInfo(new USER_INFO(strUserName,pass_md5,strEmail,ServerCommDefine.DEFAULT_COOKIE));
			
			ServerWorkThread.RegisterUserIP(strUserName, thread.getSrcIP(), thread.getSrcPort());
			
			//通知APP注册成功
			ResponseToAPP(strMsgHeader, strUserName, ServerRetCodeMgr.SUCCESS_CODE);
		
			LogWriter.WriteTraceLog(LogWriter.SELF, 
					String.format("(%s:%d)\t Succeed to Register(%s). ", 
					thread.getSrcIP(),thread.getSrcPort(),strUserName));
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
