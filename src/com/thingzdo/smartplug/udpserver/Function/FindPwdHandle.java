package com.thingzdo.smartplug.udpserver.Function;

import java.util.Random;
import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.MailTool.MailSendTool;
import com.thingzdo.platform.PWDTool.PWDManagerTool;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_INFO;

public class FindPwdHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name FindPwdHandle 密码找回
	 * @param 	strMsg: 命令字符串 格式：<cookie>,FINDPWD,<username>,<email>
	 * @RET 		<cookie>,FINDPWD,<return code> 其中return code: 0表示成功，其它：错误码
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg)
	{
		String strRet[] 		= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strMsgHeader	= strRet[1].trim();
		String strUserName	= strRet[2].trim();
		String strEmail		= strRet[3].trim();

		ServerWorkThread thread = (ServerWorkThread)thread_base;
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
			//验证该邮箱是否已注册
			USER_INFO user_info = dbMgr.QueryUserInfoByEmail(strEmail);
			if(null == user_info)
			{
				ResponseToAPPWithDefaultCookie(thread, strMsgHeader,  strUserName, strEmail, 
						ServerRetCodeMgr.ERROR_CODE_USER_NAME_UNREGISTERED);
				return ServerRetCodeMgr.ERROR_CODE_USER_NAME_UNREGISTERED;		
			}
			
			//生成随机密码
			String strRandomPwd = GenerateRandomPassword(10);
			
			//将随机密码加密
			String strPwdMD5 = PWDManagerTool.generatePassword(strRandomPwd);
			
			//将密码写入数据库
			user_info.setPassWord(strPwdMD5);
			
			boolean bRet = dbMgr.UpdateUserPWD(user_info);
			if(!bRet)
			{
				ResponseToAPPWithDefaultCookie(thread, strMsgHeader,  strUserName, strEmail, 
						ServerRetCodeMgr.ERROR_CODE_FAILED_UPDATE_PWD);
				return ServerRetCodeMgr.ERROR_CODE_FAILED_UPDATE_PWD;
			}
			
			//将新密码发至邮箱
			MailSendTool sender = new MailSendTool(strEmail);
			sender.SetSubject("<ThingzDo>【重要】密码重置邮件");
			String strContent = String.format("亲爱的%s:\r\n"
					+ "\t您好，您重置后的密码为%s,为保证您的用户安全，建议您立即登录并修改密码。\r\n"
					+ "\t非常感谢您使用本公司产品，谢谢。\r\n"
					+ "\t\t\t\t From 鑫思度科技股份有限公司", user_info.getUserName(),strRandomPwd);
			sender.SetContent(strContent);
		
			//回复服务器
			if(false == sender.Send())
			{
				ResponseToAPPWithDefaultCookie(thread, strMsgHeader,  strUserName, strEmail, 
						ServerRetCodeMgr.ERROR_CODE_FAILED_SEND_EMAIL);
				
				LogWriter.WriteErrorLog(LogWriter.SELF, 
						String.format("(%s:%d)\t Fail to send password reset mail to (%s). ",
								thread.getSrcIP(), thread.getSrcPort(), strEmail));
				return ServerRetCodeMgr.ERROR_CODE_FAILED_SEND_EMAIL;
			}

			ResponseToAPPWithDefaultCookie(thread, strMsgHeader, strUserName, strEmail, ServerRetCodeMgr.SUCCESS_CODE);
			
			LogWriter.WriteDebugLog(LogWriter.SELF, 
					String.format("(%s:%d)\t Success to send password reset mail to (%s). ",
							thread.getSrcIP(), thread.getSrcPort(), strEmail));
			
			return ServerRetCodeMgr.SUCCESS_CODE;
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ServerRetCodeMgr.ERROR_COMMON;
		}
		finally
		{
			dbMgr.Destroy();
		}
	}
	
	private String GenerateRandomPassword(int pwd_len)
	{
	  //密码数据源
//	  char[] strSource = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k','l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w','x', 'y', 'z', 
//			  							'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
//			  							'~','!','@','#','$','%','^','&','*','_','+','-','=',
//			  							'0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	  /* 降低密码的复杂度 */
	  char[] strSource = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	  final int  MAX_VALUE = strSource.length;
	  
	  StringBuffer pwd = new StringBuffer("");
	  Random generator = new Random();
	  int count = 0; //生成的密码的长度
	  while(count < pwd_len)
	  {
		  //生成随机数，取绝对值，防止 生成负数，
	  	int i = Math.abs(generator.nextInt(MAX_VALUE)); 
	   
	  	//将随机数对应的字符写入密码字符串中
	  	if (i >= 0 && i < strSource.length) {
	  		pwd.append(strSource[i]);
	  		count ++;
	  	}
	  }
		  
	  return pwd.toString();
	}

	@Override
	public int resp(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}

}
