// AIDL for the robot's chest / head serial-port services
// (com.ubtechinc.services.AlphaSerialPortServices and AlphaSerialPortHeaderServices).
// Method order defines the Binder transaction ids - must match the on-robot service.
package com.ubtechinc.alpha2serverlib.aidlinterface;

import com.ubtechinc.alpha2serverlib.aidlinterface.IAlpha2SerialPortRcvClient;

interface IAlpha2SerialPortService {
    int registerSerialPortRcvListener(IAlpha2SerialPortRcvClient cb);
    int unRegisterSerialPortRcvListener(IAlpha2SerialPortRcvClient cb);
    boolean sendCommand(byte nSessionID, byte nCmd, in byte[] nParam, int nLen);
    boolean sendRawData(in byte[] data, int nLen);
}
