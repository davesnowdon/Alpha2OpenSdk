package com.ubtechinc.alpha2ctrlapp.network.model.response;

public class VerificationResponse {
   private String status;
   private String info;
   private String models;

   public VerificationResponse() {
   }

   public String getStatus() {
      return this.status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public String getInfo() {
      return this.info;
   }

   public void setInfo(String info) {
      this.info = info;
   }

   public String getModels() {
      return this.models;
   }

   public void setModels(String models) {
      this.models = models;
   }
}
