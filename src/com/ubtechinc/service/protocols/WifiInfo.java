package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class WifiInfo {
   @Index(0)
   private String ssid;
   @Index(1)
   private int level;
   @Index(2)
   private String capabilities;
   @Index(3)
   private boolean isCurrentConnect;

   public WifiInfo() {
   }

   public String getSsid() {
      return this.ssid;
   }

   public void setSsid(String ssid) {
      this.ssid = ssid;
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   public String getCapabilities() {
      return this.capabilities;
   }

   public void setCapabilities(String capabilities) {
      this.capabilities = capabilities;
   }

   public boolean isCurrentConnect() {
      return this.isCurrentConnect;
   }

   public void setCurrentConnect(boolean isCurrentConnect) {
      this.isCurrentConnect = isCurrentConnect;
   }
}
