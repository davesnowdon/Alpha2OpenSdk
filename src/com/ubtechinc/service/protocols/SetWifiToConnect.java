package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class SetWifiToConnect {
   @Index(0)
   private String ssid;
   @Index(1)
   private String psw;
   @Index(2)
   private String capabilities;
   @Index(3)
   private boolean knowWifi;

   public SetWifiToConnect() {
   }

   public String getSsid() {
      return this.ssid;
   }

   public void setSsid(String ssid) {
      this.ssid = ssid;
   }

   public String getPsw() {
      return this.psw;
   }

   public void setPsw(String psw) {
      this.psw = psw;
   }

   public boolean isKnowWifi() {
      return this.knowWifi;
   }

   public void setKnowWifi(boolean knowWifi) {
      this.knowWifi = knowWifi;
   }

   public String getCapabilities() {
      return this.capabilities;
   }

   public void setCapabilities(String capabilities) {
      this.capabilities = capabilities;
   }
}
