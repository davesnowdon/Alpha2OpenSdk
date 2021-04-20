package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class CommonRequest {
   private String appType = "2";
   private String serviceVersion = "V1.0";
   private String requestKey;
   private String requestTime;
   private String systemLanguage;

   public CommonRequest() {
   }

   public String getAppType() {
      return this.appType;
   }

   public void setAppType(String appType) {
      this.appType = appType;
   }

   public String getServiceVersion() {
      return this.serviceVersion;
   }

   public void setServiceVersion(String serviceVersion) {
      this.serviceVersion = serviceVersion;
   }

   public String getRequestKey() {
      return this.requestKey;
   }

   public void setRequestKey(String requestKey) {
      this.requestKey = requestKey;
   }

   public String getRequestTime() {
      return this.requestTime;
   }

   public void setRequestTime(String requestTime) {
      this.requestTime = requestTime;
   }

   public String getSystemLanguage() {
      return this.systemLanguage;
   }

   public void setSystemLanguage(String systemLanguage) {
      this.systemLanguage = systemLanguage;
   }
}
