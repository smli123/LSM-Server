package com.thingzdo.smartplug.udpserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.Function.ModuleHeartBeatTask;
import com.thingzdo.smartplug.udpserver.Function.ModuleUpgradeSendTask;
import com.thingzdo.smartplug.udpserver.Function.ModuleUpgradeStartTask;
import com.thingzdo.smartplug.udpserver.commdef.ServerCommDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerParamConfiger;

public class ModuleUpgradeOnLineMgr {
	public static int 	SEND_OK 				= 0;
	public static int 	SEND_END 				= 1;
	public static int 	SEND_CONTINUE 			= 2;
	public static int 	SEND_ERROR 				= 3;
	
	private String m_strDevId;
	private String m_strUserName;
	private String m_DeviceType;
	private int m_iTotalCrcCode 		= 0;	//模块更新过程中传数据的字节数的CRC，目前使用传送的字节数取和简单处理
	private int m_iTotalBytes 	= 0;	//模块更新过程中传送的总字节数
	private int m_iFileBinNo   = 0;	//待更新的模块文件，FILE BIN索引ID
	private int m_iAuxFileBinNo   = 512;	//待更新的辅助模块文件，FILE BIN索引ID
	private int m_iBlockIdx		= -1;	//待更新的文件块的索引
	private int m_iBlockTotal		= 0;	//全部的文件块的索引
	private int m_iBlockSize 	= 0;	//每次更新的块大小，取决于模块硬件的缓存
	byte[] m_aucUserBin;		//文件读取缓存区
	FileInputStream reader;
	private Timer m_timer_Start = null;
	private Timer m_timer_Send = null;
	private boolean m_timer_Start_flag = false;
	private boolean m_timer_Send_flag = false;
	private String m_upgrade_start_cmd = "";
	private String m_upgrade_send_cmd = "";
	private byte[] m_upgrade_send_cmd_send;
	
