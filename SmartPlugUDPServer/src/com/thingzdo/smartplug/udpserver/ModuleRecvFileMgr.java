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

public class ModuleRecvFileMgr {
	public static int 	RECV_OK 				= 0;
	public static int 	RECV_END 				= 1;
	public static int 	RECV_CONTINUE 			= 2;
	public static int 	RECV_ERROR 				= 3;
	
	private String m_strDevId;
	private int m_iFileNO = 0;
	Timer m_recvfile_timer = null;
	
	private String m_strUserName;
	private int m_iBlockIdx			= -1;	//待更新的文件块的索引
	private int m_iBlockTotal		= 0;	//全部的文件块的索引
	private int m_iBlockBytes 		= 0;	//模块更新过程中传送的总字节数			
	private int m_iTotalBytes 		= 0;	//模块更新过程中传送的总字节数
	private int m_iBlockCrcCode 	= 0;	//每次更新的块大小，取决于模块硬件的缓存
	private int m_iTotalCrcCode 	= 0;	//模块更新过程中传数据的字节数的CRC，目前使用传送的字节数取和简单处理
	byte[] m_aucUserBin;					//文件写入缓存区

	private FileOutputStream writer = null;

	public ModuleRecvFileMgr(String strDevId, int fileno) 
	{
		m_strDevId = strDevId;
		m_iFileNO = fileno;
		m_iBlockIdx			= -1;
		m_iBlockTotal		= 0;
		m_iBlockBytes 		= 0;
		m_iTotalBytes 		= 0;
		m_iBlockCrcCode 	= 0;
		m_iTotalCrcCode 	= 0;
		
		// 保存的文件绝对目录名字： /root/thingzdo/smartplugudp/snap/xxxxxx/snap_00000001.jpg
		String filename = String.format("snap_%08d.jpg", m_iFileNO);
		JudeFileExist(m_strDevId);
		String str_outputfilename = ServerParamConfiger.getRecvFilePath() + "/" + m_strDevId + "/" + filename;
		
		try {
			writer = new FileOutputStream(str_outputfilename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	private void JudeFileExist(String strDevId) {
		File dir_base = new File(ServerParamConfiger.getRecvFilePath());
		if (!dir_base.exists()) {
			dir_base.mkdir();
		}

		dir_base = new File(ServerParamConfiger.getRecvFilePath() + "/" + strDevId);
		if (!dir_base.exists()) {
			dir_base.mkdir();
		}
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
	
	public void StartRecvFileStartTimer()
	{
		m_recvfile_timer = new Timer();
		m_recvfile_timer.schedule(new RecvFileBeatTask(this), 10000, 10000);
		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Start RecvFile Timer:%s,%d [%s]", m_strDevId, m_iFileNO, m_recvfile_timer.toString()));
	}
	
	public void StopRecvFileStartTimer()
	{
		LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Stop RecvFile Timer:%s,%d [%s]", m_strDevId, m_iFileNO, m_recvfile_timer.toString()));
		m_recvfile_timer.cancel();
	}
	
	/**********************************************************************************************************
	 * @name UpgradeSend 服务器向模块发送版本数据
	 * @param 	cookiID,UPGRADESEND,username,10918174,0(block no),256(block size),24902(block sum),block_data#
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
	public int WriteToFile(byte[] in_info, int in_length, int blockCrc, int blockinx)
	{
			int iValue = 0;
			int iCrcCode = 0;
			int i = 0;
			try {
				m_iBlockIdx++;//初始为-1，这样m_iBlockIdx则代表最近一次发送给模块的块ID; 
				if (m_iBlockIdx != blockinx)
				{
					LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Recv Block Index Error: [srv:%d, dev:%d]", m_iBlockIdx, blockinx));
				}
			
				/* 写入文件 */
				m_aucUserBin = new byte[in_length];				
				for (i = 0; i < in_length; i++)
				{
					iValue = in_info[i];
					m_aucUserBin[i] = in_info[i];
					m_iTotalBytes++;
					iCrcCode += iValue;
				}

				writer.write(m_aucUserBin);
				writer.flush();
				
				m_iBlockBytes = in_length;
				m_iBlockTotal++;
				m_iBlockCrcCode = iCrcCode;
				m_iTotalCrcCode += iCrcCode;
				
				LogWriter.WriteDebugLog(LogWriter.SELF,
						String.format("Write Block: %d %d %d %d", m_iBlockIdx, in_length, m_iTotalBytes, m_iTotalCrcCode));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG, e);
				return RECV_ERROR;
			}
			
			return RECV_OK;
	}

	public int getTotalCrcCode() {
		return m_iTotalCrcCode;
	}

	public void setTotalCrcCode(int iCrcCode) {
		this.m_iTotalCrcCode = iCrcCode;
	}

	public int getTotalBytes() {
		return m_iTotalBytes;
	}

	public void setTotalBytes(int iTotalBytes) {
		this.m_iTotalBytes = iTotalBytes;
	}

	public int getBlockIdx() {
		return m_iBlockIdx;
	}

	public void setBlockIdx(int iBlockIdx) {
		this.m_iBlockIdx = iBlockIdx;
	}

	public int getBlockTotal() {
		return m_iBlockTotal;
	}

	public void setBlockTotal(int iBlockTotal) {
		this.m_iBlockTotal = iBlockTotal;
	}	
	
	public int getBlockBytes() {
		return m_iBlockBytes;
	}

	public void setBlockBytes(int iBlockBytes) {
		this.m_iBlockBytes = iBlockBytes;
	}

	public String getModuleID() {
		return m_strDevId;
	}
	
	public int getFileNO() {
		return m_iFileNO;
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
