package com.thingzdo.smartplug.udpserver.commdef;

import java.util.HashMap;
import java.util.Map;

public class ServerRetCodeMgr {
	/******************************************SUCCESS CODE*******************************************************************/
	public final static int SUCCESS_CODE													= 0x00000000;
	public final static int ERROR_COMMON													= 0x00000001;
	public final static int ERROR_INVALID_COMMAND											= 0x00000002;
	public final static int ERROR_FAILD_FIND_CMD_FUNC										= 0x00000003;
	public final static int APP_QUIT 			= 2;
	public final static int MODULE_QUIT 	= 3;

	/******************************************SERVER ERROR CODE*******************************************************************/
	public final static int SERVER_ERROR_CODE_BASE 							= 0x10000000;
	public final static int ERROR_CODE_FAILED_SEND_EMAIL					= SERVER_ERROR_CODE_BASE + 1;		//0x1000 0001
	public final static int ERROR_CODE_FAILED_DB_OPERATION			= SERVER_ERROR_CODE_BASE + 2;		//0x1000 0002
	public final static int ERROR_CODE_RECEIVED_INVALID_STRING		= SERVER_ERROR_CODE_BASE + 3;		//0x1000 0003
	public final static int ERROR_CODE_FAILED_UPDATE_PWD				= SERVER_ERROR_CODE_BASE + 4;
	
	/******************************************APP ERROR CODE*******************************************************************/
	public final static int APP_ERROR_CODE_BASE 									= 0x20000000;
	public final static int ERROR_CODE_APP_NOT_LOGIN						= APP_ERROR_CODE_BASE + 1;			//0x2000 0001
	public final static int ERROR_CODE_MODULE_ID_UNREGISTERED	= APP_ERROR_CODE_BASE + 2;			//0X2000 0002
	public final static int ERROR_CODE_USER_NAME_REGISTERED		= APP_ERROR_CODE_BASE + 3;			//0X2000 0003
	public final static int ERROR_CODE_USER_NAME_UNREGISTERED	= APP_ERROR_CODE_BASE + 4;			//0X2000 0004
	public final static int ERROR_CODE_EMAIL_REGISTERED					= APP_ERROR_CODE_BASE + 5;			//0X2000 0005
	public final static int ERROR_CODE_PASSWORD_ERROR					= APP_ERROR_CODE_BASE + 6;			//0X2000 0006
	public final static int ERROR_CODE_MODULE_ID_REGISTERED			= APP_ERROR_CODE_BASE + 7;			//0X2000 0007
	public final static int ERROR_CODE_APP_HAS_LOGINED					= APP_ERROR_CODE_BASE + 8;			//0x2000 0008
	public final static int ERROR_CODE_USER_NOT_OWN_MODULE		= APP_ERROR_CODE_BASE + 9;			//0X2000 0009
	public final static int ERROR_CODE_USER_HAS_NO_AUTHORITY		= APP_ERROR_CODE_BASE + 10;			//0X2000 0010
	public final static int ERROR_CODE_COOKIE_INCORRECT					= APP_ERROR_CODE_BASE + 11;			//0X2000 0011
	public final static int ERROR_CODE_INVALID_USER_NAME				= APP_ERROR_CODE_BASE + 12;			//0X2000 0012
	public final static int ERROR_CODE_FAILED_UPDATE_COOKIE			= APP_ERROR_CODE_BASE + 13;			//0X2000 0013
	
	
	/****************************************MODULE ERROR CODE*******************************************************************/
	public final static int MODULE_ERROR_CODE_BASE					= 0x30000000;
	public final static int ERROR_CODE_MODULE_IS_OFFLINE			= MODULE_ERROR_CODE_BASE + 1;	//0x30000001
	public final static int ERROR_CODE_MODULE_NOT_ANSWER			= MODULE_ERROR_CODE_BASE + 2;	//0x30000002
	public final static int ERROR_CODE_MODULE_RET_ERROR				= MODULE_ERROR_CODE_BASE + 3;	//0X30000004
	public final static int ERROR_CODE_MODULE_HAS_LOGIN				= MODULE_ERROR_CODE_BASE + 4;	//0X30000004
	
