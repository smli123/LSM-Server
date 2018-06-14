package com.thingzdo.smartplug.udpserver.Function;

import java.sql.SQLException;
import java.util.Vector;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.MODULE_INFO;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.TIMER_INFO;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class IRQueryTVIRDataHandle implements ICallFunction{
	/**********************************************************************************************************
	 * @name QueryIRDataHandle 鏌ヨ鎸囧畾鐢ㄦ埛鍚嶄笅鐨勬墍鏈夋ā鍧椾俊鎭�
	 * @param 	strMsg: 鍛戒护瀛楃涓� 鏍煎紡锛歝ookie,QRYIRDATA,<username>
	 * @RET 		<new cookie>,QRYIRDATA, <username>,<ModuleID>
	 * @return  boolean 鏄惁鎴愬姛
	 * @author zxluan
	 * @date 2015/04/12
	 * **********************************************************************************************************/
	public int call(Runnable thread_base, String strMsg) 
	{	
		final int IR_DATA_AIRCON_ALL_NAME  = 1;
		final int IR_DATA_AIRCON_SAVE_NAME = 2;
		final int IR_DATA_AIRCON_LOAD_NAME = 3;

		//鍙栧��
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strMsgHeader = strRet[1].trim();
		String strUserName 	= strRet[2].trim();
		String strModuleId 	= strRet[3].trim();
		int i_irdata_type	= Integer.valueOf(strRet[4].trim());

		ServerDBMgr dbMgr = new ServerDBMgr();
		try
		{

			/* 鏍￠獙鍙傛暟鍚堟硶鎬� */
			int iRet = CheckAppCmdValid(strUserName, strCookie);
			if( ServerRetCodeMgr.SUCCESS_CODE != iRet)
			{
				ResponseToAPP(strMsgHeader, strUserName, ServerCommDefine.DEFAULT_MODULE_ID, iRet);
				return iRet;
			}

			if (i_irdata_type == IR_DATA_AIRCON_ALL_NAME) {			// 获取所有的电视名字
				String strIRList = ServerWorkThread.getTVIRSet();
				String s_strIRList = strRet[4].trim() + "," + strIRList;
				ResponseToAPP(strMsgHeader, strUserName, ServerRetCodeMgr.SUCCESS_CODE, s_strIRList);
				return ServerRetCodeMgr.SUCCESS_CODE;
			} else if (i_irdata_type == IR_DATA_AIRCON_SAVE_NAME) {	// 保存电视名字
				String strAirConName 	= strRet[5].trim();

				//开启事务机制
				dbMgr.BeginTansacion();

				//写数据库
				boolean bRet = dbMgr.UpdateModuleInfo_TV(strModuleId, strAirConName);
				if(!bRet)
				{
					dbMgr.Rollback();

					//结束事务机制
					dbMgr.EndTansacion();

					ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION);
					return ServerRetCodeMgr.ERROR_CODE_FAILED_DB_OPERATION;
				}

				//给APP回复成功
				ResponseToAPP(strMsgHeader, strUserName, strModuleId, ServerRetCodeMgr.SUCCESS_CODE);

				//提交
				dbMgr.Commit();
				//结束事务机制
				dbMgr.EndTansacion();
				return ServerRetCodeMgr.SUCCESS_CODE;

			} else if (i_irdata_type == IR_DATA_AIRCON_LOAD_NAME) {	// 获取电视名字
				String strAirConName = dbMgr.getTVIRName(strModuleId);
				String reponse 	= strRet[4].trim() + "," + strAirConName;
				ResponseToAPP(strMsgHeader, strUserName, ServerRetCodeMgr.SUCCESS_CODE, reponse);
				return ServerRetCodeMgr.SUCCESS_CODE;
			} else {
				// do nothing.
				return ServerRetCodeMgr.SUCCESS_CODE;
			}
		} catch (Exception e) {
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
