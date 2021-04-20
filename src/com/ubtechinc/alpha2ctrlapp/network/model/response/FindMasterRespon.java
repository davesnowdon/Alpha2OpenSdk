package com.ubtechinc.alpha2ctrlapp.network.model.response;

public class FindMasterRespon {
   private boolean status;
   private String info;
   private FindMasterModel[] models;

   public FindMasterRespon() {
   }

   public boolean isStatus() {
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

   public FindMasterModel[] getModels() {
      return this.models;
   }

   public void setModels(FindMasterModel[] models) {
      this.models = models;
   }
}
