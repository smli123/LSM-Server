package com.thingzdo.smartplug.udpserver.Function;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.ModuleUpgradeOnLineMgr;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.db.USER_MODULE;

public class UpgradeSendRespHandle implements ICallFunction{
	
	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
	/**********************************************************************************************************
	 * @name UpgradeSendRspHandle 模板响应软件发送请求
	 * @param cookiID,UPGRADESEND,usrname,moduleID,0,dd_no,dd_size,totalsize,totalsize_forerr#
		dd_no=上次服务器发送给模块的块序号
		dd_size=上次服务器发送给模块的块的字节数
		dd_data=上次服务器发送给模块的块的内容
		totalsize=服务器当前发送的累计的字节数
		totalsize_forerr=模块当前接收到字节的累加值
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg) 
	{
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		String strRet[] = strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strNewCookie	= strRet[0].trim();
		String strMsgHeader = strRet[1].trim();
		String strUserName	= strRet[2].trim();
		String strDevId		= strRet[3].trim();
		int iRetCode		= Integer.valueOf(strRet[4].trim());
		int iLastBlockIdx	= Integer.valueOf(strRet[5].trim()) ;
		int iLastBlockSizes	= Integer.valueOf(strRet[6].trim()) ;
		int iTotalSendSizes	= Integer.valueOf(strRet[7].trim()) ;
		int iTotalRecvBytes	= Integer.valueOf(strRet[8].trim()) ;
		
		/* 更新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strNewCookie);
		/* 刷新心跳状态 , 由于存在Resend机制，此处不能设置Alive为true*/
		//ServerWorkThread.RefreshModuleAliveFlag(strDevId, true);
		ServerWorkThread.RefreshModuleIP(strDevId, thread.getSrcIP(), thread.getSrcPort());
		
		if(iRetCode != 0)
		{
			LogWriter.WriteErrorLog(LogWriter.SELF, String.format("(%s) module return error. %d", strDevId, iRetCode));
			return ServerRetCodeMgr.ERROR_CODE_MODULE_RET_ERROR;
		}

		/* 更新  Upgrade send 定时器，保存上次的下发命令 */
		ModuleUpgradeOnLineMgr upgradeMgr = thread.GetModuleUpgradeMgr(strDevId);
		
		/* upgradeMgr 为空，说明 已经升级已经完成了； */ 
		if (upgradeMgr == null) {
			return ServerRetCodeMgr.SUCCESS_CODE;
			
		} else {
			if (upgradeMgr.GetUpgradeSendTimerFlag() == false) {
				LogWriter.WriteDebugLog(LogWriter.SELF, String.format("[%s] Timer is over, not resend", strDevId));
				return ServerRetCodeMgr.SUCCESS_CODE;
			}

			upgradeMgr.StopUpgradeSendTimer();
			upgradeMgr.StartUpgradeSendTimer();
		}
		
		int iLocalBlockIdx =  thread.GetModuleUpgradeMgr(strDevId).getBlockIdx();
		if(iLastBlockIdx != iLocalBlockIdx)
		{
			LogWriter.WriteErrorLog(LogWriter.RECV,
					String.format("local block index:%d, module block index:%d", iLocalBlockIdx, iLastBlockIdx));
			//return ServerRetCodeMgr.ERROR_COMMON;  // 不能返回；
		}
	
		ServerDBMgr dbMgr = new ServerDBMgr();
		try {
			int iBlockSize = thread.GetModuleUpgradeMgr(strDevId).getBlockSize();
			
			byte[] byResp = new byte[iBlockSize + 100];
			
			/* 读取并发送数据 */
			int iRet = thread.GetModuleUpgradeMgr(strDevId).GetSoftwareBinData(strNewCookie, byResp);	
			
			if(iRet == ModuleUpgradeOnLineMgr.SEND_ERROR)
			{
				/* 发送完成 */
				LogWriter.WriteErrorLog(LogWriter.SELF, "update error.stop.");
				
				// 给客户端上报进度
				USER_MODULE info = dbMgr.QueryUserModuleByDevId(strDevId);
				NotifyToAPP(info.getUserName(),strDevId, ServerCommDefine.NOTIFY_UPGRADEAP_STATUS, 
						iRet,  String.format("%d,%d", upgradeMgr.getBlockIdx(), upgradeMgr.getBlockTotal()));
				return ServerRetCodeMgr.ERROR_COMMON;
			}
			
			if(iRet == ModuleUpgradeOnLineMgr.SEND_END)
			{
				/* 发送完成 */
				LogWriter.WriteDebugLog(LogWriter.SELF,  "update send_end is complete."); 
				String strUpgradeEndCmd = GetUpgradeEndCmdString(thread.GetModuleUpgradeMgr(strDevId), strNewCookie, strUserName, strDevId);

				ResponseToModule(strDevId, strUpgradeEndCmd);
				
				strUpgradeEndCmd = strUpgradeEndCmd.replace(ServerCommDefine.MODULE_UPGRADE_END_MSG_HEADER, ServerCommDefine.MODULE_UPGRADE_REEND_MSG_HEADER);
				upgradeMgr.SetUpgradeSendCommand(strUpgradeEndCmd);
				
				return ServerRetCodeMgr.SUCCESS_CODE;
			}

			upgradeMgr.SetUpgradeStartCommand_ToModule(byResp);
			ResponseToModule(strDevId, byResp);
			
			// 转成 resend 命令进行定时处理；
			String strNewMsg = strMsg.replace(ServerCommDefine.MODULE_UPGRADE_SEND_MSG_HEADER, ServerCommDefine.MODULE_UPGRADE_RESEND_MSG_HEADER);
			upgradeMgr.SetUpgradeSendCommand(strNewMsg);
			
			// 给客户端上报进度
			USER_MODULE info = dbMgr.QueryUserModuleByDevId(strDevId);
			NotifyToAPP(info.getUserName(),strDevId, ServerCommDefine.NOTIFY_UPGRADEAP_STATUS, 
					ServerRetCodeMgr.SUCCESS_CODE,  String.format("%d,%d", upgradeMgr.getBlockIdx(), upgradeMgr.getBlockTotal()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF, e);
		} finally {
			dbMgr.Destroy();
		}

		return ServerRetCodeMgr.SUCCESS_CODE;
	}
	/**********************************************************************************************************
	 * @name GetUpgradeEndCmdString 服务器通知模块升级完成
	 * @param cookiID,UPGRADEEND,usrname,moduleID,dd_no,totalsize,totalsize_forerr#
	 * 	dd_no=块序号
		dd_size=块的字节数
		dd_data=块的内容
		totalsize=服务器当前发送的累计的字节数
		totalsize_forerr=模块当前接收到字节的累加值
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @throws FileNotFoundException 
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	private String GetUpgradeEndCmdString(ModuleUpgradeOnLineMgr upgradeMgr,String strCookie,String strUserName, String strModuleId)
	{
		/* 构造命令字符串 */
		String strRsp = String.format("%s,%s,%s,%s,%d,%d,%d,%s#", 
				strCookie,
				ServerCommDefine.MODULE_UPGRADE_END_MSG_HEADER,
				strUserName,
				strModuleId,
				upgradeMgr.getBlockIdx(),
				upgradeMgr.getTotalBytes(),
				upgradeMgr.getTotalCrcCode(),
				"V1.0");
		return strRsp;
	}
}