	public ModuleUpgradeOnLineMgr(int iBlockSize, int iFileBinNo, String strDevId,String strUserName, int iaux_FileBinNo, String strDeviceType) 
	{
		setTotalBytes(0);
		setTotalCrcCode(0);
		setBlockIdx(-1);
		setBlockSize(iBlockSize);
		setFileBinNo(iFileBinNo);
		setAuxFileBinNo(iaux_FileBinNo);
		setDeviceType(strDeviceType);
		m_aucUserBin = new byte[iBlockSize + 1];
		m_strDevId = strDevId;	
		m_strUserName = strUserName;
		m_upgrade_send_cmd_send = new byte[iBlockSize + 100];
		
		try {
			//reader = new FileInputStream("G:\\thingzdo\\CODE\\Server\\SmartPlugServer\\user1.512.new.bin");
			
			int i_real_FileBinNo = iFileBinNo;
			if (m_iAuxFileBinNo == 512) {
				i_real_FileBinNo = m_iFileBinNo;
			} else if (m_iAuxFileBinNo == 1024) {
				i_real_FileBinNo = m_iFileBinNo + 2;
			} else {
				// Error... 
				LogWriter.WriteDebugLog(LogWriter.SELF,
						String.format("Error. it can not run here. [iFileBinNo:%d, iaux_FileBinNo:%d]", m_iFileBinNo, m_iAuxFileBinNo));
				throw new FileNotFoundException();
			}

			String[] binFiles = {"user1.512.new.bin", "user2.512.new.bin", "user1.1024.new.2.bin", "user2.1024.new.2.bin"};
			String binFileName = binFiles[i_real_FileBinNo];
			String filePath = ServerParamConfiger.getUpgradeBinPath() + "/" + strDeviceType + "/" + binFileName;
			
			LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Upgrade FileName[%s]:%s", m_strDevId, filePath));
			
			reader = new FileInputStream(filePath);
//			reader = new FileInputStream(ServerParamConfiger.getUserBinPath(i_real_FileBinNo));
			
			// 获取文件的字节数
			int content = 0 ; //存放读取到的数据
			int file_length = 0;
		    try {
				while((content = reader.read())!=-1){
				    file_length++;
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			m_iBlockTotal = (int) Math.ceil((double)file_length / iBlockSize);			
			
			reader = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Upgrade Send Command to Module
	 */
	public byte[] GetUpgradeStartCommand_ToModule() {
		return m_upgrade_send_cmd_send;
	}
	
	public void SetUpgradeStartCommand_ToModule(byte[] upgrade_send_cmd_send) {
		for (int i = 0; i < upgrade_send_cmd_send.length; i++) {
			m_upgrade_send_cmd_send[i] = upgrade_send_cmd_send[i];
		}
	}
	
	/*
	 * Upgrade Start Command for resend
	 */
	public String GetUpgradeStartCommand() {
		return m_upgrade_start_cmd;
	}
	
	public String SetUpgradeStartCommand(String upgrade_start_cmd) {
		return m_upgrade_start_cmd = upgrade_start_cmd;
	}

	/*
	 * Upgrade Send Command for resend
	 */
	public String GetUpgradeSendCommand() {
		return m_upgrade_send_cmd;
	}
	
	public String SetUpgradeSendCommand(String upgrade_send_cmd) {
		return m_upgrade_send_cmd = upgrade_send_cmd;
	}
	
	/*
	 * Upgrade Start Timer is Schedule
	 */
	public boolean GetUpgradeStartTimerFlag() {
		return m_timer_Start_flag;
	}
	/*
	 * Upgrade Send Timer is Schedule
	 */
	public boolean GetUpgradeSendTimerFlag() {
		return m_timer_Send_flag;
	}
	
	public void StartUpgradeStartTimer()
	{
		m_timer_Start = new Timer();
		m_timer_Start.schedule(new ModuleUpgradeStartTask(this), 
				ServerParamConfiger.getiTimeOutUpgrade(),  
				ServerParamConfiger.getiTimeOutUpgrade());
		m_timer_Start_flag = true;
		//LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Start Upgrade_Start Timer:%s [%s]", m_strDevId, m_timer_Start.toString()));
	}
	
	public void StopUpgradeStartTimer()
	{
		if (m_timer_Start_flag == true) {
			//LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Stop Upgrade_Start Timer:%s [%s]", m_strDevId, m_timer_Start.toString()));
			m_timer_Start.cancel();
			m_timer_Start_flag = false;	
		}
	}

	public void StartUpgradeSendTimer()
	{
		m_timer_Send = new Timer();
		m_timer_Send.schedule(new ModuleUpgradeSendTask(this), 
				ServerParamConfiger.getiTimeOutUpgrade(),  
				ServerParamConfiger.getiTimeOutUpgrade());
		m_timer_Send_flag = true;
		//LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Start Upgrade_Send Timer:%s [%s]. Upgrade is begin", m_strDevId, m_timer_Send.toString()));
	}
	
	public void StopUpgradeSendTimer()
	{
		if (m_timer_Send_flag == true) {
			//LogWriter.WriteDebugLog(LogWriter.SELF, String.format("Stop Upgrade_Send Timer:%s [%s]. Upgrade is over", m_strDevId, m_timer_Send.toString()));
			m_timer_Send.cancel();
			m_timer_Send_flag = false;
		}
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
	public int GetSoftwareBinData(String strCookie, byte[] out_info) throws IOException 
	{
			int iValue = 0;
			int iCrcCode = 0;
			int iBytesReaded = 0;
			int i = 0;
			try {
				m_iBlockIdx++;//初始为-1，这样m_iBlockIdx则代表最近一次发送给模块的块ID; 
			
				/* 读取BIN文件 */
				for (i = 0; i < m_iBlockSize; i++)
				{
					iValue = reader.read();
					if(iValue == -1)
					{
						break;
					}
					m_aucUserBin[i] = (byte) iValue;
					m_iTotalBytes++;
					iBytesReaded++;
					iCrcCode += iValue;
				}
				
				/* 文件读取结束，给模块回END */
				if (0 == i)
				{
					return SEND_END;
				}
				
				/* 剩余部分初始化为0 */
				for (; i < m_iBlockSize + 1; i++)
				{
					m_aucUserBin[i] = 0;
				}
				
				/* 组装发送数据 */
				String strHeader = String.format("%s,%s,%s,%s,%d,%d,%d,", strCookie,ServerCommDefine.MODULE_UPGRADE_SEND_MSG_HEADER
						,m_strUserName, m_strDevId, m_iBlockIdx, iBytesReaded,iCrcCode); 
				
				byte[] tmp02 = strHeader.getBytes();
				
				int j = 0;
				for (j = 0; j < tmp02.length; j++)
				{
					out_info[j] = tmp02[j];
				}
				
				for (i= 0; i < iBytesReaded; i++)
				{
					out_info[j + i] = m_aucUserBin[i];
				}
				
				m_iTotalCrcCode += iCrcCode;
				
				LogWriter.WriteDebugLog(LogWriter.SELF,
						String.format("Block: %d %d %d %d", m_iBlockIdx, iBytesReaded, m_iTotalBytes, m_iTotalCrcCode));
				
				// DEBUG： 只是为了打印调试信息
//				String print_str = "";
//				for (i = 0; i < out_info.length; i++) {
//					int v = out_info[i] & 0xFF;
//			        String hv = Integer.toHexString(v);
//			        
//					print_str = print_str + hv + ",";
//				}
//				LogWriter.WriteTraceLog(LogWriter.SELF, String.format("Block Hex:%s", print_str));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SRV_SELF_LOG, e);
				return SEND_ERROR;
			}
			
			return SEND_OK;
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

	public int getFileBinNo() {
		return m_iFileBinNo;
	}

	public void setFileBinNo(int iFileBinNo) {
			this.m_iFileBinNo = iFileBinNo;
	}

	public int getAuxFileBinNo() {
		return m_iAuxFileBinNo;
	}

	public void setAuxFileBinNo(int iaux_FileBinNo) {
		this.m_iAuxFileBinNo = iaux_FileBinNo;
	}
	public int getBlockIdx() {
		return m_iBlockIdx;
	}

	public void setDeviceType(String strdevType) {
		this.m_DeviceType = strdevType;
	}
	public String getDeviceType() {
		return m_DeviceType;
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
	

	public int getBlockSize() {
		return m_iBlockSize;
	}

	public void setBlockSize(int iBlockSize) {
		this.m_iBlockSize = iBlockSize;
	}
	
	public String getModuleID() {
		return m_strDevId;
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
