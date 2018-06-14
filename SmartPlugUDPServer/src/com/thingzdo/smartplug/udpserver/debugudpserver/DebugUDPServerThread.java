package com.thingzdo.smartplug.udpserver.debugudpserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.platform.commdefine.ServerPortDefine;

public class DebugUDPServerThread extends Thread{
	private static int DEBUG_UDP_SERVER_PORT = ServerPortDefine.SMART_PLUG_DEBUG_UDP_SERVER_PORT + 10;
	
	private ExecutorService executor = Executors.newCachedThreadPool();
	public static DatagramSocket dataSocket = null;
	
	public DebugUDPServerThread()
	{
		this.start();
	}
	public void run()
	{
		/* 服务器永远也不会退出 */
		try {
			/*STEP1 查询模块IP地址*/
			dataSocket = new DatagramSocket(DEBUG_UDP_SERVER_PORT);
			while(true)
			{
				/*STEP2 监听*/
				while(true)
				{
					byte[] receiveByte = new byte[1024];
					DatagramPacket dataPacket = new DatagramPacket(receiveByte, receiveByte.length);
					dataSocket.receive(dataPacket);
					executor.execute(new DebugUDPWorkThread(dataPacket));
				}
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