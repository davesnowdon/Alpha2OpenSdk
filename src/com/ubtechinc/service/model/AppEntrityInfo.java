package com.ubtechinc.service.model;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class AppEntrityInfo {
   @Index(0)
   private String packageName;
   @Index(1)
   private String name;
   @Index(2)
   private String appId;
   @Index(3)
   private String versionCode = "";
   @Index(4)
   private String versionName = "";
   @Index(5)
   private boolean isDownLoad;
   @Index(6)
   private String url = "";
   @Index(7)
   private byte[] icon;
   @Index(8)
   private boolean isSetting;
   @Index(9)
   private boolean isSystemApp;
   @Index(10)
   private boolean isButtonEvent;
   @Index(11)
   private int downLoadState;

   public AppEntrityInfo() {
   }

   public int getDownLoadState() {
      return this.downLoadState;
   }

   public void setDownLoadState(int downLoadState) {
      this.downLoadState = downLoadState;
   }

   public boolean isSystemApp() {
      return this.isSystemApp;
   }

   public void setSystemApp(boolean isSystemApp) {
      this.isSystemApp = isSystemApp;
   }

   public boolean isButtonEvent() {
      return this.isButtonEvent;
   }

   public void setButtonEvent(boolean isButtonEvent) {
      this.isButtonEvent = isButtonEvent;
   }

   public boolean isSetting() {
      return this.isSetting;
   }

   public void setSetting(boolean isSetting) {
      this.isSetting = isSetting;
   }

   public byte[] getIcon() {
      return this.icon;
   }

   public void setIcon(byte[] icon) {
      this.icon = icon;
   }

   public String getUrl() {
      return this.url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public boolean isDownLoad() {
      return this.isDownLoad;
   }

   public void setDownLoad(boolean isDownLoad) {
      this.isDownLoad = isDownLoad;
   }

   public String getAppId() {
      return this.appId;
   }

   public void setAppId(String appId) {
      this.appId = appId;
   }

   public String getVersionCode() {
      return this.versionCode;
   }

   public void setVersionCode(String versionCode) {
      this.versionCode = versionCode;
   }

   public String getVersionName() {
      return this.versionName;
   }

   public void setVersionName(String versionName) {
      this.versionName = versionName;
   }

   public String getPackageName() {
      return this.packageName;
   }

   public void setPackageName(String packageName) {
      this.packageName = packageName;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
