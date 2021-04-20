package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class QuerySoftwareVersion {
   @Index(0)
   private boolean mIsFromClient;
   @Index(1)
   private String androidVersion = "";
   @Index(2)
   private String serviceVersion = "";
   @Index(3)
   private String headerVersion = "";
   @Index(4)
   private String chestVersion = "";
   @Index(5)
   private long sdTotalVolume = -1L;
   @Index(6)
   private long sdSurplusVolume = -1L;

   public QuerySoftwareVersion() {
   }

   public String getAndroidVersion() {
      return this.androidVersion;
   }

   public void setAndroidVersion(String androidVersion) {
      this.androidVersion = androidVersion;
   }

   public String getServiceVersion() {
      return this.serviceVersion;
   }

   public void setServiceVersion(String serviceVersion) {
      this.serviceVersion = serviceVersion;
   }

   public String getHeaderVersion() {
      return this.headerVersion;
   }

   public void setHeaderVersion(String headerVersion) {
      this.headerVersion = headerVersion;
   }

   public String getChestVersion() {
      return this.chestVersion;
   }

   public void setChestVersion(String chestVersion) {
      this.chestVersion = chestVersion;
   }

   public long getSdTotalVolume() {
      return this.sdTotalVolume;
   }

   public void setSdTotalVolume(long sdTotalVolume) {
      this.sdTotalVolume = sdTotalVolume;
   }

   public long getSdSurplusVolume() {
      return this.sdSurplusVolume;
   }

   public void setSdSurplusVolume(long sdSurplusVolume) {
      this.sdSurplusVolume = sdSurplusVolume;
   }

   public boolean ismIsFromClient() {
      return this.mIsFromClient;
   }

   public void setmIsFromClient(boolean mIsFromClient) {
      this.mIsFromClient = mIsFromClient;
   }
}
