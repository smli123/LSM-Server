package com.thingzdo.smartplug.udpserver;

import java.util.TimerTask;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.commdefine.ServerPortDefine;

public class ModuleAdjustModuleTimeTask extends TimerTask {
	/**
	 * 0,ADJTIMER,test,moduleID,TimeStamp#
	 * */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		for (Entry<String, ConnectInfo> entry : ServerWorkThread.getModuleIPMap().entrySet()) 
		{  
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
			String strTime = df.format(new Date());
			String strOutput = "0,ADJTIMER,user,moduleid," + strTime;
			try {
				DatagramPacket dp = new DatagramPacket(strOutput.getBytes(), strOutput.getBytes().length, 
						InetAddress.getByName(entry.getValue().getSrcIP()), entry.getValue().getSrcPort());  
				
				ServerMainThread.dataSocket.send(dp);
				LogWriter.WriteDebugLog(LogWriter.SRV_TO_MOD,String.format("(%s:%d)\t AdjustTime Response To Module(%s).",
						entry.getValue().getSrcIP(),entry.getValue().getSrcPort(), strOutput));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SELF, e);
			}
		} 
	}
}
