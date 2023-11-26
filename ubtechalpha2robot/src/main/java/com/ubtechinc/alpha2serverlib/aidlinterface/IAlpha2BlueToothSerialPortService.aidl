package com.ubtechinc.alpha2serverlib.aidlinterface;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient;

interface IAlpha2BlueToothSerialPortService {
	
	int registerSerialPortRcvListener(IAlpha2SerialPortRcvClient cb);
	int unRegisterSerialPortRcvListener(IAlpha2SerialPortRcvClient cb);
	
	boolean sendCommand(byte nSessionID, byte nCmd, in byte[] nParam, int nLen);
	void sendATCMD(String params);
	
}