package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class ChatLogRequest extends CommonRequest {
   private String robotSeq;
   private String uploadContext;
   private String languageVersion;
   private String appVersion;

   public ChatLogRequest() {
   }

   public String getRobotSeq() {
      return this.robotSeq;
   }

   public void setRobotSeq(String robotSeq) {
      this.robotSeq = robotSeq;
   }

   public String getUploadContext() {
      return this.uploadContext;
   }

   public void setUploadContext(String uploadContext) {
      this.uploadContext = uploadContext;
   }

   public String getLanguageVersion() {
      return this.languageVersion;
   }

   public void setLanguageVersion(String languageVersion) {
      this.languageVersion = languageVersion;
   }

   public String getAppVersion() {
      return this.appVersion;
   }

   public void setAppVersion(String appVersion) {
      this.appVersion = appVersion;
   }
}
