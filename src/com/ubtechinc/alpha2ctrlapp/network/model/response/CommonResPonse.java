package com.ubtechinc.alpha2ctrlapp.network.model.response;

public class CommonResPonse {
   private boolean status;
   private String info;
   private Object models;

   public CommonResPonse() {
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

   public Object getModels() {
      return this.models;
   }

   public void setModels(Object models) {
      this.models = models;
   }
}
