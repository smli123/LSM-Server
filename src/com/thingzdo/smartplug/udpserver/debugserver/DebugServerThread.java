package com.thingzdo.smartplug.udpserver.debugserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.thingzdo.platform.commdefine.ServerPortDefine;

public class DebugServerThread extends Thread{
	private ExecutorService executor = Executors.newCachedThreadPool();
	public DebugServerThread()
	{
		this.start();
	}
	public void run()
	{
		//创建SOCKET
		ServerSocket server;
		try {
			server = new ServerSocket(ServerPortDefine.SMART_PLUG_DEBUG_UDP_SERVER_PORT);
			while(true)
			{
				/*STEP2 监听*/
				while(true)
				{
					Socket debug_client = server.accept();
					executor.execute(new DebugWorkThread(debug_client));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}