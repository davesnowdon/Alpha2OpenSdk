package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class AuthenticationRequest extends CommonRequest {
   private String appKey;
   private String appPackage;

   public AuthenticationRequest() {
   }

   public String getAppPackage() {
      return this.appPackage;
   }

   public void setAppPackage(String appPackage) {
      this.appPackage = appPackage;
   }

   public String getAppKey() {
      return this.appKey;
   }

   public void setAppKey(String appKey) {
      this.appKey = appKey;
   }
}
