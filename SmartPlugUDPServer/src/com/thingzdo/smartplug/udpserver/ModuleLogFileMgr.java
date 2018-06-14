package com.thingzdo.smartplug.udpserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.Function.ModuleHeartBeatTask;
import com.thingzdo.smartplug.udpserver.Function.ModuleUpgradeSendTask;
import com.thingzdo.smartplug.udpserver.Function.ModuleUpgradeStartTask;
import com.thingzdo.smartplug.udpserver.Function.RecvFileBeatTask;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerParamConfiger;

public class ModuleLogFileMgr {
	
	private String m_strDevId;
	
	private String m_strUserName;
	byte[] m_aucUserBin;					//文件写入缓存区

	private FileOutputStream writer = null;

	public ModuleLogFileMgr(String strDevId) 
	{
		m_strDevId = strDevId;
		
		// 保存的文件绝对目录名字： /root/thingzdo/smartplugudp/modulelog/moduleid.log
		String filename = String.format("%s.log", strDevId);
		JudeFileExist(m_strDevId);
		
//		String str_outputfilename = ServerParamConfiger.getLogFilePath() + "/" + m_strDevId + "/" + filename;
		String str_outputfilename = ServerParamConfiger.getLogFilePath() + "/" + filename;
		
		try {
			writer = new FileOutputStream(str_outputfilename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	private void JudeFileExist(String strDevId) {
		File dir_base = new File(ServerParamConfiger.getLogFilePath());
		if (!dir_base.exists()) {
			dir_base.mkdir();
		}

//		dir_base = new File(ServerParamConfiger.getLogFilePath() + "/" + strDevId);
//		if (!dir_base.exists()) {
//			dir_base.mkdir();
//		}
	}
	
	public void Destroy()
	{
		try
		{
			if (writer != null)
			{
				writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**********************************************************************************************************
	 * @name MODULELOG 服务器向模块发送版本数据
	 * @param 	cookiID,MODULELOG,username,10918174,log_txt#
	 * 	dd_no=块序号 
		dd_size=块的字节数  iBlockSize
		dd_data=块的内容 
		iUserBinNo : 用户BIN文件的索引号
		iBlockSize:每次发送的块大小
		iBlockIdx:发送的块索引,从0开始
	 * @return  boolean 是否成功
	 * @author zxluan
	 * @throws IOException 
	 * @date 2015/04/10
	 * **********************************************************************************************************/
	public void WriteToFile(String infos)
	{
		try {
			infos += "\r\n";
			//LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Write Module(%s) Log: %s", m_strDevId, infos));
		
			/* 写入文件 */
			m_aucUserBin = infos.getBytes();
			writer.write(m_aucUserBin);
			writer.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG, e);
		}
	}

	public static void main(String[] args)  {
		/*
		ModuleUpgradeOnLineMgr mgr = new ModuleUpgradeOnLineMgr(256, 0 ,"0", "0");
		
		try {
			byte[] byResp = new byte[1000];
			mgr.GetSoftwareBinData("0" , byResp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
}
