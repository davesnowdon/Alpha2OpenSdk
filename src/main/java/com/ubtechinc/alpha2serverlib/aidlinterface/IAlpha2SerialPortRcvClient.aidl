package com.ubtechinc.alpha2serverlib.aidlinterface;

interface IAlpha2SerialPortRcvClient 
{
	void onListenSerialPortRcvData(in byte[] bytes, int len);
}