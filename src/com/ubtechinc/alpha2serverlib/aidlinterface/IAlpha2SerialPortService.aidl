package com.ubtechinc.alpha2serverlib.aidlinterface;
import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient;

interface IAlpha2SerialPortService {
	
	int registerSerialPortRcvListener(IAlpha2SerialPortRcvClient cb);
	int unRegisterSerialPortRcvListener(IAlpha2SerialPortRcvClient cb);
	
	boolean sendCommand(byte nSessionID, byte nCmd, in byte[] nParam, int nLen);
	boolean sendRawData(in byte[] data, int nLen);
}