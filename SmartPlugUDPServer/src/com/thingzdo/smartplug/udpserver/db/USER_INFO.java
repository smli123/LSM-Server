package com.thingzdo.smartplug.udpserver.db;
/**USER_INFO(String strUserName,String strPassWord,String strEmail)
 * mysql> describe user_info;
+-----------+-------------+------+-----+---------+-------+
| Field     | Type        | Null | Key | Default | Extra |
+-----------+-------------+------+-----+---------+-------+
| user_name | varchar(32) | NO   | PRI | NULL    |       |
| password  | varchar(32) | NO   |     | NULL    |       |
| email     | varchar(32) | NO   |     | NULL    |       |
+-----------+-------------+------+-----+---------+-------+
 * */
public class USER_INFO {
	public final static String TABLE_NAME		= "user_info";
	public final static String USER_NAME 			= "user_name";
	public final static String PASSWORD			= "password";
	public final static String EMAIL					= "email";
	public final static String COOKIE					= "cookie";
	
	private String  	m_strUserName;
	private String 	m_strPassWord;
	private String 	m_strEmail;
	private String 	m_strCookie;
	public USER_INFO(String strUserName,String strPassWord,String strEmail, String strCookie)
	{
		setUserName(strUserName);
		setPassWord(strPassWord);
		setEmail(strEmail);
		setCookie(strCookie);
	}
	public boolean equal(USER_INFO info)
	{
		if(null == info)
		{
			return false;
		}
		if(this.m_strEmail.equalsIgnoreCase(info.getEmail())&&
			this.m_strPassWord.equalsIgnoreCase(info.getPassWord())&&
			this.m_strUserName.equalsIgnoreCase(info.getUserName()))
		{
			return true;
		}
		return false;
	}
	public String getUserName() {
		return m_strUserName;
	}
	public void setUserName(String strUserName) {
		this.m_strUserName = strUserName;
	}
	public String getPassWord() {
		return m_strPassWord;
	}
	public void setPassWord(String strPassWord) {
		this.m_strPassWord = strPassWord;
	}
	public String getEmail() {
		return m_strEmail;
	}
	public void setEmail(String strEmail) {
		this.m_strEmail = strEmail;
	}
	public String getCookie() {
		return m_strCookie;
	}
	public void setCookie(String strCookie) {
		this.m_strCookie = strCookie;
	}
}
