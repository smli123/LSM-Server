package com.thingzdo.smartplug.udpserver.Function;

import java.io.UnsupportedEncodingException;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ModuleRecvFileMgr;
import com.thingzdo.smartplug.udpserver.ModuleUpgradeOnLineMgr;
import com.thingzdo.smartplug.udpserver.ServerWorkThread;
import com.thingzdo.smartplug.udpserver.commdef.ICallFunction;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerRetCodeMgr;

public class ModuleRecvFileSendMsgHandle implements ICallFunction{
	
	@Override
	public int call(Runnable thread_base, String strMsg) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**********************************************************************************************************
	 * @name ModuleRecvFileSendMsgHandle 模块通知服务器其已收到END消息
	 * @param 模块接收：20160802163030,JPG_SEND,test,module_id,buf_no,readsizes,errcheck,data#
	 * @param 模块回复：20160802163030,JPG_SEND,test,module_id,0,index,buf_size,total_size,total_err#
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	public int resp(Runnable thread_base, String strMsg) 
	{
		ServerWorkThread thread = (ServerWorkThread)thread_base;
		String strRet[] 	= strMsg.split(ServerCommDefine.CMD_SPLIT_STRING);
		String strCookie	= strRet[0].trim();
		String strMsgHeader	= strRet[1].trim();
		String strUserName	= strRet[2].trim();
		String strDevId		= strRet[3].trim();
		int blockIdx		= Integer.valueOf(strRet[4].trim());
		int blockSize		= Integer.valueOf(strRet[5].trim());
		int blockCrc		= Integer.valueOf(strRet[6].trim());
		byte[] data			= new byte[blockSize];
		
		int dot_num = 0;
		boolean flag_begin = false;
		byte[] data_all = null;
		try {
			//lishimin modify
			data_all = strMsg.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		String str_data_all = "Block_all:";
//		for (int k = 0; k < data_all.length; k++) {
//			str_data_all += String.format("%x ", data_all[k]);
//		}
//		LogWriter.WriteErrorLog(LogWriter.SELF, str_data_all);
		
		int j = 0;
		for (int i = 0; i < data_all.length; i++) {
			if (flag_begin == false) {
				if (data_all[i] == ',')
					dot_num++;
				if (dot_num < 7) {
					continue;
				} else {
					flag_begin = true;
				}
			} else {
				data[j] = data_all[i];
				j++;
				if (j >= blockSize) {
					break;
				}
			}
		}
		
//		str_data_all = "Data:";
//		for (int k = 0; k < data.length; k++) {
//			str_data_all += String.format("%x ", data[k]);
//		}
//		LogWriter.WriteErrorLog(LogWriter.SELF, str_data_all);
		
		/* 更新COOKIE */
		ServerWorkThread.RefreshAppCookie(strUserName, strCookie);

		ServerWorkThread.RefreshModuleIP(strDevId, thread.getSrcIP(), thread.getSrcPort());

		ModuleRecvFileMgr mgr = thread.GetModuleRecvFileMgr(strDevId);
		if (mgr == null) {
			return ServerRetCodeMgr.ERROR_COMMON;
			
		} else {
			int iRetCode = mgr.WriteToFile(data, blockSize, blockCrc, blockIdx);
			if (iRetCode == ModuleRecvFileMgr.RECV_ERROR) {
				LogWriter.WriteErrorLog(LogWriter.SELF, "recv file from module error."); 
				return ServerRetCodeMgr.ERROR_COMMON;
			}
		}
		
		/* 通知模块，服务器收到END响应 */
		//20160802163030,JPG_SEND,test,module_id,0,index,buf_size,total_size,total_err#
		String strRsp = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s#",
				ServerWorkThread.getModuleCookie(strDevId),
				ServerCommDefine.MODULE_RECV_FILE_SEND_MSG_HEADER,
				strUserName,
				strDevId,"0",blockIdx,blockSize,mgr.getTotalBytes(),mgr.getTotalCrcCode());
		
		ResponseToModule(strDevId, strRsp);
		return ServerRetCodeMgr.SUCCESS_CODE;
	}

}
