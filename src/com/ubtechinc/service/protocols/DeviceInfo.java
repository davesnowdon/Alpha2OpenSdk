package com.ubtechinc.service.protocols;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import com.ubtechinc.utils.WifiControl;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class DeviceInfo {
   @Index(0)
   private String mDevName;
   @Index(1)
   private String mOsVersion;
   @Index(2)
   private String mIpAddress;
   @Index(3)
   private String mMacAddress;
   @Index(4)
   private String robotNo;

   public String getRobotNo() {
      return this.robotNo;
   }

   public void setRobotNo(String robotNo) {
      this.robotNo = robotNo;
   }

   public String getmMacAddress() {
      return this.mMacAddress;
   }

   public void setmMacAddress(String mMacAddress) {
      this.mMacAddress = mMacAddress;
   }

   private String intToIp(int i) {
      return (i & 255) + "." + (i >> 8 & 255) + "." + (i >> 16 & 255) + "." + (i >> 24 & 255);
   }

   public DeviceInfo(Context context) {
      this.mDevName = Build.MODEL;
      this.mOsVersion = VERSION.RELEASE;
      WifiControl wifiControl = new WifiControl(context);
      int nAddress = wifiControl.getIPAddress();
      this.mIpAddress = this.intToIp(nAddress);
      this.mMacAddress = wifiControl.getMacAddress();
      this.robotNo = this.mMacAddress.replace(":", "");
   }

   public DeviceInfo() {
   }

   public String getmDevName() {
      return this.mDevName;
   }

   public void setmDevName(String mDevName) {
      this.mDevName = mDevName;
   }

   public String getmOsVersion() {
      return this.mOsVersion;
   }

   public void setmOsVersion(String mOsVersion) {
      this.mOsVersion = mOsVersion;
   }

   public String getmIpAddress() {
      return this.mIpAddress;
   }

   public void setmIpAddress(String mIpAddress) {
      this.mIpAddress = mIpAddress;
   }
}
