package com.thingzdo.smartplug.udpserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.commdefine.ServerPortDefine;
import com.thingzdo.smartplug.udpserver.commdef.ServerParamConfiger;

public class ServerMainThread extends Thread {
	public static DatagramSocket dataSocket = null;
	/*创建线程池*/
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	public ServerMainThread()
	{
		ServerWorkThread.Init();
		this.start();
	}
	@Override
	public void run()
	{
		/* 启动时间校对定时器 */
		new Timer().schedule(new ModuleAdjustModuleTimeTask(), 
				ServerParamConfiger.getiTimeoutAdjustTime(), 
				ServerParamConfiger.getiTimeoutAdjustTime());
		
		/* 服务器永远也不会退出 */
		while(true)
		{
			try {
				/*STEP1 查询模块IP地址*/
				dataSocket = new DatagramSocket(ServerPortDefine.SMART_PLUG_UDP_SERVER_PORT);
				/*STEP2 监听*/
				while(true)
				{
					byte[] receiveByte = new byte[1024];
					DatagramPacket dataPacket = new DatagramPacket(receiveByte, receiveByte.length);
					dataSocket.receive(dataPacket);
					executor.execute(new ServerWorkThread(dataPacket));
				}
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LogWriter.WriteExceptionLog(LogWriter.SELF,e);
			}
			
			if (null != dataSocket)
			{
				dataSocket.close();
			}
		}
	}
}
