package com.ubtechinc.alpha2ctrlapp.network.model;

public class AuthenticationModel {
   private String appKey;
   private String appVersion;

   public AuthenticationModel() {
   }

   public String getAppVersion() {
      return this.appVersion;
   }

   public void setAppVersion(String appVersion) {
      this.appVersion = appVersion;
   }
}
