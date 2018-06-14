package com.thingzdo.smartplug.udpserver.main;

import com.thingzdo.platform.LogTool.LogWriter;
import com.thingzdo.smartplug.udpserver.ServerMainThread;
import com.thingzdo.smartplug.udpserver.commdef.ServerParamConfiger;
import com.thingzdo.smartplug.udpserver.db.ServerDBMgr;
import com.thingzdo.smartplug.udpserver.debugserver.DebugServerThread;
import com.thingzdo.smartplug.udpserver.debugudpserver.DebugUDPServerThread;

public class Main {
	public static void main(String[] args)  {
		// TODO Auto-generated method stub
		try {	
			//STEP1 初始化日志服务 
			LogWriter.Init(ServerParamConfiger.PRODUCT_NAME);
			LogWriter.WriteTraceLog(LogWriter.SELF,"==========SmartPlugServer Begin============");
			
			//STEP2 读取配置参数
			ServerParamConfiger.Do();
			
			//STEP3 初始化数据库
			ServerDBMgr.Init();

			// 更新 MODULE_DATA的LOGOUT_TIME时间
			ServerDBMgr_InitModuleData();
			
			//STEP4 创建APP服务器线程
			Thread app_server = new ServerMainThread();
			LogWriter.WriteTraceLog(LogWriter.SELF,"SmartPlugServer App Server started successfully.");
			
			//STEP4 创建远程调试服务器
			Thread debug_server = new DebugServerThread();
			LogWriter.WriteTraceLog(LogWriter.SELF,"Remote Debug Server started successfully.");
			
			//STEP4 创建远程调试服务器(UDP)
			Thread debug_udp_server = new DebugUDPServerThread();
			LogWriter.WriteTraceLog(LogWriter.SELF,"Remote Debug UDP Server started successfully.");
			
			//STEP5 等待各线程结束
			app_server.join();
			debug_server.join();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogWriter.WriteExceptionLog(LogWriter.SELF,e);
		} 
		LogWriter.WriteTraceLog(LogWriter.SELF,"==========SmartPlugServer Terminate============");
	}
	
	private static void ServerDBMgr_InitModuleData() {
		if (ServerParamConfiger.getRecordModuleData() == true) {
			ServerDBMgr dbMgr = new ServerDBMgr();
			dbMgr.BeginTansacion();
			if (dbMgr.InitModuleData() == false) {
				dbMgr.Rollback();
			} else {
				dbMgr.Commit();	
			}
			dbMgr.EndTansacion();	
		}
	}
}
