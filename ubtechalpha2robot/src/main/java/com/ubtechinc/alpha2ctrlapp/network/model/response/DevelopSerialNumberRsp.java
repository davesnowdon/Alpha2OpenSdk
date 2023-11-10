package com.ubtechinc.alpha2ctrlapp.network.model.response;

public class DevelopSerialNumberRsp {
   private boolean status;
   private String info;
   private String models;

   public DevelopSerialNumberRsp() {
   }

   public boolean getStatus() {
      return this.status;
   }

   public void setStatus(boolean status) {
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
