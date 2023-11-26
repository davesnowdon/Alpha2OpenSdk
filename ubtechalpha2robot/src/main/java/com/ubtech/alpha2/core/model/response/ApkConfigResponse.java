package com.ubtech.alpha2.core.model.response;

public class ApkConfigResponse {
   private String apkVersion;
   private String uploadFilePath;

   public ApkConfigResponse() {
   }

   public String getApkVersion() {
      return this.apkVersion;
   }

   public void setApkVersion(String apkVersion) {
      this.apkVersion = apkVersion;
   }

   public String getUploadFilePath() {
      return this.uploadFilePath;
   }

   public void setUploadFilePath(String uploadFilePath) {
      this.uploadFilePath = uploadFilePath;
   }
}
