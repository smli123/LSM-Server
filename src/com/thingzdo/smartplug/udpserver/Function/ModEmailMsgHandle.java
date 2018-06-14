package com.thingzdo.smartplug.udpserver.Function;

import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_INFO;

public class ModEmailMsgHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name UpdateEmailHandle 修改用户邮箱
	 * @param 	strMsg: 命令字符串 格式：cookie,MODEMAIL,<username>,<newemail>
	 * @RET 		<new_cookie>,MODEMAIL, <username>,<0>,<code> 其中return code: 0表示成功，其它：错误码
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/07
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg) 
	{
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strCmd		= strRet[1].trim();
		String strUserName	= strRet[2].trim();
		String strEmail		= strRet[3].trim();
		
		/* 校验参数合法性 */
		int iRet = CheckAppCmdValid(strUserName, strCookie);
		if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
		{
			ResponseToAPP(strCmd, strUserName, ServerCommDefine.DEFAULT_MODULE_ID, iRet);
			return iRet;
		}	
		
		ServerDBMgr dbMgr = new ServerDBMgr();
		
		try
		{
			//更新新邮件
			USER_INFO user_info = dbMgr.QueryUserInfoByUserName(strUserName);
			user_info.setEmail(strEmail);
			if(false == dbMgr.UpdateUserEmail(user_info))
			{
				ResponseToAPP(strCmd, strUserName, ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION);
				return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
			}

			//更新成功
			ResponseToAPP(strCmd, strUserName, ServerRetCodeMgr.SUCCESS_CODE);
			
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
