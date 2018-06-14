package com.thingzdo.smartplug.udpserver.commdef;

public class FuncKeyType {
	private String m_strKeyValue;
	
	public String getStrKeyValue() {
		return m_strKeyValue;
	}

	public void setStrKeyValue(String strKeyValue) {
		m_strKeyValue = strKeyValue;
	}

	public FuncKeyType(String strCmdHead, String strVersion)
	{
		setStrKeyValue(strCmdHead + strVersion);
	}
	
    public boolean equals(Object obj) {
        if(obj instanceof FuncKeyType)
        {
        	FuncKeyType oo = (FuncKeyType)obj;
            return oo.getStrKeyValue()==this.getStrKeyValue();
        }
        else
        {
            return false;
        }
    }
    public int hashCode() {
        return m_strKeyValue.hashCode();
    }
}