	/***************************************FUNCTION ERROR CODE*******************************************************************/
	public final static int FUNC_ERROR_CODE_BASE								= 0x40000000;
	public final static int ERROR_CODE_FAILED_ADD_TIMER					= FUNC_ERROR_CODE_BASE + 1;		//0X4000 0001    //不方便细化的错误，统一用功能错误码代替
	public final static int ERROR_CODE_FAILED_DEL_TIMER					= FUNC_ERROR_CODE_BASE + 2;		//0x4000 0002
	public final static int ERROR_CODE_FAILED_MOD_TIMER					= FUNC_ERROR_CODE_BASE + 3;		//0x4000 0003
	public final static int ERROR_CODE_FAILED_ENABLE_TIMER				= FUNC_ERROR_CODE_BASE + 4;		//0x4000 0004
	public final static int ERROR_CODE_UNDOWN_ERROR						= FUNC_ERROR_CODE_BASE + 5;		//0x4000 0005
	
	private final static Map<Integer ,String> RetCodeMap = new HashMap()
	{
		{
			/*************************************SUCCESS CODE***********************************************/
			put(SUCCESS_CODE																,"succeed to execute.");
			
			/*************************************APP ERROR***********************************************/
			put(ERROR_CODE_APP_NOT_LOGIN										,"user not login.");
			put(ERROR_CODE_USER_NAME_REGISTERED						,"user name already registered.");
			put(ERROR_CODE_USER_NAME_UNREGISTERED					,"user name not registered.");
			put(ERROR_CODE_EMAIL_REGISTERED								,"email already registered.");
			put(ERROR_CODE_PASSWORD_ERROR								,"password error.");
			put(ERROR_CODE_MODULE_ID_REGISTERED								,"module is already in mysql.");
			put(ERROR_CODE_FAILED_SEND_EMAIL								,"failed to send email.");	
			put(ERROR_CODE_FAILED_DB_OPERATION							,"faild to operate db.");
			put(ERROR_CODE_USER_NOT_OWN_MODULE                    ,"user not own this modue.");
			put(ERROR_CODE_USER_HAS_NO_AUTHORITY                   ,"user has not authority to do.");
			put(ERROR_CODE_MODULE_ID_UNREGISTERED                 ,"module id is not registered.");
			put(ERROR_CODE_APP_HAS_LOGINED                                ,"user has logined.");
			put(ERROR_CODE_COOKIE_INCORRECT								,"invalid cookie.");
			put(ERROR_CODE_INVALID_USER_NAME 							,"invalid username.");
			
			/************************************MODULE ERROR********************************************************/
			put(ERROR_CODE_MODULE_IS_OFFLINE                             ,"module is offline.");
			put(ERROR_CODE_MODULE_NOT_ANSWER                       ,"module did not answer.");
			put(ERROR_CODE_MODULE_RET_ERROR								,"module return error code.");
			put(ERROR_CODE_FAILED_UPDATE_COOKIE						,"failed to update module cookie.")	;
			put(ERROR_CODE_MODULE_HAS_LOGIN                         ,"module has login.");
			
			/*************************************COMMON ERROR******************************************************/
			put(ERROR_CODE_RECEIVED_INVALID_STRING					,"received inavlid string.");
			
			/*************************************FUNCTION ERROR******************************************************/
			put(ERROR_CODE_FAILED_ADD_TIMER                               ,"failed to add timer.");
			put(ERROR_CODE_FAILED_MOD_TIMER                              ,"failed to mod timer.");
			put(ERROR_CODE_FAILED_DEL_TIMER                                ,"failed to del timer.");
			put(ERROR_CODE_FAILED_ENABLE_TIMER                         ,"failed to enbale or disable timer.");
			put(ERROR_CODE_FAILED_UPDATE_PWD                          ,"failed to update pwd.");	
			
		}
	};
	public static String getRetCodeDescription(int ret_code)
	{
		return RetCodeMap.get(ret_code);
	}
}
